package com.adrencina.enchu.domain.use_case

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.data.model.SyncState
import com.adrencina.enchu.domain.repository.FileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class SaveFileToWorkUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(workId: String, sourceUri: Uri): Result<FileEntity> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not authenticated"))

            val fileId = UUID.randomUUID().toString()
            val fileName = getFileName(sourceUri) ?: fileId
            // Manejo más seguro de la extensión
            val fileExtension = fileName.substringAfterLast('.', "").ifEmpty {
                // Intentar obtener la extensión desde el MIME type si no está en el nombre
                context.contentResolver.getType(sourceUri)?.let { mime ->
                    android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                } ?: ""
            }

            val destinationDir = File(context.getExternalFilesDir(null), "works/$workId/files")
            if (!destinationDir.exists()) destinationDir.mkdirs()
            val destinationFile = File(destinationDir, "$fileId.$fileExtension")

            var size = 0L
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    size = inputStream.copyTo(outputStream)
                }
            }

            val checksum = calculateSHA256(destinationFile)
            val thumbnailPath = generateThumbnail(destinationFile, fileId)

            val fileEntity = FileEntity(
                fileId = fileId,
                workId = workId,
                userId = userId, // Asegúrate que tu FileEntity tenga este campo si lo usas
                fileName = fileName,
                mimeType = context.contentResolver.getType(sourceUri) ?: "application/octet-stream",
                size = size,
                checksum = checksum,
                localPath = destinationFile.absolutePath,
                thumbnailPath = thumbnailPath,
                remoteUrl = null, // Inicia como null
                syncState = SyncState.PENDING,
                createdAt = Date(),
                updatedAt = Date()
            )

            // Asumiendo que saveFileEntity es un método en tu Impl (aunque es mejor hacerlo vía interfaz)
            // Esta línea es la que guarda en Room
            (fileRepository as com.adrencina.enchu.data.repository.FileRepositoryImpl).saveFileEntity(fileEntity)

            // Esta línea es la que encola el trabajo de subida
            fileRepository.enqueueUpload(fileId)

            Result.success(fileEntity)
        } catch (e: Exception) {
            // Loguear el error es buena idea
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (result != null) {
                    result = result.substring(cut!! + 1)
                }
            }
        }
        return result
    }

    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val byteArray = ByteArray(8192) // Buffer más grande
            var bytesCount = fis.read(byteArray)
            while (bytesCount != -1) {
                digest.update(byteArray, 0, bytesCount)
                bytesCount = fis.read(byteArray)
            }
        }
        return digest.digest().fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun generateThumbnail(file: File, fileId: String): String? {
        // Usar un URI del archivo para obtener el mime-type de forma más fiable
        val fileUri = Uri.fromFile(file)
        val mimeType = context.contentResolver.getType(fileUri) ?: return null

        if (!mimeType.startsWith("image/")) return null // Solo para imágenes

        try {
            val thumbnailDir = File(context.getExternalFilesDir(null), "works/thumbnails")
            if (!thumbnailDir.exists()) thumbnailDir.mkdirs()
            val thumbnailFile = File(thumbnailDir, "$fileId.jpg")

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, 150, 150)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
            val rotatedBitmap = rotateImageIfRequired(bitmap, file.absolutePath)

            thumbnailFile.outputStream().use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // Reciclar bitmaps si no estás en una versión muy nueva de Android
            bitmap.recycle()
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }

            return thumbnailFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null // Falló la generación del thumbnail
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(img: Bitmap, path: String): Bitmap {
        val ei: ExifInterface
        try {
            ei = ExifInterface(path)
        } catch (e: Exception) {
            e.printStackTrace()
            return img // No se pudo leer EXIF, devolver original
        }

        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
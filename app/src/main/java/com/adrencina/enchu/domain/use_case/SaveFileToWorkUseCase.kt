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
import com.adrencina.enchu.domain.repository.OrganizationRepository
import com.adrencina.enchu.domain.repository.FileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
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
    private val firestore: FirebaseFirestore,
    private val organizationRepository: OrganizationRepository,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(workId: String, sourceUri: Uri): Result<FileEntity> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not authenticated"))

            // 1. Verificación de Cuota (Freemium)
            val userSnap = firestore.collection("users").document(userId).get().await()
            val orgId = userSnap.getString("organizationId") ?: ""
            
            if (orgId.isNotEmpty()) {
                val orgSnap = firestore.collection("organizations").document(orgId).get().await()
                val plan = orgSnap.getString("plan") ?: "FREE"
                val storageUsed = orgSnap.getLong("storageUsed") ?: 0L
                
                // Estimar tamaño del nuevo archivo (aproximado)
                val estimatedSize = context.contentResolver.openFileDescriptor(sourceUri, "r")?.statSize ?: 0L
                
                val limitBytes = 50 * 1024 * 1024 // 50 MB
                
                if (plan == "FREE" && (storageUsed + estimatedSize) > limitBytes) {
                     return@withContext Result.failure(Exception("Has alcanzado el límite de 50MB de almacenamiento. Actualiza a PRO para subir más archivos."))
                }
            }

            val fileId = UUID.randomUUID().toString()
            val fileName = getFileName(sourceUri) ?: fileId
            val mimeType = context.contentResolver.getType(sourceUri) ?: "application/octet-stream"
            
            val fileExtension = fileName.substringAfterLast('.', "").ifEmpty {
                android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
            }

            val destinationDir = File(context.getExternalFilesDir(null), "works/$workId/files")
            if (!destinationDir.exists()) destinationDir.mkdirs()
            val destinationFile = File(destinationDir, "$fileId.$fileExtension")

            var size = 0L
            val isImage = mimeType.startsWith("image/")

            if (isImage) {
                // --- COMPRESIÓN DE IMAGEN ---
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it, null, options) }
                
                options.inSampleSize = calculateInSampleSize(options, 1280, 1280)
                options.inJustDecodeBounds = false
                
                val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { 
                    BitmapFactory.decodeStream(it, null, options) 
                }
                
                if (bitmap != null) {
                    destinationFile.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    size = destinationFile.length()
                    bitmap.recycle()
                } else {
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        destinationFile.outputStream().use { output ->
                            size = input.copyTo(output)
                        }
                    }
                }
            } else {
                // --- OTROS ARCHIVOS ---
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        size = inputStream.copyTo(outputStream)
                    }
                }
            }

            val checksum = calculateSHA256(destinationFile)
            val thumbnailPath = if (isImage) generateThumbnail(destinationFile, fileId) else null

            val fileEntity = FileEntity(
                fileId = fileId,
                workId = workId,
                userId = userId,
                fileName = fileName,
                mimeType = mimeType,
                size = size,
                checksum = checksum,
                localPath = destinationFile.absolutePath,
                thumbnailPath = thumbnailPath,
                remoteUrl = null,
                syncState = SyncState.PENDING,
                createdAt = Date(),
                updatedAt = Date()
            )

            (fileRepository as com.adrencina.enchu.data.repository.FileRepositoryImpl).saveFileEntity(fileEntity)
            fileRepository.enqueueUpload(fileId)

            // 2. Incrementar uso de almacenamiento
            if (orgId.isNotEmpty()) {
                organizationRepository.incrementStorageUsed(orgId, size)
            }

            Result.success(fileEntity)
        } catch (e: Exception) {
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
            val byteArray = ByteArray(8192)
            var bytesCount = fis.read(byteArray)
            while (bytesCount != -1) {
                digest.update(byteArray, 0, bytesCount)
                bytesCount = fis.read(byteArray)
            }
        }
        return digest.digest().fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun generateThumbnail(file: File, fileId: String): String? {
        val fileUri = Uri.fromFile(file)
        val mimeType = context.contentResolver.getType(fileUri) ?: return null

        if (!mimeType.startsWith("image/")) return null

        try {
            val thumbnailDir = File(context.getExternalFilesDir(null), "thumbnails")
            if (!thumbnailDir.exists()) thumbnailDir.mkdirs()
            val thumbnailFile = File(thumbnailDir, "thumb_$fileId.jpg")

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            options.inSampleSize = calculateInSampleSize(options, 200, 200)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
            
            // Rotar thumbnail si el original tenía rotación
            val rotatedBitmap = rotateImageIfRequired(bitmap, file.absolutePath)

            thumbnailFile.outputStream().use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            }
            
            bitmap.recycle()
            if (rotatedBitmap != bitmap) rotatedBitmap.recycle()

            return thumbnailFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
            return img 
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
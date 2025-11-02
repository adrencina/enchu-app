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
            val fileExtension = fileName.substringAfterLast('.', "")

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
                userId = userId,
                fileName = fileName,
                mimeType = context.contentResolver.getType(sourceUri) ?: "",
                size = size,
                checksum = checksum,
                localPath = destinationFile.absolutePath,
                thumbnailPath = thumbnailPath,
                syncState = SyncState.PENDING,
                createdAt = Date(),
                updatedAt = Date()
            )

            (fileRepository as com.adrencina.enchu.data.repository.FileRepositoryImpl).saveFileEntity(fileEntity)
            fileRepository.enqueueUpload(fileId)

            Result.success(fileEntity)
        } catch (e: Exception) {
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
            val byteArray = ByteArray(1024)
            var bytesCount = fis.read(byteArray)
            while (bytesCount != -1) {
                digest.update(byteArray, 0, bytesCount)
                bytesCount = fis.read(byteArray)
            }
        }
        return digest.digest().fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun generateThumbnail(file: File, fileId: String): String? {
        val mimeType = context.contentResolver.getType(Uri.fromFile(file)) ?: return null
        if (!mimeType.startsWith("image/")) return null

        val thumbnailDir = File(context.getExternalFilesDir(null), "works/thumbnails")
        if (!thumbnailDir.exists()) thumbnailDir.mkdirs()
        val thumbnailFile = File(thumbnailDir, "$fileId.jpg")

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        options.inSampleSize = calculateInSampleSize(options, 150, 150)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        val rotatedBitmap = rotateImageIfRequired(bitmap, file.absolutePath)

        thumbnailFile.outputStream().use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return thumbnailFile.absolutePath
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
        val ei = ExifInterface(path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }
}

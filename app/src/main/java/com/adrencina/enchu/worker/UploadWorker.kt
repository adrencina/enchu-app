package com.adrencina.enchu.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adrencina.enchu.data.local.FileDao
import com.adrencina.enchu.data.model.SyncState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.io.File

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileDao: FileDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString("fileId") ?: return Result.failure()

        return try {
            // 1. Marcar como UPLOADING en Room
            fileDao.updateSyncState(fileId, SyncState.UPLOADING)

            // 2. Obtener entidad local
            val fileEntity = fileDao.getFileById(fileId) ?: return Result.failure()
            val file = File(fileEntity.localPath)

            // 3. Subir a Firebase Storage
            val workId = fileEntity.workId
            val fileName = file.name
            val storageRef = storage.reference.child("obras/$workId/files/$fileName")

            // Adjuntar metadatos con el userId para la regla de seguridad
            val storageMetadata = StorageMetadata.Builder()
                .setCustomMetadata("userId", fileEntity.userId)
                .build()

            storageRef.putFile(Uri.fromFile(file), storageMetadata).await()
            val remoteUrl = storageRef.downloadUrl.await().toString()

            // 4. Guardar metadata en Firestore
            val firestoreDoc = firestore.collection("obras")
                .document(workId)
                .collection("files")
                .document(fileId)

            val metadata = fileEntity.copy(
                remoteUrl = remoteUrl,
                syncState = SyncState.SYNCED,
                updatedAt = java.util.Date()
            )

            firestoreDoc.set(metadata).await()

            // 5. Actualizar estado en Room
            fileDao.updateFileStatus(fileId, remoteUrl, SyncState.SYNCED)

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            // Si algo falla, volver a estado PENDING para reintentar
            fileDao.updateSyncState(fileId, SyncState.PENDING)
            Result.retry()
        }
    }
}
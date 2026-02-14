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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.io.File

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileDao: FileDao,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString("fileId") ?: return Result.failure()

        // 1. Obtener metadata de Room
        val fileEntity = fileDao.getFileById(fileId) ?: return Result.failure()

        // Si ya está sincronizado, salir
        if (fileEntity.syncState == SyncState.SYNCED) return Result.success()

        return try {
            // 2. Marcar como UPLOADING en Room
            fileDao.updateSyncState(fileId, SyncState.UPLOADING)

            // 3. Subir a Firebase Storage
            val localFile = File(fileEntity.localPath)
            if (!localFile.exists()) {
                // Error fatal: el archivo local no existe
                fileDao.updateSyncState(fileId, SyncState.FAILED) // Asumimos FAILED state existe o usamos CONFLICT
                return Result.failure()
            }

            val storageRef = storage.reference.child("obras/${fileEntity.workId}/${fileEntity.fileName}")

            // Obtener el organizationId de la Obra en Firestore para asegurar consistencia
            val obraSnap = firestore.collection("obras").document(fileEntity.workId).get().await()
            val organizationId = obraSnap.getString("organizationId") ?: ""

            // Add metadata for security rules (Owner and Organization)
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setCustomMetadata("ownerId", fileEntity.userId)
                .setCustomMetadata("organizationId", organizationId)
                .build()

            storageRef.putFile(Uri.fromFile(localFile), metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // 4. Actualizar estado en Room (ÉXITO)
            fileDao.updateFileStatus(fileId, downloadUrl, SyncState.SYNCED)

            // Opcional: Notificar a Firestore aquí si necesitamos actualizar un documento con la URL
            // Por ahora, el sistema está desacoplado: la UI lee de Room para la imagen local y de Firestore para otros datos.
            // Si Firestore necesita la URL, deberíamos actualizar el documento de la Obra aquí también.
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, volver a PENDING para reintento, o usar retry policy de WorkManager
            fileDao.updateSyncState(fileId, SyncState.PENDING) 
            Result.retry()
        }
    }
}

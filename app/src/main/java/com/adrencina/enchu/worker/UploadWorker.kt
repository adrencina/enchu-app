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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString("fileId") ?: return Result.failure()

        try {
            fileDao.updateSyncState(fileId, SyncState.UPLOADING)

            val fileEntity = fileDao.getFileById(fileId) ?: return Result.failure()

            // Deduplication check (simplified)
            val existingFile = firestore.collectionGroup("files")
                .whereEqualTo("checksum", fileEntity.checksum)
                .limit(1)
                .get()
                .await()

            if (existingFile.documents.isNotEmpty()) {
                val remoteUrl = existingFile.documents[0].getString("remoteUrl") ?: ""
                fileDao.updateFileStatus(fileId, remoteUrl, SyncState.SYNCED)
                return Result.success()
            }

            val file = File(fileEntity.localPath)
            val storageRef = storage.reference.child("users/${fileEntity.userId}/${fileEntity.workId}/${file.name}")
            val uploadTask = storageRef.putFile(Uri.fromFile(file)).await()
            val remoteUrl = uploadTask.storage.downloadUrl.await().toString()

            val firestoreDoc = firestore.collection("users").document(fileEntity.userId)
                .collection("works").document(fileEntity.workId)
                .collection("files").document(fileId)

            firestoreDoc.set(fileEntity.copy(remoteUrl = remoteUrl, syncState = SyncState.SYNCED)).await()

            fileDao.updateFileStatus(fileId, remoteUrl, SyncState.SYNCED)

            return Result.success()
        } catch (e: Exception) {
            fileDao.updateSyncState(fileId, SyncState.PENDING) // Revert to PENDING for retry
            return Result.retry()
        }
    }
}

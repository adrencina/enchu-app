package com.adrencina.enchu.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class DeleteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val workId = inputData.getString(KEY_WORK_ID)
        val fileId = inputData.getString(KEY_FILE_ID)
        val storagePath = inputData.getString(KEY_STORAGE_PATH)

        if (workId.isNullOrBlank() || fileId.isNullOrBlank() || storagePath.isNullOrBlank()) {
            return Result.failure()
        }

        return try {
            // 1. Delete from Firebase Storage
            val storageRef = storage.getReferenceFromUrl(storagePath)
            storageRef.delete().await()

            // 2. Delete from Firestore
            val firestoreDoc = firestore.collection("obras")
                .document(workId)
                .collection("files")
                .document(fileId)
            firestoreDoc.delete().await()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If it fails, retry later
            Result.retry()
        }
    }

    companion object {
        const val KEY_WORK_ID = "workId"
        const val KEY_FILE_ID = "fileId"
        const val KEY_STORAGE_PATH = "storagePath"
    }
}

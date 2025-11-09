package com.adrencina.enchu.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class RenameWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_WORK_ID = "workId"
        const val KEY_FILE_ID = "fileId"
        const val KEY_NEW_NAME = "newName"
    }

    override suspend fun doWork(): Result {
        val workId = inputData.getString(KEY_WORK_ID)
        val fileId = inputData.getString(KEY_FILE_ID)
        val newName = inputData.getString(KEY_NEW_NAME)

        if (workId.isNullOrBlank() || fileId.isNullOrBlank() || newName.isNullOrBlank()) {
            return Result.failure()
        }

        return try {
            val fileDocRef = firestore.collection("obras").document(workId)
                .collection("files").document(fileId)

            // Update the fileName field in the Firestore document
            fileDocRef.update("fileName", newName).await()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If the task fails, it will be retried automatically based on the WorkManager policy
            Result.retry()
        }
    }
}

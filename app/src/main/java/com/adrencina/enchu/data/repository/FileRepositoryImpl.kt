package com.adrencina.enchu.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.*
import com.adrencina.enchu.data.local.FileDao
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.data.model.SyncState
import com.adrencina.enchu.domain.repository.FileRepository
import com.adrencina.enchu.worker.DeleteWorker
import com.adrencina.enchu.worker.RenameWorker
import com.adrencina.enchu.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val fileDao: FileDao,
    @ApplicationContext private val context: Context
) : FileRepository {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun saveFileToWork(workId: String, sourceUri: Uri): Result<FileEntity> {
        // This method is now handled by the SaveFileToWorkUseCase
        // The repository will only be responsible for database and network operations
        TODO("Refactor this method to only handle DB operations")
    }

    suspend fun saveFileEntity(fileEntity: FileEntity) {
        fileDao.upsert(fileEntity)
    }

    override fun observeFilesForWork(workId: String): Flow<PagingData<FileEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fileDao.filesForWork(workId) }
        ).flow
    }

    override suspend fun enqueueUpload(fileId: String) {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf("fileId" to fileId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueue(uploadWorkRequest)
    }

    override suspend fun downloadFile(fileId: String): Result<FileEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFile(file: FileEntity) {
        // 1. Delete local file if it exists
        file.localPath?.let {
            try {
                File(it).delete()
            } catch (e: Exception) {
                // Log error or handle case where file doesn't exist
                e.printStackTrace()
            }
        }

        // 2. Delete from local database
        fileDao.delete(file.fileId)

        // 3. If synced, enqueue a worker to delete from cloud
        if (file.syncState == SyncState.SYNCED && file.remoteUrl != null) {
            val deleteWorkRequest = OneTimeWorkRequestBuilder<DeleteWorker>()
                .setInputData(workDataOf(
                    DeleteWorker.KEY_WORK_ID to file.workId,
                    DeleteWorker.KEY_FILE_ID to file.fileId,
                    DeleteWorker.KEY_STORAGE_PATH to file.remoteUrl
                ))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueue(deleteWorkRequest)
        }
    }

    override suspend fun renameFile(file: FileEntity, newName: String) {
        // 1. Rename local file
        val oldFile = File(file.localPath)
        val newFile = File(oldFile.parent, newName)

        if (!oldFile.renameTo(newFile)) {
            // Handle error: unable to rename file
            // You might want to throw an exception or return a Result object
            return
        }

        // 2. Update entity in local database
        val updatedFileEntity = file.copy(
            fileName = newName,
            localPath = newFile.absolutePath,
            updatedAt = java.util.Date()
        )
        fileDao.upsert(updatedFileEntity)

        // 3. If synced, enqueue a worker to rename in cloud
        if (file.syncState == SyncState.SYNCED) {
            val renameWorkRequest = OneTimeWorkRequestBuilder<RenameWorker>()
                .setInputData(workDataOf(
                    RenameWorker.KEY_WORK_ID to file.workId,
                    RenameWorker.KEY_FILE_ID to file.fileId,
                    RenameWorker.KEY_NEW_NAME to newName
                ))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueue(renameWorkRequest)
        }
    }
}

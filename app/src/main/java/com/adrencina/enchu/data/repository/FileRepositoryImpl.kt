package com.adrencina.enchu.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.adrencina.enchu.data.local.FileDao
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.domain.repository.FileRepository
import com.adrencina.enchu.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}

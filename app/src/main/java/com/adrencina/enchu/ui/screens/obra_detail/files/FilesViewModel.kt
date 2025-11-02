package com.adrencina.enchu.ui.screens.obra_detail.files

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workId: String = savedStateHandle.get<String>("obraId") ?: throw IllegalArgumentException("Obra ID missing from SavedStateHandle")

    val files: Flow<PagingData<FileEntity>> = fileRepository
        .observeFilesForWork(workId)
        .cachedIn(viewModelScope)

}

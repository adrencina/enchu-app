package com.adrencina.enchu.ui.screens.obra_detail.files

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class FileViewEffect {
    data class OpenFile(val intent: Intent) : FileViewEffect()
    data class ShareFile(val intent: Intent) : FileViewEffect()
    data class ShowError(val message: String) : FileViewEffect()
}

data class FilesUiState(
    val fileToModify: FileEntity? = null,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    @ApplicationContext private val context: Context, // Inyectar contexto de la aplicación
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workId: String = savedStateHandle.get<String>("obraId") ?: throw IllegalArgumentException("Obra ID missing from SavedStateHandle")

    val files: Flow<PagingData<FileEntity>> = fileRepository
        .observeFilesForWork(workId)
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val _viewEffect = Channel<FileViewEffect>()
    val viewEffect = _viewEffect.receiveAsFlow()

    fun onFileClicked(fileEntity: FileEntity) {
        viewModelScope.launch {
            if (fileEntity.localPath == null) {
                _viewEffect.send(FileViewEffect.ShowError("El archivo no tiene una ruta local."))
                return@launch
            }

            val file = File(fileEntity.localPath)
            if (!file.exists()) {
                _viewEffect.send(FileViewEffect.ShowError("El archivo no se encontró en el dispositivo."))
                return@launch
            }

            try {
                val authority = "${context.packageName}.provider"
                val uri = FileProvider.getUriForFile(context, authority, file)

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, fileEntity.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                _viewEffect.send(FileViewEffect.OpenFile(intent))
            } catch (e: Exception) {
                // Esto puede pasar si el FileProvider no está bien configurado o hay un problema de permisos
                e.printStackTrace()
                _viewEffect.send(FileViewEffect.ShowError("No se pudo abrir el archivo: ${e.message}"))
            }
        }
    }

    fun onRenameRequest(file: FileEntity) {
        _uiState.update { it.copy(fileToModify = file, showRenameDialog = true) }
    }

    fun onDeleteRequest(file: FileEntity) {
        _uiState.update { it.copy(fileToModify = file, showDeleteConfirmDialog = true) }
    }

    fun onShareRequest(file: FileEntity) {
        viewModelScope.launch {
            if (file.localPath == null) {
                _viewEffect.send(FileViewEffect.ShowError("El archivo no tiene una ruta local para compartir."))
                return@launch
            }

            val fileOnDisk = File(file.localPath)
            if (!fileOnDisk.exists()) {
                _viewEffect.send(FileViewEffect.ShowError("El archivo no se encontró en el dispositivo."))
                return@launch
            }

            try {
                val authority = "${context.packageName}.provider"
                val uri = FileProvider.getUriForFile(context, authority, fileOnDisk)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = file.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Compartir archivo...")
                _viewEffect.send(FileViewEffect.ShareFile(chooserIntent))

            } catch (e: Exception) {
                e.printStackTrace()
                _viewEffect.send(FileViewEffect.ShowError("No se pudo compartir el archivo: ${e.message}"))
            }
        }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                fileToModify = null,
                showRenameDialog = false,
                showDeleteConfirmDialog = false
            )
        }
    }

    fun onConfirmRename(newName: String) {
        val fileToRename = _uiState.value.fileToModify
        if (fileToRename != null) {
            viewModelScope.launch {
                // Aquí iría la llamada al repositorio para renombrar
                fileRepository.renameFile(fileToRename, newName.take(100))
            }
        }
        onDismissDialog()
    }

    fun onConfirmDelete() {
        val fileToDelete = _uiState.value.fileToModify
        if (fileToDelete != null) {
            viewModelScope.launch {
                fileRepository.deleteFile(fileToDelete)
            }
        }
        onDismissDialog()
    }
}
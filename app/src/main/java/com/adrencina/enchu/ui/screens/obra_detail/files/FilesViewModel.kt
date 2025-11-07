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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class FileViewEffect {
    data class OpenFile(val intent: Intent) : FileViewEffect()
    data class ShowError(val message: String) : FileViewEffect()
}

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
}
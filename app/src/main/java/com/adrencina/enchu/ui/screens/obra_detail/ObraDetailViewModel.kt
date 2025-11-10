package com.adrencina.enchu.ui.screens.obra_detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.domain.use_case.SaveFileToWorkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Usamos una clase sellada para seguir el patrón de HomeUiState
sealed class ObraDetailUiState {
    object Loading : ObraDetailUiState()
    data class Success(
        val obra: Obra,
        val selectedTabIndex: Int = 0,
        val isMenuExpanded: Boolean = false,
        val showEditDialog: Boolean = false,
        val editedObraName: String = "",
        val editedObraDescription: String = "",
        val editedObraEstado: String = "",
        val isEditDialogExpanded: Boolean = false,
        val editedTelefono: String = "",
        val editedDireccion: String = ""
    ) : ObraDetailUiState()
    data class Error(val message: String) : ObraDetailUiState()
}

sealed class ObraDetailEffect {
    object NavigateBack : ObraDetailEffect()
    object LaunchFilePicker : ObraDetailEffect()
}

@HiltViewModel
class ObraDetailViewModel @Inject constructor(
    private val repository: ObraRepository,
    private val saveFileToWorkUseCase: SaveFileToWorkUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId")!!

    private val _uiState = MutableStateFlow<ObraDetailUiState>(ObraDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ObraDetailEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadObraDetails()
    }

    // --- Manejo de eventos con funciones públicas, como en HomeViewModel ---

    fun onTabSelected(index: Int) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(selectedTabIndex = index)
            } else {
                currentState // No hacer nada si no estamos en estado Success
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch { _effect.emit(ObraDetailEffect.NavigateBack) }
    }

    fun onMenuPressed() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(isMenuExpanded = true)
            } else {
                currentState
            }
        }
    }

    fun onDismissMenu() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(isMenuExpanded = false)
            } else {
                currentState
            }
        }
    }

    fun onEditObra() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(
                    isMenuExpanded = false,
                    showEditDialog = true,
                    editedObraName = currentState.obra.nombreObra,
                    editedObraDescription = currentState.obra.descripcion,
                    editedObraEstado = currentState.obra.estado,
                    editedTelefono = currentState.obra.telefono,
                    editedDireccion = currentState.obra.direccion
                )
            } else {
                currentState
            }
        }
    }

    fun onDismissEditDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                // Resetea el estado de expansión al cerrar el diálogo
                currentState.copy(showEditDialog = false, isEditDialogExpanded = false)
            } else {
                currentState
            }
        }
    }

    fun onToggleExpandEditDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(isEditDialogExpanded = !currentState.isEditDialogExpanded)
            } else {
                currentState
            }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedObraName = newName)
            } else {
                currentState
            }
        }
    }

    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedObraDescription = newDescription)
            } else {
                currentState
            }
        }
    }

    fun onEstadoChanged(newEstado: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedObraEstado = newEstado)
            } else {
                currentState
            }
        }
    }

    fun onTelefonoChanged(newTelefono: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedTelefono = newTelefono)
            } else {
                currentState
            }
        }
    }

    fun onDireccionChanged(newDireccion: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedDireccion = newDireccion)
            } else {
                currentState
            }
        }
    }

    fun onConfirmEdit() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ObraDetailUiState.Success) {
                val updatedObra = currentState.obra.copy(
                    nombreObra = currentState.editedObraName,
                    descripcion = currentState.editedObraDescription,
                    estado = currentState.editedObraEstado,
                    telefono = currentState.editedTelefono,
                    direccion = currentState.editedDireccion
                )
                repository.updateObra(updatedObra)
                // TODO: Handle success/error result with a Toast/Snackbar
            }
        }
        onDismissEditDialog()
    }

    fun onArchiveObra() {
        onDismissMenu()
        // TODO: Implement archive logic
    }

    fun onFabPressed() {
        if ((_uiState.value as? ObraDetailUiState.Success)?.selectedTabIndex == 1) {
            viewModelScope.launch { _effect.emit(ObraDetailEffect.LaunchFilePicker) }
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            saveFileToWorkUseCase(obraId, uri)
        }
    }

    private fun loadObraDetails() {
        viewModelScope.launch {
            repository.getObraById(obraId)
                .catch { exception ->
                    _uiState.value = ObraDetailUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obra ->
                    _uiState.value = ObraDetailUiState.Success(obra)
                }
        }
    }
}

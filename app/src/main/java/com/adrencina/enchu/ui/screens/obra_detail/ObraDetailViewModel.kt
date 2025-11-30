package com.adrencina.enchu.ui.screens.obra_detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Avance
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.Tarea
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.domain.use_case.SaveAvanceUseCase
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
        val tareas: List<Tarea> = emptyList(),
        val avances: List<Avance> = emptyList(),
        val selectedTabIndex: Int = 0,
        val isMenuExpanded: Boolean = false,
        val showEditDialog: Boolean = false,
        val showArchiveDialog: Boolean = false,
        val showAddAvanceDialog: Boolean = false, // Nuevo estado para diálogo de avance
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
    private val saveAvanceUseCase: SaveAvanceUseCase,
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
                currentState.copy(editedObraName = newName.take(50))
            } else {
                currentState
            }
        }
    }

    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedObraDescription = newDescription.take(200))
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
                currentState.copy(editedTelefono = newTelefono.take(20))
            } else {
                currentState
            }
        }
    }

    fun onDireccionChanged(newDireccion: String) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(editedDireccion = newDireccion.take(100))
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
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(showArchiveDialog = true)
            } else {
                currentState
            }
        }
    }

    fun onDismissArchiveDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(showArchiveDialog = false)
            } else {
                currentState
            }
        }
    }

    fun onConfirmArchive() {
        viewModelScope.launch {
            repository.archiveObra(obraId)
            onDismissArchiveDialog()
            _effect.emit(ObraDetailEffect.NavigateBack)
        }
    }

    fun onFabPressed() {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        when (currentState.selectedTabIndex) {
            0 -> { // Registros (Avances)
                _uiState.update { currentState.copy(showAddAvanceDialog = true) }
            }
            1 -> { // Archivos
                viewModelScope.launch { _effect.emit(ObraDetailEffect.LaunchFilePicker) }
            }
        }
    }

    fun onDismissAddAvanceDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(showAddAvanceDialog = false)
            } else {
                currentState
            }
        }
    }

    fun onConfirmAddAvance(descripcion: String, uris: List<Uri>) {
        viewModelScope.launch {
            // Mostrar loading si se desea, por ahora cerramos dialogo y procesamos en bg
            onDismissAddAvanceDialog()
            try {
                saveAvanceUseCase(obraId, descripcion, uris)
            } catch (e: Exception) {
                e.printStackTrace() // Log error to console
                _uiState.update { currentState ->
                     if (currentState is ObraDetailUiState.Success) {
                         ObraDetailUiState.Error("Error al crear avance: ${e.message}")
                     } else {
                         currentState
                     }
                }
            }
        }
    }

    fun onDeleteAvance(avance: Avance) {
        viewModelScope.launch {
            repository.deleteAvance(obraId, avance.id)
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            saveFileToWorkUseCase(obraId, uri)
        }
    }

    fun onAddTarea(descripcion: String) {
        if (descripcion.isBlank()) return
        viewModelScope.launch {
            val nuevaTarea = Tarea(descripcionTarea = descripcion)
            repository.addTarea(obraId, nuevaTarea)
        }
    }

    fun onToggleTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.updateTareaStatus(obraId, tarea.id, !tarea.completada)
        }
    }

    fun onDeleteTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.deleteTarea(obraId, tarea.id)
        }
    }

    private fun loadObraDetails() {
        viewModelScope.launch {
            combine(
                repository.getObraById(obraId),
                repository.getTareas(obraId),
                repository.getAvances(obraId)
            ) { obra, tareas, avances ->
                Triple(obra, tareas, avances)
            }
            .catch { exception ->
                _uiState.value = ObraDetailUiState.Error(exception.message ?: "Error desconocido")
            }
            .collect { (obra, tareas, avances) ->
                _uiState.update { currentState ->
                    if (currentState is ObraDetailUiState.Success) {
                        currentState.copy(obra = obra, tareas = tareas, avances = avances)
                    } else {
                        ObraDetailUiState.Success(obra = obra, tareas = tareas, avances = avances)
                    }
                }
            }
        }
    }
}

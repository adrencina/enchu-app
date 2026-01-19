package com.adrencina.enchu.ui.screens.obra_detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Avance
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.PresupuestoItem
import com.adrencina.enchu.data.model.Tarea
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.domain.use_case.GeneratePresupuestoPdfUseCase
import com.adrencina.enchu.domain.use_case.SaveAvanceUseCase
import com.adrencina.enchu.domain.use_case.SaveFileToWorkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.adrencina.enchu.data.repository.OrganizationRepository

// Usamos una clase sellada para seguir el patrón de HomeUiState
sealed class ObraDetailUiState {
    object Loading : ObraDetailUiState()
    data class Success(
        val obra: Obra,
        val tareas: List<Tarea> = emptyList(),
        val avances: List<Avance> = emptyList(),
        val presupuestoItems: List<PresupuestoItem> = emptyList(),
        val selectedTabIndex: Int = 0,
        val isMenuExpanded: Boolean = false,
        val showEditDialog: Boolean = false,
        val showArchiveDialog: Boolean = false,
        val showAddAvanceDialog: Boolean = false,
        val showAddPresupuestoItemDialog: Boolean = false,
        val showAddTareaDialog: Boolean = false, // Nuevo estado
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
    data class SharePdf(val file: File) : ObraDetailEffect()
}

@HiltViewModel
class ObraDetailViewModel @Inject constructor(
    private val repository: ObraRepository,
    private val saveFileToWorkUseCase: SaveFileToWorkUseCase,
    private val saveAvanceUseCase: SaveAvanceUseCase,
    private val generatePresupuestoPdfUseCase: GeneratePresupuestoPdfUseCase,
    private val organizationRepository: OrganizationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId") ?: ""

    private val _uiState = MutableStateFlow<ObraDetailUiState>(ObraDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ObraDetailEffect>()
    val effect = _effect.asSharedFlow()

    init {
        if (obraId.isBlank()) {
            _uiState.value = ObraDetailUiState.Error("Error de navegación: ID de obra no encontrado.")
        } else {
            loadObraDetails()
        }
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
            2 -> { // Tareas
                _uiState.update { currentState.copy(showAddTareaDialog = true) }
            }
            3 -> { // Presupuesto
                _uiState.update { currentState.copy(showAddPresupuestoItemDialog = true) }
            }
        }
    }

    fun onDismissAddTareaDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(showAddTareaDialog = false)
            } else {
                currentState
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
    
    fun onDismissAddPresupuestoItemDialog() {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(showAddPresupuestoItemDialog = false)
            } else {
                currentState
            }
        }
    }

    fun onAddPresupuestoItem(item: PresupuestoItem) {
        viewModelScope.launch {
            repository.addPresupuestoItem(obraId, item)
            onDismissAddPresupuestoItemDialog()
        }
    }
    
    fun onDeletePresupuestoItem(item: PresupuestoItem) {
        viewModelScope.launch {
            repository.deletePresupuestoItem(obraId, item.id)
        }
    }

    fun onExportPdf() {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            // Fetch organization if available
            val orgId = currentState.obra.organizationId
            val organization = if (orgId.isNotBlank()) {
                organizationRepository.getOrganization(orgId).first()
            } else {
                null
            }
            
            val file = generatePresupuestoPdfUseCase(currentState.obra, currentState.presupuestoItems, organization)
            _effect.emit(ObraDetailEffect.SharePdf(file))
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
        android.util.Log.d("ObraDetailViewModel", "Iniciando carga resiliente para obra: $obraId")
        
        // 1. Cargar la Obra (Principal)
        viewModelScope.launch {
            repository.getObraById(obraId)
                .catch { e ->
                    android.util.Log.e("ObraDetailViewModel", "Error cargando Obra", e)
                    _uiState.update { 
                        if (it is ObraDetailUiState.Success) it // Mantener estado previo si existe
                        else ObraDetailUiState.Error("No se pudo cargar la obra: ${e.message}") 
                    }
                }
                .collect { obra ->
                    _uiState.update { current ->
                        if (current is ObraDetailUiState.Success) {
                            current.copy(obra = obra)
                        } else {
                            // Primer éxito: inicializamos el estado Success
                            ObraDetailUiState.Success(obra = obra)
                        }
                    }
                    // Una vez que tenemos la obra, disparamos la carga de lo demás
                    loadSubCollections()
                }
        }
    }

    private fun loadSubCollections() {
        // Cargar Tareas
        viewModelScope.launch {
            repository.getTareas(obraId)
                .catch { e -> 
                    android.util.Log.e("ObraDetailViewModel", "Error cargando Tareas", e) 
                    // No hacemos nada, la lista se queda vacía por defecto en el estado Success
                }
                .collect { tareas ->
                    _uiState.update { if (it is ObraDetailUiState.Success) it.copy(tareas = tareas) else it }
                }
        }

        // Cargar Avances
        viewModelScope.launch {
            repository.getAvances(obraId)
                .catch { e -> android.util.Log.e("ObraDetailViewModel", "Error cargando Avances", e) }
                .collect { avances ->
                    _uiState.update { if (it is ObraDetailUiState.Success) it.copy(avances = avances) else it }
                }
        }

        // Cargar Presupuesto
        viewModelScope.launch {
            repository.getPresupuestoItems(obraId)
                .catch { e -> android.util.Log.e("ObraDetailViewModel", "Error cargando Presupuesto", e) }
                .collect { items ->
                    _uiState.update { if (it is ObraDetailUiState.Success) it.copy(presupuestoItems = items) else it }
                }
        }
    }
}

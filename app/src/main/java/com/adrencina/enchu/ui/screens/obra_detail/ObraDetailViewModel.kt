package com.adrencina.enchu.ui.screens.obra_detail
import com.adrencina.enchu.domain.model.EstadoObra

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.domain.model.Avance
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.domain.model.Movimiento
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.model.PresupuestoItem
import com.adrencina.enchu.domain.model.Tarea
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.domain.repository.ObraRepository
import com.adrencina.enchu.domain.repository.OrganizationRepository
import com.adrencina.enchu.domain.use_case.GeneratePresupuestoPdfUseCase
import com.adrencina.enchu.domain.use_case.SaveAvanceUseCase
import com.adrencina.enchu.domain.use_case.SaveFileToWorkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ObraDetailUiState {
    object Loading : ObraDetailUiState()
    data class Success(
        val obra: Obra,
        val tareas: List<Tarea> = emptyList(),
        val avances: List<Avance> = emptyList(),
        val presupuestoItems: List<PresupuestoItem> = emptyList(),
        val movimientos: List<Movimiento> = emptyList(),
        val allClientes: List<Cliente> = emptyList(),
        val selectedTabIndex: Int = 0,
        val isMenuExpanded: Boolean = false,
        val showEditDialog: Boolean = false,
        val showArchiveDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showAddAvanceDialog: Boolean = false,
        val showAddPresupuestoItemDialog: Boolean = false,
        val showAddTareaDialog: Boolean = false,
        val showAddMovimientoDialog: Boolean = false,
        val editedObraName: String = "",
        val editedObraDescription: String = "",
        val editedObraEstado: EstadoObra = EstadoObra.PRESUPUESTADO,
        val isEditDialogExpanded: Boolean = false,
        val editedTelefono: String = "",
        val editedDireccion: String = "",
        val editedCliente: Cliente? = null
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
    private val clienteRepository: ClienteRepository,
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

    private var dataLoadingJob: Job? = null

    init {
        if (obraId.isBlank()) {
            _uiState.value = ObraDetailUiState.Error("Error de navegación: ID de obra no encontrado.")
        } else {
            loadData()
        }
    }

    private fun loadData() {
        dataLoadingJob?.cancel()
        dataLoadingJob = viewModelScope.launch {
            // Cargar Obra (Trigger principal)
            launch {
                repository.getObraById(obraId)
                    .catch { e -> 
                        _uiState.value = ObraDetailUiState.Error("Error cargando obra: ${e.message}")
                    }
                    .collect { obra ->
                        if (obra != null) {
                            _uiState.update { current ->
                                if (current is ObraDetailUiState.Success) {
                                    current.copy(obra = obra)
                                } else {
                                    ObraDetailUiState.Success(obra = obra)
                                }
                            }
                        } else {
                            _uiState.value = ObraDetailUiState.Error("Obra no encontrada")
                        }
                    }
            }

            // Cargar Subcolecciones independientemente
            launch {
                repository.getTareas(obraId).collect { tareas ->
                    updateSuccessState { it.copy(tareas = tareas) }
                }
            }
            launch {
                repository.getAvances(obraId).collect { avances ->
                    updateSuccessState { it.copy(avances = avances) }
                }
            }
            launch {
                repository.getPresupuestoItems(obraId).collect { items ->
                    updateSuccessState { it.copy(presupuestoItems = items) }
                }
            }
            launch {
                repository.getMovimientos(obraId).collect { movimientos ->
                    updateSuccessState { it.copy(movimientos = movimientos) }
                }
            }
            launch {
                clienteRepository.getClientes().collect { clientes ->
                    updateSuccessState { it.copy(allClientes = clientes) }
                }
            }
        }
    }

    private fun updateSuccessState(update: (ObraDetailUiState.Success) -> ObraDetailUiState.Success) {
        _uiState.update { current ->
            if (current is ObraDetailUiState.Success) {
                update(current)
            } else {
                current
            }
        }
    }

    // --- Actions ---

    fun onTabSelected(index: Int) {
        updateSuccessState { it.copy(selectedTabIndex = index) }
    }

    fun onBackPressed() {
        viewModelScope.launch { _effect.emit(ObraDetailEffect.NavigateBack) }
    }

    fun onMenuPressed() {
        updateSuccessState { it.copy(isMenuExpanded = true) }
    }

    fun onDismissMenu() {
        updateSuccessState { it.copy(isMenuExpanded = false) }
    }

    fun onEditObra() {
        updateSuccessState { currentState ->
            val currentClient = currentState.allClientes.find { it.id == currentState.obra.clienteId }
            currentState.copy(
                isMenuExpanded = false,
                showEditDialog = true,
                editedObraName = currentState.obra.nombreObra,
                editedObraDescription = currentState.obra.descripcion,
                editedObraEstado = currentState.obra.estado,
                editedTelefono = currentState.obra.telefono,
                editedDireccion = currentState.obra.direccion,
                editedCliente = currentClient
            )
        }
    }

    fun onDismissEditDialog() {
        updateSuccessState { it.copy(showEditDialog = false, isEditDialogExpanded = false) }
    }

    fun onToggleExpandEditDialog() {
        updateSuccessState { it.copy(isEditDialogExpanded = !it.isEditDialogExpanded) }
    }

    fun onNameChanged(newName: String) {
        updateSuccessState { it.copy(editedObraName = newName.take(50)) }
    }

    fun onDescriptionChanged(newDescription: String) {
        updateSuccessState { it.copy(editedObraDescription = newDescription.take(200)) }
    }

    fun onEstadoChanged(newEstado: String) {
        updateSuccessState { it.copy(editedObraEstado = EstadoObra.fromValue(newEstado)) }
    }

    fun onTelefonoChanged(newTelefono: String) {
        updateSuccessState { it.copy(editedTelefono = newTelefono.take(20)) }
    }

    fun onDireccionChanged(newDireccion: String) {
        updateSuccessState { it.copy(editedDireccion = newDireccion.take(100)) }
    }

    fun onClienteChanged(cliente: Cliente) {
        updateSuccessState { it.copy(editedCliente = cliente) }
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
                    direccion = currentState.editedDireccion,
                    clienteId = currentState.editedCliente?.id ?: currentState.obra.clienteId,
                    clienteNombre = currentState.editedCliente?.nombre ?: currentState.obra.clienteNombre
                )
                repository.updateObra(updatedObra)
            }
        }
        onDismissEditDialog()
    }

    fun onDeleteObraClick() {
        onDismissMenu()
        updateSuccessState { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        updateSuccessState { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        dataLoadingJob?.cancel()
        onDismissDeleteDialog()
        viewModelScope.launch {
            try {
                repository.deleteObra(obraId)
                onBackPressed()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun onArchiveObra() {
        onDismissMenu()
        updateSuccessState { it.copy(showArchiveDialog = true) }
    }

    fun onDismissArchiveDialog() {
        updateSuccessState { it.copy(showArchiveDialog = false) }
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
            0 -> updateSuccessState { it.copy(showAddAvanceDialog = true) }
            1 -> viewModelScope.launch { _effect.emit(ObraDetailEffect.LaunchFilePicker) }
            2 -> updateSuccessState { it.copy(showAddTareaDialog = true) }
            3 -> updateSuccessState { it.copy(showAddPresupuestoItemDialog = true) }
            4 -> updateSuccessState { it.copy(showAddMovimientoDialog = true) }
        }
    }

    fun onDismissAddTareaDialog() {
        updateSuccessState { it.copy(showAddTareaDialog = false) }
    }

    fun onDismissAddAvanceDialog() {
        updateSuccessState { it.copy(showAddAvanceDialog = false) }
    }
    
    fun onDismissAddPresupuestoItemDialog() {
        updateSuccessState { it.copy(showAddPresupuestoItemDialog = false) }
    }

    fun onDismissAddMovimientoDialog() {
        updateSuccessState { it.copy(showAddMovimientoDialog = false) }
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

    fun onUpdateItemLogistics(item: PresupuestoItem, isComprado: Boolean, isInstalado: Boolean, costoReal: Double?) {
        viewModelScope.launch {
            repository.updateItemLogistics(obraId, item.id, isComprado, isInstalado, costoReal)
            
            // Si se marca como comprado y hay un costo real, registrar automáticamente en la Caja como EGRESO
            if (isComprado && costoReal != null) {
                val mov = Movimiento(
                    obraId = obraId,
                    descripcion = "Compra: ${item.descripcion}",
                    monto = costoReal * item.cantidad,
                    tipo = "EGRESO",
                    categoria = "MATERIALES"
                )
                repository.addMovimiento(obraId, mov)
            }
        }
    }

    fun onAddMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            repository.addMovimiento(obraId, movimiento.copy(obraId = obraId))
            onDismissAddMovimientoDialog()
        }
    }

    fun onDeleteMovimiento(movimientoId: String) {
        viewModelScope.launch {
            repository.deleteMovimiento(obraId, movimientoId)
        }
    }

    fun onExportPdf() {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            val orgId = currentState.obra.organizationId
            val organization = if (orgId.isNotBlank()) {
                organizationRepository.getOrganization(orgId).first()
            } else {
                null
            }

            var currentObra = currentState.obra

            if (currentObra.budgetNumber == 0 && organization != null) {
                val nextNumber = organization.lastBudgetNumber + 1
                val updatedOrg = organization.copy(lastBudgetNumber = nextNumber)
                organizationRepository.updateOrganization(updatedOrg)
                currentObra = currentObra.copy(budgetNumber = nextNumber)
                repository.updateObra(currentObra)
            }
            
            val file = generatePresupuestoPdfUseCase(currentObra, currentState.presupuestoItems, organization)
            _effect.emit(ObraDetailEffect.SharePdf(file))
        }
    }

    fun onConfirmAddAvance(descripcion: String, uris: List<Uri>) {
        viewModelScope.launch {
            onDismissAddAvanceDialog()
            try {
                saveAvanceUseCase(obraId, descripcion, uris)
            } catch (e: Exception) {
                e.printStackTrace()
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
}
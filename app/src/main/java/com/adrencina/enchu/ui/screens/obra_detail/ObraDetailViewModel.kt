package com.adrencina.enchu.ui.screens.obra_detail

import com.adrencina.enchu.domain.model.EstadoObra
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.domain.model.Avance
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.UserProfile
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
        val organizationMembers: List<UserProfile> = emptyList(),
        val selectedTabIndex: Int = 0,
        val isMenuExpanded: Boolean = false,
        val showEditDialog: Boolean = false,
        val showArchiveDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showAddAvanceDialog: Boolean = false,
        val showAddPresupuestoItemDialog: Boolean = false,
        val showAddTareaDialog: Boolean = false,
        val showAddMovimientoDialog: Boolean = false,
        val showAssignMemberDialog: Boolean = false,
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
    data class LaunchTaskPhotoPicker(val tareaId: String) : ObraDetailEffect()
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
    private val initialTab: Int = savedStateHandle.get<Int>("initialTab") ?: -1

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

    fun retry() {
        _uiState.value = ObraDetailUiState.Loading
        loadData()
    }

    private fun loadData() {
        dataLoadingJob?.cancel()
        dataLoadingJob = viewModelScope.launch {
            launch {
                repository.getObraById(obraId)
                    .catch { e -> 
                        _uiState.value = ObraDetailUiState.Error("Error cargando obra: ${e.message}")
                    }
                    .collect { obra ->
                        if (obra != null) {
                            updateSuccessStateOrCreate(obra)
                            if (obra.organizationId.isNotEmpty()) {
                                loadOrganizationMembers(obra.organizationId)
                            }
                        } else {
                            _uiState.value = ObraDetailUiState.Error("Obra no encontrada")
                        }
                    }
            }

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

    private fun loadOrganizationMembers(orgId: String) {
        viewModelScope.launch {
            organizationRepository.getMembers(orgId).collect { members ->
                updateSuccessState { it.copy(organizationMembers = members) }
            }
        }
    }

    private fun updateSuccessStateOrCreate(obra: Obra) {
        _uiState.update { current ->
            when (current) {
                is ObraDetailUiState.Success -> current.copy(obra = obra)
                else -> ObraDetailUiState.Success(
                    obra = obra,
                    selectedTabIndex = if (initialTab != -1) initialTab else 0
                )
            }
        }
    }

    private fun updateSuccessState(update: (ObraDetailUiState.Success) -> ObraDetailUiState.Success) {
        _uiState.update { current ->
            if (current is ObraDetailUiState.Success) update(current) else current
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

    fun onAssignMemberClick() {
        updateSuccessState { it.copy(showAssignMemberDialog = true, isMenuExpanded = false) }
    }

    fun onDismissAssignMemberDialog() {
        updateSuccessState { it.copy(showAssignMemberDialog = false) }
    }

    fun onToggleMemberAssignment(memberId: String, isAssigned: Boolean) {
        viewModelScope.launch {
            if (isAssigned) {
                repository.removeMemberFromObra(obraId, memberId)
            } else {
                repository.assignMemberToObra(obraId, memberId)
            }
        }
    }

    fun onUpdateMemberPermissions(memberId: String, permissions: com.adrencina.enchu.domain.model.MemberPermissions) {
        viewModelScope.launch {
            repository.updateMemberPermissions(obraId, memberId, permissions)
        }
    }

    fun onEditObra() {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        updateSuccessState { it.copy(
            showEditDialog = true, 
            isMenuExpanded = false,
            editedObraName = currentState.obra.nombreObra,
            editedObraDescription = currentState.obra.descripcion,
            editedObraEstado = currentState.obra.estado,
            editedTelefono = currentState.obra.telefono,
            editedDireccion = currentState.obra.direccion,
            editedCliente = currentState.allClientes.find { c -> c.id == currentState.obra.clienteId }
        ) }
    }

    fun onDismissEditDialog() {
        updateSuccessState { it.copy(showEditDialog = false) }
    }

    fun onUpdateObra(name: String, description: String, estado: EstadoObra, telefono: String, direccion: String, cliente: Cliente?) {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            val updatedObra = currentState.obra.copy(
                nombreObra = name,
                descripcion = description,
                estado = estado,
                telefono = telefono,
                direccion = direccion,
                clienteId = cliente?.id ?: "",
                clienteNombre = cliente?.nombre ?: ""
            )
            repository.updateObra(updatedObra)
            onDismissEditDialog()
        }
    }

    fun onArchiveObra() {
        updateSuccessState { it.copy(showArchiveDialog = true, isMenuExpanded = false) }
    }

    fun onConfirmArchive() {
        viewModelScope.launch {
            repository.archiveObra(obraId)
            onBackPressed()
        }
    }

    fun onDismissArchiveDialog() {
        updateSuccessState { it.copy(showArchiveDialog = false) }
    }

    fun onDeleteObra() {
        updateSuccessState { it.copy(showDeleteDialog = true, isMenuExpanded = false) }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            repository.deleteObra(obraId)
            onBackPressed()
        }
    }

    fun onDismissDeleteDialog() {
        updateSuccessState { it.copy(showDeleteDialog = false) }
    }

    fun onAddAvance() {
        updateSuccessState { it.copy(showAddAvanceDialog = true) }
    }

    fun onDismissAddAvanceDialog() {
        updateSuccessState { it.copy(showAddAvanceDialog = false) }
    }

    fun onAddPresupuestoItem() {
        updateSuccessState { it.copy(showAddPresupuestoItemDialog = true) }
    }

    fun onDismissAddPresupuestoItemDialog() {
        updateSuccessState { it.copy(showAddPresupuestoItemDialog = false) }
    }

    fun onAddTarea() {
        updateSuccessState { it.copy(showAddTareaDialog = true) }
    }

    fun onDismissAddTareaDialog() {
        updateSuccessState { it.copy(showAddTareaDialog = false) }
    }

    fun onAddMovimiento() {
        updateSuccessState { it.copy(showAddMovimientoDialog = true) }
    }

    fun onDismissAddMovimientoDialog() {
        updateSuccessState { it.copy(showAddMovimientoDialog = false) }
    }

    fun onConfirmAddMovimiento(movimiento: Movimiento) {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            repository.addMovimiento(
                obraId, 
                movimiento.copy(
                    obraId = obraId,
                    userId = currentState.obra.userId,
                    organizationId = currentState.obra.organizationId
                )
            )
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
            } else null
            
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
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            onDismissAddAvanceDialog()
            try {
                saveAvanceUseCase(
                    obraId, 
                    descripcion, 
                    uris, 
                    currentState.obra.userId, 
                    currentState.obra.organizationId
                )
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
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            val nuevaTarea = Tarea(
                descripcionTarea = descripcion,
                userId = currentState.obra.userId,
                organizationId = currentState.obra.organizationId
            )
            repository.addTarea(obraId, nuevaTarea)
        }
    }

    fun onToggleTarea(tarea: Tarea) {
        if (tarea.completada) return // Bloqueo total: no se puede desmarcar una tarea certificada
        
        viewModelScope.launch {
            // Como ya filtramos que no esté completada, aquí siempre pedimos foto
            _effect.emit(ObraDetailEffect.LaunchTaskPhotoPicker(tarea.id))
        }
    }

    fun onTaskPhotoSelected(tareaId: String, uri: Uri) {
        viewModelScope.launch {
            repository.completeTareaWithImage(obraId, tareaId, uri)
        }
    }

    fun onDeleteTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.deleteTarea(obraId, tarea.id)
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
        }
    }

    fun onFabPressed() {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        when (currentState.selectedTabIndex) {
            0 -> onAddAvance()
            1 -> viewModelScope.launch { _effect.emit(ObraDetailEffect.LaunchFilePicker) }
            2 -> onAddTarea()
            3 -> onAddPresupuestoItem()
            4 -> onAddMovimiento()
        }
    }

    fun onAddPresupuestoItem(item: PresupuestoItem) {
        val currentState = _uiState.value as? ObraDetailUiState.Success ?: return
        viewModelScope.launch {
            repository.addPresupuestoItem(obraId, item.copy(
                userId = currentState.obra.userId,
                organizationId = currentState.obra.organizationId
            ))
            onDismissAddPresupuestoItemDialog()
        }
    }
}

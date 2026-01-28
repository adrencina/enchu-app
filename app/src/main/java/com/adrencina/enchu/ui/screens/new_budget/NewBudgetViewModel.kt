package com.adrencina.enchu.ui.screens.new_budget

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.local.PresupuestoDao
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoItemEntity
import com.adrencina.enchu.data.model.PresupuestoItem
import com.adrencina.enchu.domain.use_case.GeneratePresupuestoPdfUseCase
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.OrganizationRepository
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.core.utils.BudgetNumberManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

data class NewBudgetUiState(
    val currentStep: Int = 1,
    val selectedClient: Cliente? = null,
    val budgetTitle: String = "",
    val items: List<PresupuestoItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val validity: String = "15",
    val notes: String = "",
    val discountInput: String = "",
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val budgetNumber: Int = 0,
    val isEditingSentBudget: Boolean = false
)

@HiltViewModel
class NewBudgetViewModel @Inject constructor(
    private val presupuestoDao: PresupuestoDao,
    savedStateHandle: SavedStateHandle,
    private val generatePresupuestoPdfUseCase: GeneratePresupuestoPdfUseCase,
    private val authRepository: AuthRepository,
    private val organizationRepository: OrganizationRepository,
    private val budgetNumberManager: BudgetNumberManager,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewBudgetUiState())
    val uiState: StateFlow<NewBudgetUiState> = _uiState.asStateFlow()

    private val budgetIdArg: String? = savedStateHandle["budgetId"]
    private val clientIdArg: String? = savedStateHandle["clientId"]
    private val presupuestoId = budgetIdArg ?: UUID.randomUUID().toString()

    init {
        if (budgetIdArg != null) {
            loadBudget(budgetIdArg)
        } else if (clientIdArg != null) {
            loadClient(clientIdArg)
        }
    }

    private fun loadClient(clientId: String) {
        viewModelScope.launch {
            try {
                val clientes = clienteRepository.getClientes().first()
                val client = clientes.find { it.id == clientId }
                
                if (client != null) {
                    _uiState.value = _uiState.value.copy(
                        selectedClient = client,
                        budgetTitle = "Presupuesto - ${client.nombre}",
                        currentStep = 2
                    )
                }
            } catch (e: Exception) {
                // Ignore pre-selection on error
            }
        }
    }

    private fun loadBudget(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val existingBudget = presupuestoDao.getPresupuestoWithItemsById(id)
            if (existingBudget != null) {
                val tempClient = Cliente(
                    id = existingBudget.presupuesto.clienteId,
                    nombre = existingBudget.presupuesto.clienteNombre,
                    direccion = existingBudget.presupuesto.clienteDireccion,
                    telefono = existingBudget.presupuesto.clienteTelefono,
                    email = existingBudget.presupuesto.clienteEmail,
                    organizationId = ""
                )
                
                val discPercent = existingBudget.presupuesto.descuento
                val discInput = if (discPercent == 0.0) "" else if (discPercent % 1.0 == 0.0) discPercent.toInt().toString() else discPercent.toString()
                
                val isSent = existingBudget.presupuesto.estado == "ENVIADO" || existingBudget.presupuesto.numero > 0

                _uiState.value = _uiState.value.copy(
                    currentStep = 3,
                    selectedClient = tempClient,
                    budgetTitle = existingBudget.presupuesto.titulo,
                    items = existingBudget.items,
                    subtotal = existingBudget.presupuesto.subtotal,
                    total = existingBudget.presupuesto.total,
                    validity = existingBudget.presupuesto.validez.toString(),
                    notes = existingBudget.presupuesto.notas,
                    discountPercent = discPercent,
                    discountInput = discInput,
                    budgetNumber = existingBudget.presupuesto.numero,
                    isEditingSentBudget = isSent,
                    isLoading = false
                )
                recalculateTotals()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onClientSelected(cliente: Cliente) {
        _uiState.value = _uiState.value.copy(
            selectedClient = cliente,
            budgetTitle = "Presupuesto - ${cliente.nombre}",
            currentStep = 2
        )
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(budgetTitle = newTitle)
    }

    fun onValidityChanged(newValidity: String) {
        if (newValidity.all { it.isDigit() }) {
             _uiState.value = _uiState.value.copy(validity = newValidity)
        }
    }

    fun onNotesChanged(newNotes: String) {
        _uiState.value = _uiState.value.copy(notes = newNotes)
    }
    
    fun onDiscountChanged(newInput: String) {
        if (newInput.isEmpty()) {
            _uiState.value = _uiState.value.copy(discountInput = "", discountPercent = 0.0)
            recalculateTotals()
            return
        }
        if (newInput.count { it == '.' } <= 1 && newInput.all { it.isDigit() || it == '.' }) {
            val discountVal = newInput.toDoubleOrNull() ?: 0.0
            val finalDiscount = discountVal.coerceIn(0.0, 100.0)
            _uiState.value = _uiState.value.copy(discountInput = newInput, discountPercent = finalDiscount)
            recalculateTotals()
        }
    }

    fun addItemFromMaterial(item: PresupuestoItem) {
        val newItem = PresupuestoItemEntity(
            presupuestoId = presupuestoId,
            descripcion = item.descripcion,
            cantidad = item.cantidad,
            precioUnitario = item.precioUnitario,
            tipo = item.tipo,
            fuente = "CATALOGO_LOCAL",
            orden = _uiState.value.items.size
        )
        val newItems = _uiState.value.items + newItem
        _uiState.value = _uiState.value.copy(items = newItems)
        recalculateTotals()
    }

    fun removeItem(index: Int) {
        val newItems = _uiState.value.items.toMutableList().apply { removeAt(index) }
        _uiState.value = _uiState.value.copy(items = newItems)
        recalculateTotals()
    }

    fun updateItem(index: Int, newQuantity: Double, newPrice: Double) {
        val newItems = _uiState.value.items.toMutableList().apply {
            val oldItem = this[index]
            this[index] = oldItem.copy(cantidad = newQuantity, precioUnitario = newPrice)
        }
        _uiState.value = _uiState.value.copy(items = newItems)
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val state = _uiState.value
        val subtotal = state.items.sumOf { it.cantidad * it.precioUnitario }
        val discountAmount = subtotal * (state.discountPercent / 100.0)
        val total = subtotal - discountAmount
        
        _uiState.value = state.copy(
            subtotal = subtotal,
            total = total,
            discountAmount = discountAmount
        )
    }

    fun nextStep() {
        if (_uiState.value.currentStep < 3) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            saveBudgetInternal(isSent = false)
        }
    }

    fun finalizeBudget() {
        viewModelScope.launch {
            saveBudgetInternal(isSent = true)
        }
    }

    private suspend fun saveBudgetInternal(isSent: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val state = _uiState.value
        val userProfile = authRepository.getUserProfile().firstOrNull()
        
        if (state.selectedClient == null) {
             _uiState.value = _uiState.value.copy(isLoading = false)
             return
        }

        var finalNumber = state.budgetNumber
        if (isSent && finalNumber == 0) {
            finalNumber = budgetNumberManager.getNextNumber()
        }

        val presupuesto = PresupuestoEntity(
            id = presupuestoId,
            titulo = state.budgetTitle,
            clienteId = state.selectedClient.id,
            clienteNombre = state.selectedClient.nombre,
            clienteApellido = "", 
            clienteDireccion = state.selectedClient.direccion,
            clienteTelefono = state.selectedClient.telefono,
            clienteEmail = state.selectedClient.email,
            subtotal = state.subtotal,
            total = state.total,
            descuento = state.discountPercent,
            validez = state.validity.toIntOrNull() ?: 15,
            notas = state.notes,
            estado = if (isSent) "ENVIADO" else "BORRADOR",
            numero = finalNumber,
            creadoEn = System.currentTimeMillis()
        )

        presupuestoDao.upsertPresupuestoWithItems(presupuesto, state.items)
        _uiState.value = _uiState.value.copy(isLoading = false, budgetNumber = finalNumber)
    }

    suspend fun generatePdf(context: Context): File? {
        val state = _uiState.value
        val userProfile = authRepository.getUserProfile().firstOrNull()
        val organization = if (userProfile != null) organizationRepository.getOrganization(userProfile.organizationId).firstOrNull() else null
        
        if (state.selectedClient == null) return null

        val obraMapping = Obra(
            nombreObra = state.budgetTitle,
            clienteNombre = state.selectedClient.nombre,
            descuento = state.discountPercent,
            notas = state.notes,
            validez = state.validity.toIntOrNull() ?: 15
        )

        val domainItems = state.items.map {
            PresupuestoItem(
                id = it.localId.toString(),
                descripcion = it.descripcion,
                cantidad = it.cantidad,
                precioUnitario = it.precioUnitario,
                tipo = it.tipo
            )
        }

        return generatePresupuestoPdfUseCase(obraMapping, domainItems, organization)
    }
}
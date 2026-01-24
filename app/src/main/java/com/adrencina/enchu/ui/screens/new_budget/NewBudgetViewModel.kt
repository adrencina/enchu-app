package com.adrencina.enchu.ui.screens.new_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.local.PresupuestoDao
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

import com.adrencina.enchu.data.model.PresupuestoItem

data class NewBudgetUiState(
    val currentStep: Int = 1,
    val selectedClient: Cliente? = null,
    val budgetTitle: String = "",
    val items: List<PresupuestoItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val subtotal: Double = 0.0,
    val total: Double = 0.0
)

@HiltViewModel
class NewBudgetViewModel @Inject constructor(
    private val presupuestoDao: PresupuestoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewBudgetUiState())
    val uiState: StateFlow<NewBudgetUiState> = _uiState.asStateFlow()

    private val presupuestoId = UUID.randomUUID().toString()

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
        updateTotals(newItems)
    }

    fun removeItem(index: Int) {
        val newItems = _uiState.value.items.toMutableList().apply { removeAt(index) }
        updateTotals(newItems)
    }

    fun updateItem(index: Int, newQuantity: Double, newPrice: Double) {
        val newItems = _uiState.value.items.toMutableList().apply {
            val oldItem = this[index]
            this[index] = oldItem.copy(cantidad = newQuantity, precioUnitario = newPrice)
        }
        updateTotals(newItems)
    }

    private fun updateTotals(items: List<PresupuestoItemEntity>) {
        val subtotal = items.sumOf { it.cantidad * it.precioUnitario }
        _uiState.value = _uiState.value.copy(
            items = items,
            subtotal = subtotal,
            total = subtotal // Por ahora total = subtotal (sin impuestos/descuentos aplicados)
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
            val state = _uiState.value
            val client = state.selectedClient ?: return@launch
            
            val presupuesto = PresupuestoEntity(
                id = presupuestoId,
                titulo = state.budgetTitle,
                clienteId = client.id,
                clienteNombre = client.nombre,
                clienteApellido = "", // Opcional, Cliente solo tiene nombre en tu modelo
                clienteDireccion = client.direccion,
                clienteTelefono = client.telefono,
                clienteEmail = client.email,
                subtotal = state.subtotal,
                total = state.total,
                estado = "PENDIENTE"
            )
            
            presupuestoDao.upsertPresupuestoWithItems(presupuesto, state.items)
        }
    }
}

package com.adrencina.enchu.ui.screens.presupuestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.local.PresupuestoDao
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoWithItems
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PresupuestosUiState(
    val drafts: List<PresupuestoWithItems> = emptyList(),
    val sent: List<PresupuestoWithItems> = emptyList()
)

sealed class PresupuestosEvent {
    data class NavigateToObra(val obraId: String) : PresupuestosEvent()
    data class ShowError(val message: String) : PresupuestosEvent()
}

@HiltViewModel
class PresupuestosViewModel @Inject constructor(
    private val presupuestoDao: PresupuestoDao,
    private val obraRepository: ObraRepository
) : ViewModel() {

    private val _events = Channel<PresupuestosEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<PresupuestosUiState> = presupuestoDao.getAllPresupuestosWithItems()
        .map { list ->
            PresupuestosUiState(
                drafts = list.filter { it.presupuesto.estado != "ENVIADO" && it.presupuesto.estado != "ACEPTADO" },
                sent = list.filter { it.presupuesto.estado == "ENVIADO" }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PresupuestosUiState()
        )

    fun deletePresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch {
            presupuestoDao.deletePresupuesto(presupuesto)
        }
    }

    fun acceptBudget(presupuesto: PresupuestoWithItems) {
        android.util.Log.d("PresupuestosVM", "acceptBudget called for ${presupuesto.presupuesto.id}")
        viewModelScope.launch {
            val result = obraRepository.createObraFromPresupuesto(presupuesto)
            result.onSuccess { obraId ->
                android.util.Log.d("PresupuestosVM", "Obra created successfully: $obraId")
                // Actualizar estado local a ACEPTADO para que salga de la lista de pendientes/enviados
                presupuestoDao.updatePresupuesto(presupuesto.presupuesto.copy(estado = "ACEPTADO"))
                _events.send(PresupuestosEvent.NavigateToObra(obraId))
            }.onFailure { error ->
                android.util.Log.e("PresupuestosVM", "Failed to create Obra", error)
                _events.send(PresupuestosEvent.ShowError(error.message ?: "Error desconocido al aceptar presupuesto"))
            }
        }
    }
}
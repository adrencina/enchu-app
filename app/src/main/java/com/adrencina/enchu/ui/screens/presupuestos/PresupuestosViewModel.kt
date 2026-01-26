package com.adrencina.enchu.ui.screens.presupuestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.local.PresupuestoDao
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoWithItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PresupuestosUiState(
    val drafts: List<PresupuestoWithItems> = emptyList(),
    val sent: List<PresupuestoWithItems> = emptyList()
)

@HiltViewModel
class PresupuestosViewModel @Inject constructor(
    private val presupuestoDao: PresupuestoDao
) : ViewModel() {

    val uiState: StateFlow<PresupuestosUiState> = presupuestoDao.getAllPresupuestosWithItems()
        .map { list ->
            PresupuestosUiState(
                drafts = list.filter { it.presupuesto.estado != "ENVIADO" },
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
}
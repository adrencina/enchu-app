package com.adrencina.enchu.ui.screens.presupuestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.local.PresupuestoDao
import com.adrencina.enchu.data.mapper.toDomain
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoWithItems
import com.adrencina.enchu.domain.repository.ObraRepository
import com.adrencina.enchu.domain.repository.OrganizationRepository
import com.adrencina.enchu.domain.use_case.GeneratePresupuestoPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PresupuestosUiState(
    val drafts: List<PresupuestoWithItems> = emptyList(),
    val sent: List<PresupuestoWithItems> = emptyList()
)

sealed class PresupuestosEvent {
    data class NavigateToObra(val obraId: String) : PresupuestosEvent()
    data class ShowError(val message: String) : PresupuestosEvent()
    data class SharePdf(val file: File) : PresupuestosEvent()
}

@HiltViewModel
class PresupuestosViewModel @Inject constructor(
    private val presupuestoDao: PresupuestoDao,
    private val obraRepository: ObraRepository,
    private val organizationRepository: OrganizationRepository,
    private val generatePresupuestoPdfUseCase: GeneratePresupuestoPdfUseCase
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

    fun acceptBudget(presupuestoWithItems: PresupuestoWithItems) {
        android.util.Log.d("PresupuestosVM", "acceptBudget called for ${presupuestoWithItems.presupuesto.id}")
        viewModelScope.launch {
            // Convertir Data (Room) -> Domain
            val presupuestoDomain = presupuestoWithItems.presupuesto.toDomain(presupuestoWithItems.items)
            
            val result = obraRepository.createObraFromPresupuesto(presupuestoDomain)
            result.onSuccess { obraId ->
                android.util.Log.d("PresupuestosVM", "Obra created successfully: $obraId")
                // Actualizar estado local a ACEPTADO
                presupuestoDao.updatePresupuesto(presupuestoWithItems.presupuesto.copy(estado = "ACEPTADO"))
                _events.send(PresupuestosEvent.NavigateToObra(obraId))
            }.onFailure { error ->
                android.util.Log.e("PresupuestosVM", "Failed to create Obra", error)
                _events.send(PresupuestosEvent.ShowError(error.message ?: "Error desconocido al aceptar presupuesto"))
            }
        }
    }

    fun onExportPdf(presupuestoWithItems: PresupuestoWithItems) {
        viewModelScope.launch {
            try {
                val organization = organizationRepository.getOrganization("").first() // En una app multi-org esto cambiaría
                val presupuestoDomain = presupuestoWithItems.presupuesto.toDomain(presupuestoWithItems.items)
                
                // Mapeo simple de Presupuesto a Obra solo para el PDF (el UseCase pide Obra)
                val dummyObra = com.adrencina.enchu.domain.model.Obra(
                    clienteNombre = "${presupuestoDomain.clienteNombre} ${presupuestoDomain.clienteApellido}",
                    nombreObra = presupuestoDomain.titulo,
                    presupuestoTotal = presupuestoDomain.total,
                    budgetNumber = presupuestoDomain.numero,
                    direccion = presupuestoDomain.clienteDireccion,
                    telefono = presupuestoDomain.clienteTelefono,
                    notas = presupuestoDomain.notas
                )

                val file = generatePresupuestoPdfUseCase(dummyObra, presupuestoDomain.items, organization)
                _events.send(PresupuestosEvent.SharePdf(file))
                
                // Si estaba en borrador, lo pasamos a ENVIADO automáticamente
                if (presupuestoWithItems.presupuesto.estado == "BORRADOR") {
                    presupuestoDao.updatePresupuesto(presupuestoWithItems.presupuesto.copy(estado = "ENVIADO"))
                }
            } catch (e: Exception) {
                _events.send(PresupuestosEvent.ShowError("Error al generar PDF: ${e.message}"))
            }
        }
    }
}
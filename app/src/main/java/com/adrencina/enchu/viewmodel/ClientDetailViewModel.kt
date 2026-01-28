package com.adrencina.enchu.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ClientDetailUiState(
    val cliente: Cliente? = null,
    val obras: List<Obra> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val obraRepository: ObraRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    private var isDeleting = false

    init {
        loadData()
    }

    fun onEditClick() {
        _uiState.update { it.copy(showEditDialog = true) }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(showEditDialog = false) }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete(onSuccess: () -> Unit) {
        isDeleting = true
        onDismissDeleteDialog()
        viewModelScope.launch {
            try {
                clienteRepository.deleteCliente(clientId)
                onSuccess()
            } catch (e: Exception) {
                isDeleting = false
                // Handle error
            }
        }
    }

    fun onConfirmEdit(updatedCliente: Cliente) {
        viewModelScope.launch {
            onDismissEditDialog()
            try {
                clienteRepository.updateCliente(updatedCliente)
                // El flujo de datos actualizará la UI automáticamente
            } catch (e: Exception) {
                // Handle error (e.g. show toast via effect)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val safeClientFlow = clienteRepository.getClientes()
                val obrasFlow = obraRepository.getObras()
                
                combine(safeClientFlow, obrasFlow) { clientes, obras ->
                    val cliente = clientes.find { it.id == clientId }
                    val obrasDelCliente = obras.filter { it.clienteId == clientId }
                    
                    if (cliente != null) {
                        ClientDetailUiState(
                            cliente = cliente,
                            obras = obrasDelCliente,
                            isLoading = false
                        )
                    } else {
                        if (!isDeleting) {
                            ClientDetailUiState(
                                isLoading = false,
                                error = "Cliente no encontrado"
                            )
                        } else {
                            ClientDetailUiState(isLoading = true) // Mantener loading mientras salimos
                        }
                    }
                }.collect { state ->
                    _uiState.value = state
                }

            } catch (e: Exception) {
                if (!isDeleting) {
                    _uiState.value = ClientDetailUiState(isLoading = false, error = e.message)
                }
            }
        }
    }
}

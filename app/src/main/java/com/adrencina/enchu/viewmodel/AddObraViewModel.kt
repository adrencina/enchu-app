package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddObraUiState(
    val nombreObra: String = "",
    val clientes: List<Cliente> = emptyList(),
    val clienteSeleccionado: Cliente? = null,
    val descripcion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val estado: String = "Presupuestado",
    val showDiscardDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null
) {
    val isSaveEnabled: Boolean get() = nombreObra.isNotBlank() && clienteSeleccionado != null
    val hasUnsavedChanges: Boolean get() = nombreObra.isNotEmpty()
            || clienteSeleccionado != null
            || descripcion.isNotEmpty()
            || telefono.isNotEmpty()
            || direccion.isNotEmpty()
}

sealed class AddObraSideEffect {
    data class NavigateBackWithResult(val clientName: String) : AddObraSideEffect()
    object NavigateBack : AddObraSideEffect()
}

@HiltViewModel
class AddObraViewModel @Inject constructor(
    private val obraRepository: ObraRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddObraUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<AddObraSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private var defaultClientCheckDone = false

    init {
        viewModelScope.launch {
            clienteRepository.getClientes().collect { clientesFromRepo ->
                if (clientesFromRepo.isEmpty() && !defaultClientCheckDone) {
                    defaultClientCheckDone = true
                    val defaultClient = Cliente(nombre = "Consumidor Final")
                    clienteRepository.saveCliente(defaultClient)
                } else {
                    val defaultClient = clientesFromRepo.find { it.nombre == "Consumidor Final" }
                    _uiState.update {
                        it.copy(
                            clientes = clientesFromRepo,
                            clienteSeleccionado = it.clienteSeleccionado ?: defaultClient
                        )
                    }
                }
            }
        }
    }

    fun onNombreChange(newValue: String) { _uiState.update { it.copy(nombreObra = newValue) } }
    fun onDescripcionChange(newValue: String) { _uiState.update { it.copy(descripcion = newValue) } }
    fun onTelefonoChange(newValue: String) { _uiState.update { it.copy(telefono = newValue) } }
    fun onDireccionChange(newValue: String) { _uiState.update { it.copy(direccion = newValue) } }
    fun onEstadoChange(newState: String) { _uiState.update { it.copy(estado = newState) } }
    fun onClienteSelected(cliente: Cliente) { _uiState.update { it.copy(clienteSeleccionado = cliente) } }

    fun onSaveClick() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled || currentState.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val newObra = Obra(
                nombreObra = currentState.nombreObra,
                clienteId = currentState.clienteSeleccionado!!.id,
                clienteNombre = currentState.clienteSeleccionado.nombre,
                descripcion = currentState.descripcion,
                telefono = currentState.telefono,
                direccion = currentState.direccion,
                estado = currentState.estado
            )

            val result = obraRepository.saveObra(newObra)

            if (result.isSuccess) {
                _sideEffect.emit(AddObraSideEffect.NavigateBackWithResult(newObra.clienteNombre))
            } else {
                _uiState.update { it.copy(saveError = result.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun onBackPress() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            viewModelScope.launch { _sideEffect.emit(AddObraSideEffect.NavigateBack) }
        }
    }

    fun onDismissDialog() { _uiState.update { it.copy(showDiscardDialog = false) } }

    fun onConfirmDiscard() {
        _uiState.update { it.copy(showDiscardDialog = false) }
        viewModelScope.launch { _sideEffect.emit(AddObraSideEffect.NavigateBack) }
    }
}
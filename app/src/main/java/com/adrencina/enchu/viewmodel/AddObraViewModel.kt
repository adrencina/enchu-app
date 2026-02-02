package com.adrencina.enchu.viewmodel
import com.adrencina.enchu.domain.model.EstadoObra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.domain.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

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
    val saveError: String? = null,

    // State for the Add Client Dialog
    val showAddClientDialog: Boolean = false,
    val newClientNameInput: String = "",
    val newClientDniInput: String = "",
    val newClientPhoneInput: String = "",
    val newClientEmailInput: String = "",
    val newClientAddressInput: String = "",
    val isClientFormExpanded: Boolean = false, // Default collapsed for "Add Obra" flow
    val isAutoDniChecked: Boolean = false,
    val isSavingClient: Boolean = false,
    val saveClientError: String? = null
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
            try {
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
            } catch (e: Exception) {
                Log.e("AddObraViewModel", "Error loading clients", e)
            }
        }
    }

    fun onNombreChange(newValue: String) { _uiState.update { it.copy(nombreObra = newValue.take(50)) } }
    fun onDescripcionChange(newValue: String) { _uiState.update { it.copy(descripcion = newValue.take(200)) } }
    fun onTelefonoChange(newValue: String) { _uiState.update { it.copy(telefono = newValue.take(20)) } }
    fun onDireccionChange(newValue: String) { _uiState.update { it.copy(direccion = newValue.take(100)) } }
    fun onEstadoChange(newState: String) { _uiState.update { it.copy(estado = newState) } }
    fun onClienteSelected(cliente: Cliente) { _uiState.update { it.copy(clienteSeleccionado = cliente) } }

    fun onSaveClick() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled || currentState.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val newObra = Obra(
                nombreObra = currentState.nombreObra,
                clienteId = currentState.clienteSeleccionado!!.id,
                clienteNombre = currentState.clienteSeleccionado.nombre,
                descripcion = currentState.descripcion,
                telefono = currentState.telefono,
                direccion = currentState.direccion,
                estado = EstadoObra.fromValue(currentState.estado)
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

    //region Add Client Dialog
    fun onAddClientClick() {
        _uiState.update { it.copy(showAddClientDialog = true, saveClientError = null) }
    }

    fun onDismissAddClientDialog() {
        _uiState.update { it.copy(
            showAddClientDialog = false,
            newClientNameInput = "",
            newClientDniInput = "",
            newClientPhoneInput = "",
            newClientEmailInput = "",
            newClientAddressInput = "",
            isClientFormExpanded = false,
            isAutoDniChecked = false
        ) }
    }

    fun onNewClientNameChange(name: String) { _uiState.update { it.copy(newClientNameInput = name.take(50)) } }
    fun onNewClientDniChange(dni: String) { _uiState.update { it.copy(newClientDniInput = dni.take(15)) } }
    fun onNewClientPhoneChange(phone: String) { _uiState.update { it.copy(newClientPhoneInput = phone.take(20)) } }
    fun onNewClientEmailChange(email: String) { _uiState.update { it.copy(newClientEmailInput = email.take(50)) } }
    fun onNewClientAddressChange(address: String) { _uiState.update { it.copy(newClientAddressInput = address.take(100)) } }
    fun onToggleClientFormExpand() { _uiState.update { it.copy(isClientFormExpanded = !it.isClientFormExpanded) } }

    fun onAutoDniCheckedChange(isChecked: Boolean) {
        _uiState.update {
            it.copy(
                isAutoDniChecked = isChecked,
                newClientDniInput = if (isChecked) "" else it.newClientDniInput
            )
        }
    }

    private fun generateProvisionalDni(): String {
        return "AUTO-${UUID.randomUUID().toString().take(8).uppercase()}"
    }

    fun onSaveNewClient() {
        val state = _uiState.value
        val newClientName = state.newClientNameInput
        var newClientDni = state.newClientDniInput

        if (newClientName.isBlank() || (newClientDni.isBlank() && !state.isAutoDniChecked) || state.isSavingClient) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingClient = true, saveClientError = null) }

            if (state.isAutoDniChecked) {
                newClientDni = generateProvisionalDni()
            } else {
                // Check if DNI exists only if NOT auto
                if (clienteRepository.doesDniExist(newClientDni)) {
                     _uiState.update {
                        it.copy(
                            isSavingClient = false,
                            saveClientError = "El DNI ingresado ya existe."
                        )
                    }
                    return@launch
                }
            }

            val newClient = Cliente(
                nombre = newClientName,
                dni = newClientDni,
                telefono = state.newClientPhoneInput,
                email = state.newClientEmailInput,
                direccion = state.newClientAddressInput
            )

            val result = clienteRepository.saveCliente(newClient)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSavingClient = false,
                        showAddClientDialog = false,
                        newClientNameInput = "",
                        newClientDniInput = "",
                        newClientPhoneInput = "",
                        newClientEmailInput = "",
                        newClientAddressInput = "",
                        isClientFormExpanded = false,
                        isAutoDniChecked = false
                    )
                }
                // Select the newly created client?
                onClienteSelected(newClient)
            } else {
                _uiState.update {
                    it.copy(
                        isSavingClient = false,
                        saveClientError = result.exceptionOrNull()?.message ?: "Error desconocido"
                    )
                }
            }
        }
    }
    //endregion
}

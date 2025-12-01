package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddClientUiState(
    val name: String = "",
    val dni: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val isAutoDni: Boolean = false,
    val isExpanded: Boolean = true, // Default expanded for standalone screen
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AddClientSideEffect {
    object ClientSaved : AddClientSideEffect()
    data class ShowError(val message: String) : AddClientSideEffect()
}

@HiltViewModel
class AddClientViewModel @Inject constructor(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClientUiState())
    val uiState: StateFlow<AddClientUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<AddClientSideEffect>()
    val sideEffect: SharedFlow<AddClientSideEffect> = _sideEffect.asSharedFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onDniChange(value: String) {
        _uiState.update { it.copy(dni = value) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onAutoDniChange(value: Boolean) {
        _uiState.update { it.copy(isAutoDni = value) }
    }

    fun onToggleExpand() {
        _uiState.update { it.copy(isExpanded = !it.isExpanded) }
    }

    fun saveClient() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            viewModelScope.launch { _sideEffect.emit(AddClientSideEffect.ShowError("El nombre es obligatorio")) }
            return
        }
        if (!state.isAutoDni && state.dni.isBlank()) {
            viewModelScope.launch { _sideEffect.emit(AddClientSideEffect.ShowError("El DNI es obligatorio o marque 'Automático'")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val finalDni = if (state.isAutoDni) generateAutoDni() else state.dni
                val newClient = Cliente(
                    nombre = state.name,
                    dni = finalDni,
                    telefono = state.phone,
                    email = state.email,
                    direccion = state.address
                )
                repository.saveCliente(newClient)
                _sideEffect.emit(AddClientSideEffect.ClientSaved)
            } catch (e: Exception) {
                _sideEffect.emit(AddClientSideEffect.ShowError("Error al guardar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun generateAutoDni(): String {
        return "AUTO-${UUID.randomUUID().toString().take(8).uppercase()}"
    }
}

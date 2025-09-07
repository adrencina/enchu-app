package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.core.resources.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddObraUiState(
    val nombreObra: String = "",
    val cliente: String = "",
    val descripcion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val estado: String = AppStrings.stateBudgeted,
    val showDiscardDialog: Boolean = false
) {
    val isSaveEnabled: Boolean get() = nombreObra.isNotBlank() && cliente.isNotBlank()
    val hasUnsavedChanges: Boolean get() = nombreObra.isNotEmpty() || cliente.isNotEmpty() || descripcion.isNotEmpty() || telefono.isNotEmpty() || direccion.isNotEmpty()
}

sealed class AddObraSideEffect {
    data class NavigateBackWithResult(val clientName: String) : AddObraSideEffect()
    object NavigateBack : AddObraSideEffect()
}

@HiltViewModel
class AddObraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddObraUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<AddObraSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun onNombreChange(newValue: String) {
        _uiState.update { it.copy(nombreObra = newValue) }
    }
    fun onClienteChange(newValue: String) {
        _uiState.update { it.copy(cliente = newValue) }
    }
    fun onDescripcionChange(newValue: String) {
        _uiState.update { it.copy(descripcion = newValue) }
    }
    fun onTelefonoChange(newValue: String) {
        _uiState.update { it.copy(telefono = newValue) }
    }
    fun onDireccionChange(newValue: String) {
        _uiState.update { it.copy(direccion = newValue) }
    }
    fun onEstadoChange(newState: String) {
        _uiState.update { it.copy(estado = newState) }
    }

    fun onSaveClick() {
        if (!_uiState.value.isSaveEnabled) return
        // TODO: Lógica para guardar la obra en el repositorio
        viewModelScope.launch {
            _sideEffect.emit(AddObraSideEffect.NavigateBackWithResult(_uiState.value.cliente))
        }
    }

    fun onBackPress() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            viewModelScope.launch {
                _sideEffect.emit(AddObraSideEffect.NavigateBack)
            }
        }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun onConfirmDiscard() {
        _uiState.update { it.copy(showDiscardDialog = false) }
        viewModelScope.launch {
            _sideEffect.emit(AddObraSideEffect.NavigateBack)
        }
    }
}
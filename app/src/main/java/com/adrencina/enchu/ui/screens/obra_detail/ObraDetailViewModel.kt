package com.adrencina.enchu.ui.screens.obra_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Usamos una clase sellada para seguir el patrón de HomeUiState
sealed class ObraDetailUiState {
    object Loading : ObraDetailUiState()
    data class Success(
        val obra: Obra,
        val selectedTabIndex: Int = 0
    ) : ObraDetailUiState()
    data class Error(val message: String) : ObraDetailUiState()
}

sealed class ObraDetailEffect {
    object NavigateBack : ObraDetailEffect()
}

@HiltViewModel
class ObraDetailViewModel @Inject constructor(
    private val repository: ObraRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId")!!

    private val _uiState = MutableStateFlow<ObraDetailUiState>(ObraDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ObraDetailEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadObraDetails()
    }

    // --- Manejo de eventos con funciones públicas, como en HomeViewModel ---

    fun onTabSelected(index: Int) {
        _uiState.update { currentState ->
            if (currentState is ObraDetailUiState.Success) {
                currentState.copy(selectedTabIndex = index)
            } else {
                currentState // No hacer nada si no estamos en estado Success
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch { _effect.emit(ObraDetailEffect.NavigateBack) }
    }

    fun onMenuPressed() {
        // TODO: Implementar lógica del menú (ej: mostrar dropdown)
    }

    fun onFabPressed() {
        // TODO: Implementar lógica del FAB según la pestaña seleccionada
    }

    private fun loadObraDetails() {
        viewModelScope.launch {
            repository.getObraById(obraId)
                .catch { exception ->
                    _uiState.value = ObraDetailUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obra ->
                    _uiState.value = ObraDetailUiState.Success(obra)
                }
        }
    }
}

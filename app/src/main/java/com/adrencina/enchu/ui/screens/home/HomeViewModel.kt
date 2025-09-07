package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// CORRECTO: Sealed class para los ESTADOS de la UI (lo que se ve permanentemente)
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val obras: List<Obra>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// CORRECTO: Sealed class SEPARADA para los EFECTOS (eventos de un solo uso)
sealed class HomeUiEffect {
    data class ShowObraCreatedSnackbar(val clientName: String) : HomeUiEffect()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obraRepository: ObraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadObras()
    }

    fun onNewObraCreated(clientName: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.ShowObraCreatedSnackbar(clientName))
        }
    }

    private fun loadObras() {
        // CORRECTO: La llamada a la base de datos debe estar dentro de un launch
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            obraRepository.getObras()
                .catch { exception ->
                    _uiState.value = HomeUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obras ->
                    _uiState.value = HomeUiState.Success(obras)
                }
        }
    }
}
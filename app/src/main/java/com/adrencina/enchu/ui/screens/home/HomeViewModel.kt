package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val obras: List<Obra>, val archivedCount: Int = 0) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

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
        viewModelScope.launch {
            combine(
                obraRepository.getObras(),
                obraRepository.getArchivedObras()
            ) { activeObras, archivedObras ->
                HomeUiState.Success(activeObras, archivedObras.size)
            }
            .catch { exception ->
                _uiState.value = HomeUiState.Error(exception.message ?: "Error desconocido")
            }
            .collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
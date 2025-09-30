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
    data class Success(val obras: List<Obra>) : HomeUiState()
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
        // La única llamada a la carga de datos se hace aquí.
        // El .collect se mantendrá escuchando cambios mientras el ViewModel viva.
        loadObras()
    }

    fun onNewObraCreated(clientName: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.ShowObraCreatedSnackbar(clientName))
        }
    }

    // CAMBIO: La función vuelve a ser privada. La UI ya no necesita llamarla.
    private fun loadObras() {
        viewModelScope.launch {
            obraRepository.getObras()
                .catch { exception ->
                    _uiState.value = HomeUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obras ->
                    // Cada vez que haya un cambio en Firestore, esta línea se ejecutará
                    // y actualizará la UI con la nueva lista de obras.
                    _uiState.value = HomeUiState.Success(obras)
                }
        }
    }
}
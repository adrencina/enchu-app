package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definimos los posibles estados de nuestra UI
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val obras: List<Obra>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obraRepository: ObraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // Apenas se crea el ViewModel, salimos a buscar las obras.
        loadObras()
    }

    private fun loadObras() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            obraRepository.getObras()
                .catch { exception ->
                    // Si algo falla al conectar con Firebase, emitimos un estado de error.
                    _uiState.value = HomeUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obras ->
                    // Cuando Firebase nos devuelve la lista (o una lista actualizada),
                    // emitimos el estado de éxito con los datos.
                    _uiState.value = HomeUiState.Success(obras)
                }
        }
    }
}
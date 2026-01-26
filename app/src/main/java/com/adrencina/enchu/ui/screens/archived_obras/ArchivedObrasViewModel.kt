package com.adrencina.enchu.ui.screens.archived_obras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.ObraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ArchivedObrasUiState {
    object Loading : ArchivedObrasUiState()
    data class Success(val obras: List<Obra>) : ArchivedObrasUiState()
    data class Error(val message: String) : ArchivedObrasUiState()
}

@HiltViewModel
class ArchivedObrasViewModel @Inject constructor(
    private val repository: ObraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArchivedObrasUiState>(ArchivedObrasUiState.Loading)
    val uiState: StateFlow<ArchivedObrasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getArchivedObras()
                .catch { exception ->
                    _uiState.value = ArchivedObrasUiState.Error(exception.message ?: "Error desconocido")
                }
                .collect { obras ->
                    _uiState.value = ArchivedObrasUiState.Success(obras)
                }
        }
    }

    fun deleteObra(obraId: String) {
        viewModelScope.launch {
            repository.deleteObra(obraId)
        }
    }
}

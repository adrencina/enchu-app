package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.data.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val userName: String = "",
        val activeObras: List<Obra>, 
        val archivedObras: List<Obra>,
        val plan: String = "FREE"
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class HomeUiEffect {
    data class ShowObraCreatedSnackbar(val clientName: String) : HomeUiEffect()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obraRepository: ObraRepository,
    private val authRepository: AuthRepository,
    private val organizationRepository: OrganizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadObras()
    }

    fun onNewObraCreated(clientName: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.ShowObraCreatedSnackbar(clientName))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadObras() {
        viewModelScope.launch {
            authRepository.getUserProfile()
                .flatMapLatest { userProfile ->
                    val userName = userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Usuario"
                    val orgId = userProfile?.organizationId ?: ""
                    
                    organizationRepository.getOrganization(orgId).map { org ->
                        userName to org
                    }
                }
                .flatMapLatest { (userName, organization) ->
                    val plan = organization?.plan ?: "FREE"
                    
                    combine(
                        obraRepository.getObras(),
                        obraRepository.getArchivedObras(),
                        _searchQuery
                    ) { activeObras: List<Obra>, archivedList: List<Obra>, query: String ->
                        if (query.isBlank()) {
                            HomeUiState.Success(
                                userName = userName,
                                activeObras = activeObras,
                                archivedObras = archivedList,
                                plan = plan
                            )
                        } else {
                            val allObras = activeObras + archivedList
                            val filteredObras = allObras.filter { obra ->
                                obra.nombreObra.contains(query, ignoreCase = true) ||
                                obra.clienteNombre.contains(query, ignoreCase = true) ||
                                (obra.estado ?: "").contains(query, ignoreCase = true) ||
                                (obra.direccion ?: "").contains(query, ignoreCase = true)
                            }
                            HomeUiState.Success(
                                userName = userName,
                                activeObras = filteredObras,
                                archivedObras = emptyList(), 
                                plan = plan
                            )
                        }
                    }
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
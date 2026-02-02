package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.domain.common.Resource
import com.adrencina.enchu.domain.model.Movimiento
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.domain.repository.ObraRepository
import com.adrencina.enchu.domain.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val activeObras: List<Obra> = emptyList(),
    val archivedObras: List<Obra> = emptyList(),
    val plan: String = "FREE",
    val totalCobrado: Double = 0.0,
    val totalGastado: Double = 0.0,
    val saldoTotal: Double = 0.0,
    val totalPendiente: Double = 0.0,
    val userMessage: String? = null
)

sealed class HomeUiEffect {
    data class ShowObraCreatedSnackbar(val clientName: String) : HomeUiEffect()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obraRepository: ObraRepository,
    private val authRepository: AuthRepository,
    private val organizationRepository: OrganizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
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
                    ) { activeRes, archivedRes, query ->
                        Triple(activeRes, archivedRes, query)
                    }.flatMapLatest { (activeRes, archivedRes, query) ->
                        
                        val isLoading = activeRes is Resource.Loading || archivedRes is Resource.Loading
                        val errorMsg = activeRes.message ?: archivedRes.message
                        
                        val activeObras = activeRes.data ?: emptyList()
                        val archivedObras = archivedRes.data ?: emptyList()

                        if (activeObras.isEmpty()) {
                            flowOf(
                                HomeUiState(
                                    isLoading = isLoading,
                                    userName = userName,
                                    activeObras = emptyList(),
                                    archivedObras = archivedObras,
                                    plan = plan,
                                    userMessage = errorMsg
                                )
                            )
                        } else {
                            val flowsMovimientos = activeObras.map { obra ->
                                obraRepository.getMovimientos(obra.id).map { movimientos ->
                                    obra.id to movimientos
                                }
                            }

                            combine(flowsMovimientos) { arrayMovimientos ->
                                val allMovimientos = arrayMovimientos.flatMap { it.second }
                                
                                val totalIngresos = allMovimientos.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
                                val totalEgresos = allMovimientos.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
                                val saldo = totalIngresos - totalEgresos
                                
                                val totalPresupuestado = activeObras.sumOf { it.presupuestoTotal }
                                val totalCobradoClientes = allMovimientos
                                    .filter { it.tipo == "INGRESO" && (it.categoria == "PAGO_CLIENTE" || it.categoria == "ADELANTO" || it.categoria == "OTRO") }
                                    .sumOf { it.monto }
                                    
                                val pendiente = (totalPresupuestado - totalCobradoClientes).coerceAtLeast(0.0)

                                val filteredActive = if (query.isBlank()) activeObras else activeObras.filter { 
                                    it.nombreObra.contains(query, true) || it.clienteNombre.contains(query, true)
                                }
                                val filteredArchived = if (query.isBlank()) archivedObras else archivedObras.filter {
                                    it.nombreObra.contains(query, true)
                                }

                                HomeUiState(
                                    isLoading = isLoading,
                                    userName = userName,
                                    activeObras = filteredActive,
                                    archivedObras = filteredArchived,
                                    plan = plan,
                                    totalCobrado = totalIngresos,
                                    totalGastado = totalEgresos,
                                    saldoTotal = saldo,
                                    totalPendiente = pendiente,
                                    userMessage = errorMsg
                                )
                            }
                        }
                    }
                }
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, userMessage = exception.message) }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
}

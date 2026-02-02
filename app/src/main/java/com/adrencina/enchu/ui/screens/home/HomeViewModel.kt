package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val userName: String = "",
        val activeObras: List<Obra>, 
        val archivedObras: List<Obra>,
        val plan: String = "FREE",
        val totalCobrado: Double = 0.0,
        val totalGastado: Double = 0.0,
        val saldoTotal: Double = 0.0,
        val totalPendiente: Double = 0.0
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
                    
                    // Combinamos Obras Activas y Archivadas
                    combine(
                        obraRepository.getObras(),
                        obraRepository.getArchivedObras(),
                        _searchQuery
                    ) { activeObras, archivedObras, query ->
                        Triple(activeObras, archivedObras, query)
                    }.flatMapLatest { (activeObras, archivedObras, query) ->
                        
                        // Si no hay obras activas, emitimos estado con ceros
                        if (activeObras.isEmpty()) {
                            flowOf(
                                HomeUiState.Success(
                                    userName = userName,
                                    activeObras = emptyList(),
                                    archivedObras = archivedObras,
                                    plan = plan
                                )
                            )
                        } else {
                            // Si hay obras, obtenemos los movimientos de CADA una para calcular totales
                            val flowsMovimientos = activeObras.map { obra ->
                                obraRepository.getMovimientos(obra.id).map { movimientos ->
                                    obra.id to movimientos
                                }
                            }

                            // Combinamos los flujos de movimientos
                            combine(flowsMovimientos) { arrayMovimientos ->
                                // arrayMovimientos es Array<Pair<String, List<Movimiento>>>
                                val allMovimientos = arrayMovimientos.flatMap { it.second }
                                
                                val totalIngresos = allMovimientos.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
                                val totalEgresos = allMovimientos.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
                                val saldo = totalIngresos - totalEgresos
                                
                                // Calculamos lo pendiente (Presupuestado - Cobrado)
                                // Nota: Esto es aproximado. Asume que todo ingreso cuenta como pago del presupuesto.
                                // Idealmente se filtraría por categoría "PAGO_CLIENTE".
                                val totalPresupuestado = activeObras.sumOf { it.presupuestoTotal }
                                val totalCobradoClientes = allMovimientos
                                    .filter { it.tipo == "INGRESO" && (it.categoria == "PAGO_CLIENTE" || it.categoria == "ADELANTO" || it.categoria == "OTRO") } // Asumimos general por ahora
                                    .sumOf { it.monto }
                                    
                                val pendiente = (totalPresupuestado - totalCobradoClientes).coerceAtLeast(0.0)

                                // Filtrado por búsqueda (si aplica)
                                val filteredActive = if (query.isBlank()) activeObras else activeObras.filter { 
                                    it.nombreObra.contains(query, true) || it.clienteNombre.contains(query, true)
                                }
                                val filteredArchived = if (query.isBlank()) archivedObras else archivedObras.filter {
                                    it.nombreObra.contains(query, true)
                                }

                                HomeUiState.Success(
                                    userName = userName,
                                    activeObras = filteredActive,
                                    archivedObras = filteredArchived,
                                    plan = plan,
                                    totalCobrado = totalIngresos,
                                    totalGastado = totalEgresos,
                                    saldoTotal = saldo,
                                    totalPendiente = pendiente
                                )
                            }
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
package com.adrencina.enchu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.domain.common.Resource
import com.adrencina.enchu.domain.model.Movimiento
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.domain.repository.BillingRepository
import com.adrencina.enchu.domain.repository.ObraRepository
import com.adrencina.enchu.domain.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.adrencina.enchu.domain.model.UserRole

data class HomeUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val userName: String = "",
    val userRole: UserRole = UserRole.OWNER, // Default to OWNER for now
    val activeObras: List<Obra> = emptyList(),
    val obraActiva: Obra? = null,
    val recientes: List<Obra> = emptyList(),
    val archivedObras: List<Obra> = emptyList(),
    val plan: String = "FREE",
    val totalCobrado: Double = 0.0,
    val totalGastado: Double = 0.0,
    val saldoTotal: Double = 0.0,
    val totalPendiente: Double = 0.0,
    val userMessage: String? = null
)

private data class DataBundle(
    val activeRes: Resource<List<Obra>>,
    val archivedRes: Resource<List<Obra>>,
    val query: String,
    val plan: String,
    val role: UserRole
)

sealed class HomeUiEffect {
    data class ShowObraCreatedSnackbar(val clientName: String) : HomeUiEffect()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obraRepository: ObraRepository,
    private val authRepository: AuthRepository,
    private val organizationRepository: OrganizationRepository,
    private val billingRepository: BillingRepository
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

    fun launchBilling(activity: android.app.Activity) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: ""
            billingRepository.launchBillingFlow(activity, userId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadObras() {
        viewModelScope.launch {
            authRepository.getUserProfile()
                .flatMapLatest { userProfile ->
                    val userName = userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Usuario"
                    val orgId = userProfile?.organizationId ?: ""
                    val role = UserRole.fromValue(userProfile?.role ?: "OWNER")
                    
                    organizationRepository.getOrganization(orgId).map { org ->
                        Triple(userName, org, role)
                    }
                }
                .flatMapLatest { (userName, organization, role) ->
                    combine(
                        obraRepository.getObras(),
                        obraRepository.getArchivedObras(),
                        _searchQuery,
                        billingRepository.isPro
                    ) { activeRes, archivedRes, query, isPro ->
                        val plan = if (isPro) "PRO" else (organization?.plan ?: "FREE")
                        DataBundle(activeRes, archivedRes, query, plan, role)
                    }.flatMapLatest { bundle ->
                        val activeRes = bundle.activeRes
                        val archivedRes = bundle.archivedRes
                        val query = bundle.query
                        val plan = bundle.plan
                        val currentRole = bundle.role
                        
                        val isLoading = activeRes is Resource.Loading || archivedRes is Resource.Loading
                        val isError = activeRes is Resource.Error || archivedRes is Resource.Error
                        val errorMsg = activeRes.message ?: archivedRes.message
                        
                        val activeObras = activeRes.data ?: emptyList()
                        val archivedObras = archivedRes.data ?: emptyList()

                        if (activeObras.isEmpty()) {
                            flowOf(
                                HomeUiState(
                                    isLoading = isLoading,
                                    isError = isError,
                                    userName = userName,
                                    userRole = currentRole,
                                    activeObras = emptyList(),
                                    obraActiva = null,
                                    recientes = emptyList(),
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
                                
                                // Calcular totales solo si tiene permiso (sino 0.0)
                                val canViewMoney = currentRole.canViewMoney()
                                val totalIngresos = if (canViewMoney) allMovimientos.filter { it.tipo == "INGRESO" }.sumOf { it.monto } else 0.0
                                val totalEgresos = if (canViewMoney) allMovimientos.filter { it.tipo == "EGRESO" }.sumOf { it.monto } else 0.0
                                val saldo = totalIngresos - totalEgresos
                                
                                val totalPresupuestado = if (canViewMoney) activeObras.sumOf { it.presupuestoTotal } else 0.0
                                val totalCobradoClientes = if (canViewMoney) allMovimientos
                                    .filter { it.tipo == "INGRESO" && (it.categoria == "PAGO_CLIENTE" || it.categoria == "ADELANTO" || it.categoria == "OTRO") }
                                    .sumOf { it.monto } else 0.0
                                    
                                val pendiente = (totalPresupuestado - totalCobradoClientes).coerceAtLeast(0.0)

                                val filteredActive = if (query.isBlank()) activeObras else activeObras.filter { 
                                    it.nombreObra.contains(query, true) || it.clienteNombre.contains(query, true)
                                }
                                
                                // Ordenar por lastActivity si existe, sino por fechaCreacion
                                val sortedActive = filteredActive.sortedWith(
                                    compareByDescending<Obra> { it.lastActivity }.thenByDescending { it.fechaCreacion }
                                )
                                val obraActiva = sortedActive.firstOrNull()
                                val recientes = if (obraActiva != null) sortedActive.drop(1) else emptyList()

                                val filteredArchived = if (query.isBlank()) archivedObras else archivedObras.filter {
                                    it.nombreObra.contains(query, true)
                                }

                                HomeUiState(
                                    isLoading = isLoading,
                                    isError = isError,
                                    userName = userName,
                                    userRole = currentRole,
                                    activeObras = filteredActive,
                                    obraActiva = obraActiva,
                                    recientes = recientes,
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

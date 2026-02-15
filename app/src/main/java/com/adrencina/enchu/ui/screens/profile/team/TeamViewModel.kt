package com.adrencina.enchu.ui.screens.profile.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.repository.ObraRepository
import com.adrencina.enchu.domain.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberStats(
    val userId: String,
    val activeWorksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val assignedWorks: List<Obra> = emptyList()
)

data class TeamUiState(
    val isLoading: Boolean = false,
    val activeMembers: List<UserProfile> = emptyList(),
    val pendingMembers: List<UserProfile> = emptyList(),
    val stats: Map<String, MemberStats> = emptyMap(),
    val organizationId: String = "",
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val organizationRepository: OrganizationRepository,
    private val obraRepository: ObraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTeamData()
    }

    private fun loadTeamData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.getUserProfile().collect { profile ->
                val orgId = profile?.organizationId ?: ""
                _uiState.update { it.copy(organizationId = orgId) }
                
                if (orgId.isNotEmpty()) {
                    // Combinamos miembros y obras para calcular estadísticas
                    combine(
                        organizationRepository.getMembers(orgId),
                        obraRepository.getObras() // Nota: getObras ya filtra por orgId en el repo
                    ) { members, obrasRes ->
                        val obras = obrasRes.data ?: emptyList()
                        
                        val active = members.filter { it.status == "ACTIVE" }
                        val pending = members.filter { it.status == "PENDING" }
                        
                        // Calcular estadísticas básicas
                        val statsMap = active.associate { member ->
                            val memberWorks = obras.filter { it.assignedMemberIds.contains(member.id) }
                            member.id to MemberStats(
                                userId = member.id,
                                activeWorksCount = memberWorks.size,
                                // Por ahora sumamos las tareas completadas totales de la obra como aproximación
                                // En el futuro podriamos traer las subcolecciones de tareas para precision total
                                completedTasksCount = memberWorks.sumOf { it.tareasCompletadas },
                                assignedWorks = memberWorks
                            )
                        }

                        _uiState.update { it.copy(
                            isLoading = false,
                            activeMembers = active,
                            pendingMembers = pending,
                            stats = statsMap,
                            error = null
                        ) }
                    }.collect()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No perteneces a ninguna organización") }
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun approveMember(userId: String) {
        viewModelScope.launch { organizationRepository.approveMember(userId) }
    }

    fun rejectMember(userId: String) {
        viewModelScope.launch { organizationRepository.removeMember(userId) }
    }

    fun updateRole(userId: String, newRole: String) {
        viewModelScope.launch { organizationRepository.updateMemberRole(userId, newRole) }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch { organizationRepository.removeMember(userId) }
    }
}

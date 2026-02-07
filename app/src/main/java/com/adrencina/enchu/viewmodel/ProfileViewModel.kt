package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.domain.common.Resource
import com.adrencina.enchu.domain.repository.ObraRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.net.Uri
import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.domain.repository.OrganizationRepository

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val organization: Organization? = null,
    val organizationMembers: List<UserProfile> = emptyList(),
    val obrasCount: Int = 0,
    val clientesCount: Int = 0,
    val isLoading: Boolean = false,
    val showEditOrgDialog: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obraRepository: ObraRepository,
    private val clienteRepository: ClienteRepository,
    private val organizationRepository: OrganizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadStats()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                authRepository.getUserProfile().collectLatest { profile ->
                    _uiState.update { it.copy(user = authRepository.currentUser, userProfile = profile) }
                    profile?.organizationId?.let { orgId ->
                        loadOrganization(orgId)
                    }
                }
            } catch (e: Exception) {
                // Error silencioso (común al cerrar sesión)
            }
        }
    }

    private fun loadOrganization(orgId: String) {
        viewModelScope.launch {
            try {
                organizationRepository.getOrganization(orgId).collectLatest { org ->
                    _uiState.update { it.copy(organization = org) }
                    org?.members?.let { memberIds ->
                        if (memberIds.isNotEmpty()) {
                            val memberProfiles = mutableListOf<UserProfile>()
                            for (memberId in memberIds) {
                                val memberProfile = authRepository.getUserProfileById(memberId)
                                memberProfile?.let { memberProfiles.add(it) }
                            }
                            _uiState.update { it.copy(organizationMembers = memberProfiles) }
                        } else {
                            _uiState.update { it.copy(organizationMembers = emptyList()) }
                        }
                    }
                }
            } catch (e: Exception) {
                // Error silencioso
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            launch {
                try {
                    obraRepository.getObras().collectLatest { resource ->
                        val count = resource.data?.size ?: 0
                        _uiState.update { it.copy(obrasCount = count) }
                    }
                } catch (e: Exception) {
                    // Error silencioso
                }
            }
            launch {
                try {
                    clienteRepository.getClientes().collectLatest { clientes ->
                        _uiState.update { it.copy(clientesCount = clientes.size) }
                    }
                } catch (e: Exception) {
                    // Error silencioso
                }
            }
        }
    }

    fun onEditOrgClick() {
        _uiState.update { it.copy(showEditOrgDialog = true) }
    }

    fun onDismissEditOrgDialog() {
        _uiState.update { it.copy(showEditOrgDialog = false) }
    }

    fun onUpdateOrganization(name: String, phone: String, email: String, address: String, web: String, cuit: String, taxCondition: String) {
        viewModelScope.launch {
            val currentOrg = _uiState.value.organization ?: return@launch
            val updatedOrg = currentOrg.copy(
                name = name,
                businessPhone = phone,
                businessEmail = email,
                businessAddress = address,
                businessWeb = web,
                cuit = cuit,
                taxCondition = taxCondition
            )
            organizationRepository.updateOrganization(updatedOrg)
            onDismissEditOrgDialog()
        }
    }

    fun onLogoSelected(uri: Uri) {
        viewModelScope.launch {
            val orgId = _uiState.value.organization?.id ?: return@launch
            organizationRepository.uploadLogo(orgId, uri)
        }
    }

    fun onUpdateUserRole(role: String) {
        viewModelScope.launch {
            authRepository.updateUserRole(role)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
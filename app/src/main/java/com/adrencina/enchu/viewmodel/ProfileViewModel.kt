package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.data.repository.ObraRepository
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
import com.adrencina.enchu.data.model.Organization
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.data.repository.OrganizationRepository

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val organization: Organization? = null,
    val organizationMembers: List<UserProfile> = emptyList(), // New list of member profiles
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
            authRepository.getUserProfile().collectLatest { profile ->
                _uiState.update { it.copy(user = authRepository.currentUser, userProfile = profile) }
                profile?.organizationId?.let { orgId ->
                    loadOrganization(orgId)
                }
            }
        }
    }

    private fun loadOrganization(orgId: String) {
        viewModelScope.launch {
            organizationRepository.getOrganization(orgId).collectLatest { org ->
                _uiState.update { it.copy(organization = org) }
                org?.members?.let { memberIds ->
                    // Fetch user profiles for each member
                    if (memberIds.isNotEmpty()) {
                        val memberProfiles = mutableListOf<UserProfile>()
                        for (memberId in memberIds) {
                            // This is a simplified way. In a real app, you might use a batch read
                            // or a dedicated repository method to get multiple profiles at once.
                            val memberProfile = authRepository.getUserProfile().first { it?.id == memberId }
                            memberProfile?.let { memberProfiles.add(it) }
                        }
                        _uiState.update { it.copy(organizationMembers = memberProfiles) }
                    } else {
                        _uiState.update { it.copy(organizationMembers = emptyList()) }
                    }
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

    fun onUpdateOrganization(name: String, phone: String, email: String, address: String, web: String) {
        viewModelScope.launch {
            val currentOrg = _uiState.value.organization ?: return@launch
            val updatedOrg = currentOrg.copy(
                name = name,
                businessPhone = phone,
                businessEmail = email,
                businessAddress = address,
                businessWeb = web
            )
            organizationRepository.updateOrganization(updatedOrg)
            onDismissEditOrgDialog()
        }
    }

    fun onLogoSelected(uri: Uri) {
        viewModelScope.launch {
            val org = _uiState.value.organization ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            val result = organizationRepository.uploadLogo(org.id, uri)
            if (result.isSuccess) {
                val logoUrl = result.getOrNull() ?: ""
                val updatedOrg = org.copy(logoUrl = logoUrl)
                organizationRepository.updateOrganization(updatedOrg)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            // Collect stats in parallel or sequentially
            launch {
                obraRepository.getObras().collectLatest { obras ->
                    _uiState.update { it.copy(obrasCount = obras.size) }
                }
            }
            launch {
                clienteRepository.getClientes().collectLatest { clientes ->
                    _uiState.update { it.copy(clientesCount = clientes.size) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

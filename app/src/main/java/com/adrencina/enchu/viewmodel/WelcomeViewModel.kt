package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val inviteCode: String = "",
    val isProfileCreated: Boolean = false,
    val userProfile: UserProfile? = null
)

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
                // Si ya está activo, navegar a home
                if (profile?.status == "ACTIVE" && profile.organizationId.isNotEmpty()) {
                    _uiState.update { it.copy(isProfileCreated = true) }
                }
            }
        }
    }

    fun onInviteCodeChanged(code: String) {
        _uiState.update { it.copy(inviteCode = code) }
    }

    fun onCreateIndependentProfile() {
        val user = authRepository.currentUser ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = authRepository.createIndependentProfile(user)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isProfileCreated = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al crear perfil.") }
            }
        }
    }

    fun onJoinOrganization() {
        val user = authRepository.currentUser ?: return
        val code = _uiState.value.inviteCode.trim()
        
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa un código válido.") }
            return
        }

        val lastRejection = _uiState.value.userProfile?.lastRejectionTimestamp ?: 0L
        val waitTime = 5 * 60 * 1000 // 5 minutos
        val remainingTime = (lastRejection + waitTime) - System.currentTimeMillis()

        if (remainingTime > 0) {
            val minutes = (remainingTime / 1000) / 60
            val seconds = (remainingTime / 1000) % 60
            _uiState.update { it.copy(error = "Solicitud rechazada. Reintenta en ${minutes}m ${seconds}s.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = authRepository.joinOrganizationProfile(user, code)
            if (result.isSuccess) {
                // Al unirse exitosamente, el status pasa a PENDING y lo detectará observeUserProfile
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al unirse: ${result.exceptionOrNull()?.message}") }
            }
        }
    }
}

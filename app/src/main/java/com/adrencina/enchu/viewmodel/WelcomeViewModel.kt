package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val inviteCode: String = "",
    val isProfileCreated: Boolean = false
)

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

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

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = authRepository.joinOrganizationProfile(user, code)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isProfileCreated = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al unirse: ${result.exceptionOrNull()?.message}") }
            }
        }
    }
}

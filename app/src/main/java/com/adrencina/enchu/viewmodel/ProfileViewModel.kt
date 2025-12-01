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

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val obrasCount: Int = 0,
    val clientesCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obraRepository: ObraRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadStats()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(user = authRepository.currentUser) }
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
        authRepository.signOut()
    }
}

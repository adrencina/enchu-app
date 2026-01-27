package com.adrencina.enchu.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado para representar el resultado del intento de inicio de sesión
data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isLoading: Boolean = false,
    val needsOnboarding: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    fun onSignInResult(token: String?) {
        viewModelScope.launch {
            _signInState.value = _signInState.value.copy(isLoading = true, signInError = null)
            
            if (token != null) {
                val isSuccessful = repo.firebaseSignInWithGoogle(token)
                if (isSuccessful) {
                    // Verificamos si ya tiene perfil completo para saltar el onboarding
                    val user = repo.currentUser
                    val profile = user?.uid?.let { repo.getUserProfileById(it) }
                    val needsOnboarding = profile?.organizationId.isNullOrBlank()
                    
                    _signInState.value = SignInState(
                        isSignInSuccessful = true,
                        isLoading = false,
                        needsOnboarding = needsOnboarding
                    )
                } else {
                    _signInState.value = SignInState(
                        isSignInSuccessful = false,
                        signInError = "Error al iniciar sesión con Firebase.",
                        isLoading = false
                    )
                }
            } else {
                _signInState.value = SignInState(
                    isSignInSuccessful = false,
                    signInError = "No se pudo obtener el token de Google.",
                    isLoading = false
                )
            }
        }
    }
}
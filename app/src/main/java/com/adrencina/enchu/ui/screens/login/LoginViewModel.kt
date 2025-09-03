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
    val signInError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    fun onSignInResult(token: String?) {
        viewModelScope.launch {
            if (token != null) {
                val isSuccessful = repo.firebaseSignInWithGoogle(token)
                _signInState.value = SignInState(
                    isSignInSuccessful = isSuccessful,
                    signInError = if (!isSuccessful) "Error al iniciar sesión con Firebase." else null
                )
            } else {
                _signInState.value = SignInState(
                    isSignInSuccessful = false,
                    signInError = "No se pudo obtener el token de Google."
                )
            }
        }
    }
}
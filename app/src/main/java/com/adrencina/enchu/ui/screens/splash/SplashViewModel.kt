package com.adrencina.enchu.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Usamos una clase sellada para eventos, ideal para acciones de un solo uso como la navegación.
sealed class SplashUiEvent {
    object NavigateToHome : SplashUiEvent()
    object NavigateToLogin : SplashUiEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    // Un SharedFlow es perfecto para emitir eventos que deben ser consumidos solo una vez.
    private val _uiEvent = MutableSharedFlow<SplashUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // Delay para mejorar la experiencia de usuario y mostrar la marca.
            delay(1500L)

            // Tu lógica de negocio se mantiene intacta.
            val event = if (repo.currentUser != null) {
                SplashUiEvent.NavigateToHome
            } else {
                SplashUiEvent.NavigateToLogin
            }
            _uiEvent.emit(event)
        }
    }
}
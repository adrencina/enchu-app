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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
// Usamos una clase sellada para eventos, ideal para acciones de un solo uso como la navegación.
sealed class SplashUiEvent {
    object NavigateToHome : SplashUiEvent()
    object NavigateToLogin : SplashUiEvent()
    object NavigateToWelcome : SplashUiEvent()
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
            delay(1500L) // Delay para mejorar la experiencia de usuario

            if (repo.currentUser != null) {
                // Usamos firstOrNull para obtener el primer valor y no quedarnos esperando si el Flow nunca emite
                val userProfile = repo.getUserProfile().firstOrNull()
                if (userProfile?.organizationId.isNullOrBlank()) {
                    _uiEvent.emit(SplashUiEvent.NavigateToWelcome)
                } else {
                    _uiEvent.emit(SplashUiEvent.NavigateToHome)
                }
            } else {
                _uiEvent.emit(SplashUiEvent.NavigateToLogin)
            }
        }
    }
}
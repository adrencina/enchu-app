package com.adrencina.enchu.ui.screens.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.BillingRepositoryImpl
import com.adrencina.enchu.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val isPro: Boolean = false
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billingRepository.isPro.collect { isPro ->
                _uiState.update { it.copy(isPro = isPro) }
            }
        }

        viewModelScope.launch {
            billingRepository.purchasesUpdateFlow.collect { message ->
                _uiState.update { it.copy(userMessage = message, isLoading = false) }
            }
        }
    }

    fun subscribe(activity: Activity, plan: String) {
        val productId = if (plan == "ANNUAL") {
            BillingRepositoryImpl.PRO_ANNUAL_ID
        } else {
            BillingRepositoryImpl.PRO_MONTHLY_ID
        }

        _uiState.update { it.copy(isLoading = true, userMessage = null) }
        
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: ""
            billingRepository.launchBillingFlow(activity, userId, productId)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
package com.adrencina.enchu.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val isPro: Flow<Boolean>
    val purchasesUpdateFlow: Flow<String?> // Flow para mensajes de estado (éxito, error)

    suspend fun startConnection()
    suspend fun launchBillingFlow(activity: Activity, userId: String)
    suspend fun checkSubscriptionStatus()
    fun terminateConnection()
}
package com.adrencina.enchu.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.adrencina.enchu.domain.repository.BillingRepository
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _purchasesUpdateFlow = MutableStateFlow<String?>(null)
    override val purchasesUpdateFlow: StateFlow<String?> = _purchasesUpdateFlow.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // IDs de productos en Google Play Console
    companion object {
        const val PRO_MONTHLY_ID = "suscripcion_mensual_pro"
        const val PRO_ANNUAL_ID = "suscripcion_anual_pro"
        private val ALL_PRODUCTS = listOf(PRO_MONTHLY_ID, PRO_ANNUAL_ID)
    }

    override suspend fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingRepository", "Conexión con Google Play establecida")
                    CoroutineScope(Dispatchers.IO).launch {
                        checkSubscriptionStatus()
                    }
                } else {
                    Log.e("BillingRepository", "Error al conectar: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingRepository", "Desconectado de Google Play. Reintentando...")
            }
        })
    }

    override suspend fun launchBillingFlow(activity: Activity, userId: String, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        withContext(Dispatchers.IO) {
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList[0]
                    
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                        )
                        .setObfuscatedAccountId(userId)
                        .setObfuscatedProfileId(userId)
                        .build()

                    billingClient.launchBillingFlow(activity, billingFlowParams)
                } else {
                    Log.e("BillingRepository", "No se encontraron detalles del producto: $productId")
                    _purchasesUpdateFlow.value = "Error: El plan seleccionado no está disponible en Google Play."
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                CoroutineScope(Dispatchers.IO).launch {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i("BillingRepository", "Compra cancelada por el usuario")
        } else {
            Log.e("BillingRepository", "Error en compra: ${billingResult.debugMessage}")
            _purchasesUpdateFlow.value = "Error en la compra: ${billingResult.debugMessage}"
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingRepository", "Compra reconocida exitosamente")
                        _isPro.value = true
                        _purchasesUpdateFlow.value = "¡Bienvenido a PRO!"
                    }
                }
            } else {
                 _isPro.value = true
            }
        }
    }

    override suspend fun checkSubscriptionStatus() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var hasActiveSubscription = false
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val isProProduct = purchase.products.any { it in ALL_PRODUCTS }
                        if (isProProduct) {
                            hasActiveSubscription = true
                            if (!purchase.isAcknowledged) {
                                CoroutineScope(Dispatchers.IO).launch { handlePurchase(purchase) }
                            }
                        }
                    }
                }
                _isPro.value = hasActiveSubscription
                Log.d("BillingRepository", "Estado de suscripción PRO: $hasActiveSubscription")
            }
        }
    }

    override fun terminateConnection() {
        billingClient.endConnection()
    }
}
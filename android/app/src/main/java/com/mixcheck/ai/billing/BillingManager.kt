
package com.mixcheck.ai.billing

import android.content.Context
import com.android.billingclient.api.*

/**
 * Wrapper Google Play Billing v6 - verificacion segura debe hacerse en backend /billing/verify
 * No confiar solo en cliente para validar suscripcion
 */
class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    var onEntitlementChanged: ((Entitlement) -> Unit)? = null

    fun startConnection(onReady: ()->Unit) {
        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) onReady()
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun queryProducts(onResult: (List<ProductDetails>)->Unit) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(ProductIds.SUBSCRIPTIONS.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
            } + ProductIds.CONSUMABLES.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
            }).build()
        billingClient.queryProductDetailsAsync(params) { _, list -> onResult(list) }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // TODO: enviar purchases a backend /billing/verify con Google Play Developer API
            // Por seguridad, no activar PRO solo con cliente
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // verificar en backend
                }
            }
        }
    }

    fun launchPurchase(productDetails: ProductDetails) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )).build()
        billingClient.launchBillingFlow(context as android.app.Activity, params)
    }
}

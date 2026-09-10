
package com.mixcheck.ai.billing

object ProductIds {
    const val PRO_MONTHLY = "mixcheck_pro_monthly"
    const val PRO_YEARLY = "mixcheck_pro_yearly"
    const val STUDIO_MONTHLY = "mixcheck_studio_monthly"
    const val STUDIO_YEARLY = "mixcheck_studio_yearly"
    const val CREDITS_10 = "credits_10"
    const val CREDITS_20 = "credits_20"

    val SUBSCRIPTIONS = listOf(PRO_MONTHLY, PRO_YEARLY, STUDIO_MONTHLY, STUDIO_YEARLY)
    val CONSUMABLES = listOf(CREDITS_10, CREDITS_20)
}

enum class Plan { FREE, PRO, STUDIO }

data class Entitlement(
    val plan: Plan,
    val analysesUsed: Int,
    val analysesLimit: Int,
    val isActive: Boolean
) {
    fun remaining(): Int = (analysesLimit - analysesUsed).coerceAtLeast(0)
}

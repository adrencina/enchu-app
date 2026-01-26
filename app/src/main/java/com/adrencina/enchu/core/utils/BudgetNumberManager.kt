package com.adrencina.enchu.core.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetNumberManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
    private val KEY_LAST_NUMBER = "last_budget_number"

    /**
     * Obtiene el siguiente número disponible y lo reserva (incrementa el contador).
     */
    fun getNextNumber(): Int {
        val lastNumber = prefs.getInt(KEY_LAST_NUMBER, 0)
        val nextNumber = lastNumber + 1
        prefs.edit().putInt(KEY_LAST_NUMBER, nextNumber).apply()
        return nextNumber
    }

    /**
     * Solo para visualización, no incrementa.
     */
    fun peekNextNumber(): Int {
        return prefs.getInt(KEY_LAST_NUMBER, 0) + 1
    }
}

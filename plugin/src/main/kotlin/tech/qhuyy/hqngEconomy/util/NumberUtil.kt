package tech.qhuyy.hqngEconomy.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberUtil {

    private val symbols = DecimalFormatSymbols(Locale.US)

    fun formatMoney(amount: Double, decimals: Int = 2): String {
        val pattern = buildString {
            append("#,##0")
            if (decimals > 0) {
                append(".")
                repeat(decimals) { append("0") }
            }
        }
        return DecimalFormat(pattern, symbols).format(amount)
    }

    fun formatGems(amount: Int): String {
        return DecimalFormat("#,##0", symbols).format(amount)
    }

    fun parseAmount(input: String): Double? {
        return input.replace(",", "").toDoubleOrNull()
    }

    /**
     * Compact notation: 1200 → "1.2K", 1500000 → "1.5M", etc.
     * Thresholds: T (1T), B (1B), M (1M), K (1K)
     */
    fun formatCompact(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        return when {
            abs >= 1_000_000_000_000.0 -> String.format("%.1fT", amount / 1_000_000_000_000.0)
            abs >= 1_000_000_000.0    -> String.format("%.1fB", amount / 1_000_000_000.0)
            abs >= 1_000_000.0         -> String.format("%.1fM", amount / 1_000_000.0)
            abs >= 1_000.0             -> String.format("%.1fK", amount / 1_000.0)
            else                       -> formatMoney(amount)
        }
    }

    /**
     * Returns [singular] when amount == 1, otherwise [plural].
     * Works for both Int and Double amounts.
     * Examples:
     *   pluralize(1, "Coin", "Coins")  → "Coin"
     *   pluralize(100, "Coin", "Coins") → "Coins"
     *   pluralize(0, "Gem", "Gems")    → "Gems"
     *   pluralize(1.5, "Coin", "Coins") → "Coins"
     */
    fun pluralize(amount: Number, singular: String, plural: String): String {
        val isOne = when (amount) {
            is Int    -> amount == 1
            is Long   -> amount == 1L
            is Double -> amount == 1.0
            is Float  -> amount == 1f
            else      -> amount.toDouble() == 1.0
        }
        return if (isOne) singular else plural
    }
}

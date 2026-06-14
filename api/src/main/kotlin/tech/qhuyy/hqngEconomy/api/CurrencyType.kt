package tech.qhuyy.hqngEconomy.api

/**
 * Supported currency types.
 */
enum class CurrencyType(val displayName: String, val singular: String, val plural: String) {
    MONEY("Money", "Coin", "Coins"),
    GEM("Gem", "Gem", "Gems");

    companion object {
        fun fromString(name: String): CurrencyType? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

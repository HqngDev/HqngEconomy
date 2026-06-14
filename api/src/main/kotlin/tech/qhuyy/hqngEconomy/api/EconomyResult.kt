package tech.qhuyy.hqngEconomy.api

/**
 * Result of an economy operation.
 */
data class EconomyResult(
    val success: Boolean,
    val message: String,
    val balanceBefore: Double = 0.0,
    val balanceAfter: Double = 0.0
) {
    companion object {
        fun success(message: String, before: Double = 0.0, after: Double = 0.0) =
            EconomyResult(true, message, before, after)

        fun failure(message: String) =
            EconomyResult(false, message)
    }
}

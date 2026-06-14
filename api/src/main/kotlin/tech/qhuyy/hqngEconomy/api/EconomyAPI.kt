package tech.qhuyy.hqngEconomy.api

/**
 * Static access point for the EconomyProvider.
 * Other plugins can use: EconomyAPI.provider.getBalance(uuid)
 */
object EconomyAPI {

    @Volatile
    private var _provider: EconomyProvider? = null

    /** The active economy provider instance. */
    val provider: EconomyProvider
        get() = checkNotNull(_provider) {
            "HqngEconomy provider not yet initialized. Wait for plugin enable."
        }

    /** Whether the provider has been registered. */
    val isAvailable: Boolean
        get() = _provider != null

    /**
     * Register the provider. Called by HqngEconomy on enable.
     */
    fun register(p: EconomyProvider) {
        _provider = p
    }

    /**
     * Unregister the provider. Called by HqngEconomy on disable.
     */
    fun unregister() {
        _provider = null
    }
}

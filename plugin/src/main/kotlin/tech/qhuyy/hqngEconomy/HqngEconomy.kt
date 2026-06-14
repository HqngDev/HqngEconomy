package tech.qhuyy.hqngEconomy

import org.bukkit.plugin.java.JavaPlugin
import tech.qhuyy.hqngEconomy.api.EconomyAPI
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.api.EconomyProviderImpl
import tech.qhuyy.hqngEconomy.command.BalanceCommand
import tech.qhuyy.hqngEconomy.command.EcoCommand
import tech.qhuyy.hqngEconomy.command.GemCommand
import tech.qhuyy.hqngEconomy.command.PayCommand
import tech.qhuyy.hqngEconomy.data.AccountCache
import tech.qhuyy.hqngEconomy.data.AccountRepository
import tech.qhuyy.hqngEconomy.data.DatabaseManager
import tech.qhuyy.hqngEconomy.expansion.PlaceholderExpansion
import tech.qhuyy.hqngEconomy.listener.PlayerListener
import tech.qhuyy.hqngEconomy.util.MessageManager

class HqngEconomy : JavaPlugin() {

    lateinit var databaseManager: DatabaseManager
        private set
    lateinit var accountCache: AccountCache
        private set
    lateinit var economyProvider: EconomyProviderImpl
        private set
    lateinit var messageManager: MessageManager
        private set

    private var placeholderExpansion: PlaceholderExpansion? = null

    override fun onEnable() {
        // ── Config ───────────────────────────────────────────────
        saveDefaultConfig()
        reloadConfig()

        // ── Messages ─────────────────────────────────────────────
        messageManager = MessageManager(this)

        // ── Database ─────────────────────────────────────────────
        databaseManager = DatabaseManager(this)
        databaseManager.initialize()
        databaseManager.createTables()

        // ── Cache ────────────────────────────────────────────────
        val startingMoney = config.getDouble("starting.money", 100.0)
        val startingGems = config.getInt("starting.gem", 0)
        val repository = AccountRepository(databaseManager)
        accountCache = AccountCache(this, repository, startingMoney, startingGems)

        // ── API ──────────────────────────────────────────────────
        economyProvider = EconomyProviderImpl(this, accountCache)
        EconomyAPI.register(economyProvider)

        // Register via Paper ServiceManager for other plugins
        server.servicesManager.register(
            EconomyProvider::class.java,
            economyProvider,
            this,
            org.bukkit.plugin.ServicePriority.Normal
        )

        // ── Commands (all receive MessageManager) ────────────────
        getCommand("balance")?.setExecutor(BalanceCommand(economyProvider, messageManager))
        getCommand("gem")?.setExecutor(GemCommand(economyProvider, messageManager))
        getCommand("pay")?.setExecutor(PayCommand(economyProvider, messageManager))
        getCommand("eco")?.setExecutor(EcoCommand(economyProvider, messageManager))

        // ── Listeners ────────────────────────────────────────────
        server.pluginManager.registerEvents(PlayerListener(economyProvider), this)

        // ── PlaceholderAPI ───────────────────────────────────────
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = PlaceholderExpansion().also { it.register() }
            logger.info("PlaceholderAPI expansion registered.")
        } else {
            logger.info("PlaceholderAPI not found — expansion not registered.")
        }

        // ── Auto-save task ───────────────────────────────────────
        val autoSaveInterval = 20L * 60 * 5 // 5 minutes
        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            accountCache.saveAll()
        }, autoSaveInterval, autoSaveInterval)

        logger.info("HqngEconomy enabled! Database: ${databaseManager.databaseType}")
    }

    override fun onDisable() {
        // Unregister PlaceholderAPI expansion
        placeholderExpansion?.let {
            it.unregister()
            logger.info("PlaceholderAPI expansion unregistered.")
        }

        // Save all cached accounts
        if (::accountCache.isInitialized) {
            accountCache.saveAll()
        }

        // Unregister API
        EconomyAPI.unregister()

        // Close database
        if (::databaseManager.isInitialized) {
            databaseManager.shutdown()
        }

        logger.info("HqngEconomy disabled.")
    }
}

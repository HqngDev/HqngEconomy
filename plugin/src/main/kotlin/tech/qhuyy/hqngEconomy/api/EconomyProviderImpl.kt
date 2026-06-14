package tech.qhuyy.hqngEconomy.api

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import tech.qhuyy.hqngEconomy.data.AccountCache
import java.util.UUID

/**
 * Thread-safe implementation of EconomyProvider.
 * All mutations happen on the main server thread.
 */
class EconomyProviderImpl(
    private val plugin: JavaPlugin,
    private val cache: AccountCache
) : EconomyProvider {

    private fun runSync(action: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            action()
        } else {
            Bukkit.getScheduler().runTask(plugin, action)
        }
    }

    // ── Balance queries ──────────────────────────────────────────

    override fun getBalance(player: UUID): Double = getBalance(player, CurrencyType.MONEY)

    override fun getBalance(player: UUID, currency: CurrencyType): Double {
        val account = cache.getOrLoad(player)
        return when (currency) {
            CurrencyType.MONEY -> account.money
            CurrencyType.GEM -> account.gems.toDouble()
        }
    }

    // ── Money operations ────────────────────────────────────────

    override fun deposit(player: UUID, amount: Double): EconomyResult {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive.")
        val account = cache.getOrLoad(player)
        val before = account.money
        account.money += amount
        account.dirty = true
        return EconomyResult.success("Deposited.", before, account.money)
    }

    override fun withdraw(player: UUID, amount: Double): EconomyResult {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive.")
        val account = cache.getOrLoad(player)
        if (account.money < amount) {
            return EconomyResult.failure("Insufficient funds. Balance: ${account.money}")
        }
        val before = account.money
        account.money -= amount
        account.dirty = true
        return EconomyResult.success("Withdrawn.", before, account.money)
    }

    override fun transfer(from: UUID, to: UUID, amount: Double): EconomyResult {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive.")
        if (from == to) return EconomyResult.failure("Cannot transfer to yourself.")

        val fromAccount = cache.getOrLoad(from)
        if (fromAccount.money < amount) {
            return EconomyResult.failure("Insufficient funds. Balance: ${fromAccount.money}")
        }

        val toAccount = cache.getOrLoad(to)
        val fromBefore = fromAccount.money
        val toBefore = toAccount.money

        fromAccount.money -= amount
        toAccount.money += amount
        fromAccount.dirty = true
        toAccount.dirty = true

        return EconomyResult.success(
            "Transferred $amount Money.",
            fromBefore,
            fromAccount.money
        )
    }

    override fun setBalance(player: UUID, amount: Double): EconomyResult {
        if (amount < 0) return EconomyResult.failure("Balance cannot be negative.")
        val account = cache.getOrLoad(player)
        val before = account.money
        account.money = amount
        account.dirty = true
        return EconomyResult.success("Balance set.", before, account.money)
    }

    override fun resetBalance(player: UUID): EconomyResult {
        val starting = plugin.config.getDouble("starting.money", 100.0)
        val account = cache.getOrLoad(player)
        val before = account.money
        account.money = starting
        account.dirty = true
        return EconomyResult.success("Balance reset.", before, account.money)
    }

    // ── Gem operations ──────────────────────────────────────────

    override fun getGems(player: UUID): Int {
        return cache.getOrLoad(player).gems
    }

    override fun setGems(player: UUID, amount: Int): EconomyResult {
        if (amount < 0) return EconomyResult.failure("Gems cannot be negative.")
        val account = cache.getOrLoad(player)
        val before = account.gems.toDouble()
        account.gems = amount
        account.dirty = true
        return EconomyResult.success("Gems set.", before, amount.toDouble())
    }

    override fun giveGems(player: UUID, amount: Int): EconomyResult {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive.")
        val account = cache.getOrLoad(player)
        val before = account.gems.toDouble()
        account.gems += amount
        account.dirty = true
        return EconomyResult.success("Gems added.", before, account.gems.toDouble())
    }

    override fun resetGems(player: UUID): EconomyResult {
        val account = cache.getOrLoad(player)
        val before = account.gems.toDouble()
        account.gems = 0
        account.dirty = true
        return EconomyResult.success("Gems reset.", before, 0.0)
    }

    // ── Account management ──────────────────────────────────────

    override fun hasAccount(player: UUID): Boolean = cache.exists(player)

    override fun createAccount(player: UUID) {
        runSync { cache.create(player) }
    }

    override fun loadAccount(player: UUID) {
        runSync { cache.getOrLoad(player) }
    }

    override fun unloadAccount(player: UUID) {
        runSync { cache.unload(player) }
    }

    override fun saveAccount(player: UUID) {
        runSync { cache.save(player) }
    }
}

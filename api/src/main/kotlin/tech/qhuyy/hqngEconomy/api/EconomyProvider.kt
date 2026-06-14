package tech.qhuyy.hqngEconomy.api

import java.util.UUID

/**
 * Public API for other plugins to interact with HqngEconomy.
 * All operations are thread-safe.
 */
interface EconomyProvider {

    // ── Balance queries ──────────────────────────────────────────

    /** Get a player's Money balance. */
    fun getBalance(player: UUID): Double

    /** Get a player's balance for a specific currency. */
    fun getBalance(player: UUID, currency: CurrencyType): Double

    // ── Money operations ────────────────────────────────────────

    /** Deposit Money into a player's account. */
    fun deposit(player: UUID, amount: Double): EconomyResult

    /** Withdraw Money from a player's account. */
    fun withdraw(player: UUID, amount: Double): EconomyResult

    /** Transfer Money between players. */
    fun transfer(from: UUID, to: UUID, amount: Double): EconomyResult

    /** Set a player's Money balance. */
    fun setBalance(player: UUID, amount: Double): EconomyResult

    /** Reset a player's Money balance to the starting amount. */
    fun resetBalance(player: UUID): EconomyResult

    // ── Gem operations (admin only for give/set) ────────────────

    /** Get a player's Gem balance. */
    fun getGems(player: UUID): Int

    /** Set a player's Gem balance (admin). */
    fun setGems(player: UUID, amount: Int): EconomyResult

    /** Add Gems to a player (admin give). */
    fun giveGems(player: UUID, amount: Int): EconomyResult

    /** Reset a player's Gem balance to 0 (admin). */
    fun resetGems(player: UUID): EconomyResult

    // ── Account management ──────────────────────────────────────

    /** Check if an account exists for the player. */
    fun hasAccount(player: UUID): Boolean

    /** Create an account if it doesn't exist. */
    fun createAccount(player: UUID)

    /** Load account data from database into cache. */
    fun loadAccount(player: UUID)

    /** Save and unload account from cache. */
    fun unloadAccount(player: UUID)

    /** Save account to database without unloading. */
    fun saveAccount(player: UUID)
}

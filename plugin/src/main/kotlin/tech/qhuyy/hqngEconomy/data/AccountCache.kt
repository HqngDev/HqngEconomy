package tech.qhuyy.hqngEconomy.data

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory cache of loaded accounts with auto-save.
 */
class AccountCache(
    private val plugin: JavaPlugin,
    private val repository: AccountRepository,
    private val startingMoney: Double,
    private val startingGems: Int
) {
    private val accounts = ConcurrentHashMap<UUID, Account>()

    /** Get or load an account into cache. */
    fun getOrLoad(uuid: UUID): Account {
        return accounts.getOrPut(uuid) {
            repository.load(uuid) ?: Account(
                uuid = uuid,
                money = startingMoney,
                gems = startingGems
            ).also { repository.insert(it) }
        }
    }

    /** Get an account if it's already cached. */
    fun getCached(uuid: UUID): Account? = accounts[uuid]

    /** Check if account is cached. */
    fun isCached(uuid: UUID): Boolean = accounts.containsKey(uuid)

    /** Check if account exists in database. */
    fun exists(uuid: UUID): Boolean = if (isCached(uuid)) true else repository.exists(uuid)

    /** Create a new account (inserts into DB). */
    fun create(uuid: UUID): Account {
        val account = Account(uuid, startingMoney, startingGems)
        repository.insert(account)
        accounts[uuid] = account
        return account
    }

    /** Save a specific account to DB. */
    fun save(uuid: UUID) {
        accounts[uuid]?.let { repository.save(it) }
    }

    /** Save and remove from cache. */
    fun unload(uuid: UUID) {
        accounts.remove(uuid)?.let { repository.save(it) }
    }

    /** Save all dirty accounts. */
    fun saveAll() {
        var saved = 0
        accounts.values.filter { it.dirty }.forEach {
            repository.save(it)
            saved++
        }
        if (saved > 0) plugin.logger.info("Saved $saved dirty accounts.")
    }

    /** Get all cached accounts (for admin use). */
    fun allCached(): Collection<Account> = accounts.values

    /** Remove from cache without saving. */
    fun remove(uuid: UUID) = accounts.remove(uuid)
}

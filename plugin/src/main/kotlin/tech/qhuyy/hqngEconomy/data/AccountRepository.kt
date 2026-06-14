package tech.qhuyy.hqngEconomy.data

import java.util.UUID

/**
 * Data access layer for player accounts.
 */
class AccountRepository(private val db: DatabaseManager) {

    fun load(uuid: UUID): Account? {
        db.getConnection().use { conn ->
            conn.prepareStatement("SELECT money, gems FROM hqng_accounts WHERE uuid = ?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        Account(
                            uuid = uuid,
                            money = rs.getDouble("money"),
                            gems = rs.getInt("gems")
                        )
                    } else null
                }
            }
        }
    }

    fun save(account: Account) {
        val upsert = when (db.databaseType) {
            DatabaseType.MYSQL ->
                """
                INSERT INTO hqng_accounts (uuid, money, gems) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE money = VALUES(money), gems = VALUES(gems)
                """.trimIndent()
            DatabaseType.SQLITE ->
                """
                INSERT OR REPLACE INTO hqng_accounts (uuid, money, gems, updated_at)
                VALUES (?, ?, ?, datetime('now'))
                """.trimIndent()
        }

        db.getConnection().use { conn ->
            conn.prepareStatement(upsert).use { ps ->
                ps.setString(1, account.uuid.toString())
                ps.setDouble(2, account.money)
                ps.setInt(3, account.gems)
                ps.executeUpdate()
            }
        }
        account.dirty = false
    }

    fun insert(account: Account) {
        db.getConnection().use { conn ->
            conn.prepareStatement(
                "INSERT OR IGNORE INTO hqng_accounts (uuid, money, gems) VALUES (?, ?, ?)"
            ).use { ps ->
                ps.setString(1, account.uuid.toString())
                ps.setDouble(2, account.money)
                ps.setInt(3, account.gems)
                ps.executeUpdate()
            }
        }
    }

    fun exists(uuid: UUID): Boolean {
        db.getConnection().use { conn ->
            conn.prepareStatement("SELECT 1 FROM hqng_accounts WHERE uuid = ?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { return it.next() }
            }
        }
    }
}

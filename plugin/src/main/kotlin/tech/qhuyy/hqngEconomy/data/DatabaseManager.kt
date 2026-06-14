package tech.qhuyy.hqngEconomy.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection

/**
 * Manages database connections via HikariCP (MySQL) or direct JDBC (SQLite).
 */
class DatabaseManager(private val plugin: JavaPlugin) {

    lateinit var dataSource: HikariDataSource
        private set

    lateinit var databaseType: DatabaseType
        private set

    fun initialize() {
        databaseType = DatabaseType.fromString(
            plugin.config.getString("database.type", "SQLITE") ?: "SQLITE"
        )

        val hikariConfig = HikariConfig()

        when (databaseType) {
            DatabaseType.MYSQL -> {
                val host = plugin.config.getString("database.mysql.host", "localhost")!!
                val port = plugin.config.getInt("database.mysql.port", 3306)
                val database = plugin.config.getString("database.mysql.database", "hqng_economy")!!
                val username = plugin.config.getString("database.mysql.username", "root")!!
                val password = plugin.config.getString("database.mysql.password", "password")!!
                val poolSize = plugin.config.getInt("database.mysql.pool-size", 10)

                hikariConfig.jdbcUrl = "jdbc:mysql://$host:$port/$database?useSSL=false&serverTimezone=UTC"
                hikariConfig.username = username
                hikariConfig.password = password
                hikariConfig.maximumPoolSize = poolSize
                hikariConfig.poolName = "HqngEconomy-MySQL"
            }

            DatabaseType.SQLITE -> {
                val file = plugin.config.getString("database.sqlite.file", "economy.db")!!
                val dbPath = plugin.dataFolder.resolve(file).absolutePath
                hikariConfig.jdbcUrl = "jdbc:sqlite:$dbPath"
                hikariConfig.maximumPoolSize = 1
                hikariConfig.poolName = "HqngEconomy-SQLite"
            }
        }

        hikariConfig.connectionTestQuery = "SELECT 1"
        hikariConfig.minimumIdle = 1
        hikariConfig.idleTimeout = 30_000
        hikariConfig.maxLifetime = 600_000
        hikariConfig.connectionTimeout = 10_000

        dataSource = HikariDataSource(hikariConfig)
        plugin.logger.info("Database initialized: $databaseType")
    }

    fun getConnection(): Connection = dataSource.connection

    fun createTables() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                if (databaseType == DatabaseType.MYSQL) {
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS hqng_accounts (
                            uuid VARCHAR(36) PRIMARY KEY,
                            money DOUBLE NOT NULL DEFAULT 0,
                            gems INT NOT NULL DEFAULT 0,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """.trimIndent()
                    )
                } else {
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS hqng_accounts (
                            uuid TEXT PRIMARY KEY,
                            money REAL NOT NULL DEFAULT 0,
                            gems INTEGER NOT NULL DEFAULT 0,
                            updated_at TEXT DEFAULT (datetime('now'))
                        )
                        """.trimIndent()
                    )
                }
            }
        }
        plugin.logger.info("Database tables created/verified.")
    }

    fun shutdown() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
            plugin.logger.info("Database connection pool closed.")
        }
    }
}

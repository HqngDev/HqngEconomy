package tech.qhuyy.hqngEconomy.data

enum class DatabaseType {
    MYSQL,
    SQLITE;

    companion object {
        fun fromString(name: String): DatabaseType =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SQLITE
    }
}

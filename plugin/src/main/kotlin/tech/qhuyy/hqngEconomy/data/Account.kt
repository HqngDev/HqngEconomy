package tech.qhuyy.hqngEconomy.data

import java.util.UUID

/**
 * Represents a player's economy account.
 */
data class Account(
    val uuid: UUID,
    var money: Double = 0.0,
    var gems: Int = 0,
    var dirty: Boolean = false  // true if modified since last save
)

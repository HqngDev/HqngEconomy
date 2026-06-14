package tech.qhuyy.hqngEconomy.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import tech.qhuyy.hqngEconomy.api.EconomyProvider

class PlayerListener(private val provider: EconomyProvider) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        provider.loadAccount(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        provider.unloadAccount(event.player.uniqueId)
    }
}

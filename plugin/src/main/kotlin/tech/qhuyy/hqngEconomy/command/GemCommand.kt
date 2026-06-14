package tech.qhuyy.hqngEconomy.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.util.MessageManager
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatGems
import tech.qhuyy.hqngEconomy.util.NumberUtil.pluralize

class GemCommand(
    private val provider: EconomyProvider,
    private val messages: MessageManager
) : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        val isPlayer = sender is Player
        val hasAdmin = sender.hasPermission("hqng.economy.admin")

        if (args.isEmpty()) {
            // No args → self lookup (must be a player)
            if (!isPlayer) {
                messages.send(sender, "balance.gem.specify-player")
                return true
            }
            showSelf(sender, sender.uniqueId)
            return true
        }

        // Args provided → look up other player (admin or console)
        if (!hasAdmin && isPlayer) {
            messages.send(sender, "no-permission")
            return true
        }

        val targetName = args[0]
        val target = Bukkit.getOfflinePlayer(targetName)
        if (!provider.hasAccount(target.uniqueId)) {
            messages.send(sender, "player-not-found", targetName)
            return true
        }

        val gems = provider.getGems(target.uniqueId)
        val singular = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.gem.singular", "Gem") ?: "Gem"
        val plural = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.gem.plural", "Gems") ?: "Gems"

        messages.send(sender, "balance.gem.other", targetName, formatGems(gems), pluralize(gems, singular, plural))
        return true
    }

    private fun showSelf(sender: CommandSender, uuid: java.util.UUID) {
        val gems = provider.getGems(uuid)
        val singular = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.gem.singular", "Gem") ?: "Gem"
        val plural = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.gem.plural", "Gems") ?: "Gems"

        messages.send(sender, "balance.gem.self", formatGems(gems), pluralize(gems, singular, plural))
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> emptyList() // player names — let Bukkit handle it
            else -> emptyList()
        }
    }
}

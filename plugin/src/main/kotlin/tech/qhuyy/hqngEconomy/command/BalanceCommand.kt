package tech.qhuyy.hqngEconomy.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.util.MessageManager
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatMoney
import tech.qhuyy.hqngEconomy.util.NumberUtil.pluralize

class BalanceCommand(
    private val provider: EconomyProvider,
    private val messages: MessageManager
) : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        val isPlayer = sender is Player
        val hasAdmin = sender.hasPermission("hqng.economy.admin")

        if (args.isEmpty()) {
            // No args → self lookup (must be a player)
            if (!isPlayer) {
                messages.send(sender, "balance.specify-player")
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

        val balance = provider.getBalance(target.uniqueId)
        val singular = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.money.singular", "Coin") ?: "Coin"
        val plural = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.money.plural", "Coins") ?: "Coins"

        messages.send(sender, "balance.other", targetName, formatMoney(balance), pluralize(balance, singular, plural))
        return true
    }

    private fun showSelf(sender: CommandSender, uuid: java.util.UUID) {
        val balance = provider.getBalance(uuid)
        val singular = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.money.singular", "Coin") ?: "Coin"
        val plural = sender.server.pluginManager.getPlugin("HqngEconomy")
            ?.config?.getString("currency.money.plural", "Coins") ?: "Coins"

        messages.send(sender, "balance.self", formatMoney(balance), pluralize(balance, singular, plural))
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> emptyList() // player names — let Bukkit handle it
            else -> emptyList()
        }
    }
}

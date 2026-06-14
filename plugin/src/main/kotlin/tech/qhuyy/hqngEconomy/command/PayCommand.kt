package tech.qhuyy.hqngEconomy.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.util.MessageManager
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatMoney
import tech.qhuyy.hqngEconomy.util.NumberUtil.parseAmount

class PayCommand(
    private val provider: EconomyProvider,
    private val messages: MessageManager
) : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        val player = sender as? org.bukkit.entity.Player ?: run {
            messages.send(sender, "player-only")
            return true
        }

        if (args.size < 2) {
            messages.send(sender, "pay.usage")
            return true
        }

        val targetName = args[0]
        val amountStr = args[1]

        val target = Bukkit.getOfflinePlayer(targetName)
        if (!provider.hasAccount(target.uniqueId)) {
            messages.send(sender, "player-not-found", targetName)
            return true
        }

        val amount = parseAmount(amountStr) ?: run {
            messages.send(sender, "invalid-amount", amountStr)
            return true
        }

        if (amount <= 0) {
            messages.send(sender, "amount-positive")
            return true
        }

        val result = provider.transfer(player.uniqueId, target.uniqueId, amount)
        if (result.success) {
            messages.send(sender, "pay.success", formatMoney(amount), targetName, formatMoney(result.balanceAfter))

            // Notify target if online
            Bukkit.getPlayer(target.uniqueId)?.let { onlineTarget ->
                messages.send(
                    onlineTarget, "pay.received",
                    formatMoney(amount),
                    player.name,
                    formatMoney(provider.getBalance(onlineTarget.uniqueId))
                )
            }
        } else {
            messages.send(sender, "generic-error", result.message)
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> emptyList()
            2 -> listOf("100", "500", "1000").filter { it.startsWith(args[1], ignoreCase = true) }
            else -> emptyList()
        }
    }
}

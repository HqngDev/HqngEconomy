package tech.qhuyy.hqngEconomy.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import tech.qhuyy.hqngEconomy.api.CurrencyType
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.util.MessageManager
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatGems
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatMoney
import tech.qhuyy.hqngEconomy.util.NumberUtil.parseAmount
import tech.qhuyy.hqngEconomy.util.NumberUtil.pluralize

class EcoCommand(
    private val provider: EconomyProvider,
    private val messages: MessageManager
) : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("hqng.economy.admin")) {
            messages.send(sender, "no-permission")
            return true
        }

        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            return handleReload(sender)
        }

        if (args.size < 4) {
            messages.send(sender, "eco.usage")
            return true
        }

        val action = args[0].lowercase()
        val targetName = args[1]
        val currencyStr = args[2].lowercase()
        val amountStr = args[3]

        val target = Bukkit.getOfflinePlayer(targetName)
        val currency = CurrencyType.fromString(currencyStr)
        if (currency == null) {
            messages.send(sender, "eco.invalid-currency")
            return true
        }

        when (action) {
            "give" -> handleGive(sender, target.uniqueId, targetName, currency, amountStr)
            "set" -> handleSet(sender, target.uniqueId, targetName, currency, amountStr)
            "reset" -> handleReset(sender, target.uniqueId, targetName, currency)
            else -> messages.send(sender, "eco.invalid-action")
        }
        return true
    }

    private fun handleReload(sender: CommandSender): Boolean {
        return try {
            messages.reload()
            sender.server.pluginManager.getPlugin("HqngEconomy")?.reloadConfig()
            messages.send(sender, "reload.success")
            true
        } catch (e: Exception) {
            messages.send(sender, "reload.error", e.message ?: "unknown")
            false
        }
    }

    private fun handleGive(sender: CommandSender, uuid: java.util.UUID, name: String, currency: CurrencyType, amountStr: String) {
        when (currency) {
            CurrencyType.MONEY -> {
                val amount = parseAmount(amountStr) ?: run {
                    messages.send(sender, "invalid-amount", amountStr)
                    return
                }
                val result = provider.deposit(uuid, amount)
                if (result.success) {
                    messages.send(sender, "eco.give.money", formatMoney(amount), name, formatMoney(result.balanceAfter))
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
            CurrencyType.GEM -> {
                val amount = amountStr.toIntOrNull() ?: run {
                    messages.send(sender, "invalid-amount", amountStr)
                    return
                }
                val result = provider.giveGems(uuid, amount)
                if (result.success) {
                    val gemSingular = Bukkit.getPluginManager().getPlugin("HqngEconomy")
                        ?.config?.getString("currency.gem.singular", "Gem") ?: "Gem"
                    val gemPlural = Bukkit.getPluginManager().getPlugin("HqngEconomy")
                        ?.config?.getString("currency.gem.plural", "Gems") ?: "Gems"
                    val currencyName = pluralize(amount, gemSingular, gemPlural)
                    messages.send(sender, "eco.give.gem", formatGems(amount), name, formatGems(result.balanceAfter.toInt()))
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
        }
    }

    private fun handleSet(sender: CommandSender, uuid: java.util.UUID, name: String, currency: CurrencyType, amountStr: String) {
        when (currency) {
            CurrencyType.MONEY -> {
                val amount = parseAmount(amountStr) ?: run {
                    messages.send(sender, "invalid-amount", amountStr)
                    return
                }
                val result = provider.setBalance(uuid, amount)
                if (result.success) {
                    messages.send(sender, "eco.set.money", name, formatMoney(amount))
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
            CurrencyType.GEM -> {
                val amount = amountStr.toIntOrNull() ?: run {
                    messages.send(sender, "invalid-amount", amountStr)
                    return
                }
                val result = provider.setGems(uuid, amount)
                if (result.success) {
                    messages.send(sender, "eco.set.gem", name, formatGems(amount))
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
        }
    }

    private fun handleReset(sender: CommandSender, uuid: java.util.UUID, name: String, currency: CurrencyType) {
        when (currency) {
            CurrencyType.MONEY -> {
                val result = provider.resetBalance(uuid)
                if (result.success) {
                    messages.send(sender, "eco.reset.money", name, formatMoney(result.balanceAfter))
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
            CurrencyType.GEM -> {
                val result = provider.resetGems(uuid)
                if (result.success) {
                    messages.send(sender, "eco.reset.gem", name)
                } else {
                    messages.send(sender, "generic-error", result.message)
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        if (!sender.hasPermission("hqng.economy.admin")) return emptyList()

        return when (args.size) {
            1 -> listOf("give", "set", "reset", "reload").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> emptyList()
            3 -> listOf("money", "gem").filter { it.startsWith(args[2], ignoreCase = true) }
            4 -> listOf("100", "1000", "10000").filter { it.startsWith(args[3], ignoreCase = true) }
            else -> emptyList()
        }
    }
}

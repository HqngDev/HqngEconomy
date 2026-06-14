package tech.qhuyy.hqngEconomy.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads messages from messages.properties (UTF-8), caches them in memory,
 * and provides placeholder substitution via {0}, {1}, ...
 *
 * Currency placeholders (resolved from plugin config at render time):
 *   {currency.money}       — singular money name from config (e.g. "Coin")
 *   {currency.money.plural} — plural money name from config (e.g. "Coins")
 *   {currency.gem}         — singular gem name from config (e.g. "Gem")
 *   {currency.gem.plural}   — plural gem name from config (e.g. "Gems")
 *
 * Usage:
 *   messageManager.get("balance.self", formatMoney(balance), currencyName)
 *   messageManager.send(player, "no-permission")
 */
class MessageManager(private val plugin: JavaPlugin) {

    private val miniMessage = MiniMessage.miniMessage()
    private val cache = ConcurrentHashMap<String, String>()

    /** Resource path inside the jar / plugin folder. */
    private val resourcePath = "messages.properties"

    init {
        reload()
    }

    /**
     * Reload messages from disk (plugin data folder first, then jar fallback).
     * Clears and repopulates the cache.
     */
    fun reload() {
        cache.clear()

        val file = plugin.dataFolder.resolve(resourcePath)

        if (!file.exists()) {
            // Copy default from jar to data folder on first run
            plugin.saveResource(resourcePath, false)
        }

        if (file.exists()) {
            // Read as UTF-8 to support non-ASCII characters
            InputStreamReader(file.inputStream(), Charsets.UTF_8).use { reader ->
                val props = java.util.Properties()
                props.load(reader)
                props.forEach { (key, value) ->
                    cache[key.toString()] = value.toString()
                }
            }
        } else {
            // Last resort: read directly from jar
            plugin.getResource(resourcePath)?.use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                    val props = java.util.Properties()
                    props.load(reader)
                    props.forEach { (key, value) ->
                        cache[key.toString()] = value.toString()
                    }
                }
            } ?: plugin.logger.warning("Could not find $resourcePath anywhere!")
        }

        plugin.logger.info("Loaded ${cache.size} message keys.")
    }

    /**
     * Get a raw message string by key, with optional placeholder args.
     * Resolves {prefix}, {currency.*}, and {0}/{1}/{2}... placeholders.
     */
    fun get(key: String, vararg args: Any): String {
        val template = cache[key] ?: return "<red>Missing key: $key"

        // Resolve {prefix}
        var resolved = template.replace("{prefix}", cache["prefix"] ?: "")

        // Resolve {currency.*} from plugin config
        resolved = resolved.replace("{currency.money}", getCfg("currency.money.singular", "Coin"))
        resolved = resolved.replace("{currency.money.plural}", getCfg("currency.money.plural", "Coins"))
        resolved = resolved.replace("{currency.gem}", getCfg("currency.gem.singular", "Gem"))
        resolved = resolved.replace("{currency.gem.plural}", getCfg("currency.gem.plural", "Gems"))

        // Substitute positional placeholders {0}, {1}, ...
        args.forEachIndexed { index, value ->
            resolved = resolved.replace("{$index}", value.toString())
        }

        return resolved
    }

    private fun getCfg(path: String, default: String): String =
        plugin.config.getString(path, default) ?: default

    /** Parse a message key into a MiniMessage [Component]. */
    fun parse(key: String, vararg args: Any): Component =
        miniMessage.deserialize(get(key, *args))

    /** Send a parsed message to a [CommandSender]. */
    fun send(sender: CommandSender, key: String, vararg args: Any) {
        sender.sendMessage(parse(key, *args))
    }

    /** Get all loaded keys (for debugging). */
    fun keys(): Set<String> = cache.keys.toSet()

    /** Check if a key exists. */
    fun has(key: String): Boolean = cache.containsKey(key)
}

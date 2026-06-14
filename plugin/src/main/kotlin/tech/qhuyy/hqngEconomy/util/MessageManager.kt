package tech.qhuyy.hqngEconomy.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads messages from messages.properties, caches them in memory,
 * and provides placeholder substitution via {0}, {1}, ...
 *
 * Usage:
 *   messageManager.get("balance.self", formatMoney(balance), currencyName)
 *   messageManager.send(player, "no-permission")
 *   messageManager.send(player, "eco.give.money", formatMoney(amount), name, formatMoney(newBal))
 */
class MessageManager(private val plugin: JavaPlugin) {

    private val miniMessage = MiniMessage.miniMessage()
    private val cache = ConcurrentHashMap<String, String>()
    private val properties = java.util.Properties()

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
        properties.clear()

        val file = plugin.dataFolder.resolve(resourcePath)

        if (!file.exists()) {
            // Copy default from jar to data folder on first run
            plugin.saveResource(resourcePath, false)
        }

        if (file.exists()) {
            file.inputStream().use { properties.load(it) }
        } else {
            // Last resort: read directly from jar
            plugin.getResource(resourcePath)?.use { properties.load(it) }
                ?: plugin.logger.warning("Could not find $resourcePath anywhere!")
        }

        // Populate cache
        properties.forEach { (key, value) ->
            cache[key.toString()] = value.toString()
        }

        plugin.logger.info("Loaded ${cache.size} message keys.")
    }

    /**
     * Get a raw message string by key, with optional placeholder args.
     * Placeholders: {0}, {1}, {2}, ...
     * If the key is missing, returns the key itself as fallback.
     */
    fun get(key: String, vararg args: Any): String {
        val template = cache[key] ?: return "<red>Missing key: $key"

        // Resolve {prefix} reference first
        var resolved = template.replace("{prefix}", cache["prefix"] ?: "")

        // Substitute positional placeholders
        args.forEachIndexed { index, value ->
            resolved = resolved.replace("{$index}", value.toString())
        }

        return resolved
    }

    /**
     * Parse a message key into a MiniMessage [Component].
     */
    fun parse(key: String, vararg args: Any): Component {
        return miniMessage.deserialize(get(key, *args))
    }

    /**
     * Send a parsed message to a [CommandSender].
     */
    fun send(sender: CommandSender, key: String, vararg args: Any) {
        sender.sendMessage(parse(key, *args))
    }

    /**
     * Get all loaded keys (for debugging).
     */
    fun keys(): Set<String> = cache.keys.toSet()

    /**
     * Check if a key exists.
     */
    fun has(key: String): Boolean = cache.containsKey(key)
}

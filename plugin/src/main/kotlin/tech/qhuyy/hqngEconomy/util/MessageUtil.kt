package tech.qhuyy.hqngEconomy.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.command.CommandSender

/**
 * Low-level MiniMessage utilities.
 * For plugin messages, prefer [MessageManager].
 */
object MessageUtil {

    private val miniMessage = MiniMessage.miniMessage()
    private val plain = PlainTextComponentSerializer.plainText()

    /** Parse a raw MiniMessage string into a Component. */
    fun parse(input: String): Component = miniMessage.deserialize(input)

    /** Send a raw MiniMessage string to a sender. */
    fun send(sender: CommandSender, message: String) {
        sender.sendMessage(parse(message))
    }

    /** Strip MiniMessage tags to get plain text. */
    fun strip(message: String): String = plain.serialize(parse(message))

    /** Send a title + subtitle to a player. */
    fun title(
        sender: CommandSender,
        title: String,
        subtitle: String = "",
        fadeIn: Int = 5,
        stay: Int = 40,
        fadeOut: Int = 10
    ) {
        val audience = sender as? org.bukkit.entity.Player ?: return
        audience.showTitle(
            net.kyori.adventure.title.Title.title(
                parse(title),
                parse(subtitle),
                net.kyori.adventure.title.Title.Times.times(
                    net.kyori.adventure.util.Ticks.duration(fadeIn.toLong()),
                    net.kyori.adventure.util.Ticks.duration(stay.toLong()),
                    net.kyori.adventure.util.Ticks.duration(fadeOut.toLong())
                )
            )
        )
    }
}

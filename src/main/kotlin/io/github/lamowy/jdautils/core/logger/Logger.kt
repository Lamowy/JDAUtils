package io.github.lamowy.jdautils.core.logger

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.shared.model.logger.LoggingLevel
import net.dv8tion.jda.api.EmbedBuilder
import kotlin.reflect.KClass

class Logger(
    val discordBot: DiscordBot,
    val clazz: Class<*>,
    channelId: String = discordBot.loggerConfig.channelId,
) {
    private val channel by lazy { discordBot.jda.getTextChannelById(channelId) ?: throw IllegalArgumentException("Channel $channelId does not exist!") }

    fun log(message: String, level: LoggingLevel = LoggingLevel.Info) {
        val time = System.currentTimeMillis()
        val formattedTime = "<t:${time / 1000}:F>"
        val formattedRelativeTime = "<t:${time / 1000}:R>"

        val description = when (level) {
            is LoggingLevel.Error ->
                message + "\n" +
                        "```" +
                        level.throwable.stackTrace.joinToString("\n") { it.toString() } +
                        "```"

            else -> message
        }

        val chunks = description.chunked(4090)

        chunks.forEachIndexed { index, chunk ->
            val embed = EmbedBuilder().apply {
                setTitle(
                    if (chunks.size > 1)
                        "Log (${index + 1}/${chunks.size})"
                    else
                        "Log"
                )

                setDescription(
                    if (chunks.indexOf(chunk) > 0) "```" else "" +
                    chunk +
                    if (chunks.size > 1) "\n```" else ""
                )

                setColor(
                    when (level) {
                        is LoggingLevel.Info -> 0xFFFFFF
                        is LoggingLevel.Warning -> 0xFFFF00
                        is LoggingLevel.Error -> 0xFF0000
                        is LoggingLevel.Debug -> 0x00FFFF
                    }
                )

                addField("Level", level.javaClass.simpleName.uppercase(), true)
                addField("Timestamp", "$formattedTime ($formattedRelativeTime)", true)
                addField("Source", clazz.simpleName, true)
            }.build()

            channel.sendMessage("")
                .addEmbeds(embed)
                .queue()
        }
    }
}
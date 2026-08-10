package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.core.command.factory.arguments.PrefixCommandArgumentsFactory
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class PrefixCommandContextFactory(
    private val discordBot: DiscordBot,
    private val event: MessageReceivedEvent
) : CommandContextFactory {
    data class ContentParts(
        val commandName: String,
        val rawCommandArguments: List<String>,
    )

    private fun checkSource(): Boolean {
        return !event.author.isBot
    }

    private fun checkPrefix(): Boolean {
        if (discordBot.commandPrefix == null) return false
        return event.message.contentRaw.startsWith(discordBot.commandPrefix)
    }

    private fun splitMessageIntoParts(): ContentParts {
        val rawParts = event.message.contentRaw.split(' ')
        return ContentParts(
            commandName = rawParts[0].removePrefix(discordBot.commandPrefix!!),
            rawCommandArguments = rawParts.drop(1),
        )
    }

    fun getCommand(commandName: String): Command? {
        return discordBot.getCommandByName(commandName)
    }

    private fun getArguments(command: Command, rawArguments: List<String>): CommandArguments {
        return PrefixCommandArgumentsFactory(discordBot, command, rawArguments).createArguments()
    }

    override fun createContext(): Command.Context? {
        if (!checkPrefix()) return null
        if (!checkSource()) return null

        val parts = splitMessageIntoParts()
        val command = getCommand(parts.commandName) ?: return null
        val arguments = getArguments(command, parts.rawCommandArguments)

        return Command.Context(
            command = command,
            guild = event.guild,
            channel = event.channel,
            type = Command.ContextType.PrefixContext(event.message),
            author = event.author,
            authorAsMember = event.member,
            arguments = arguments
        )
    }
}
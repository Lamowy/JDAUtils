package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.core.command.factory.arguments.SlashCommandArgumentsFactory
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SlashCommandContextFactory(
    private val discordBot: DiscordBot,
    private val event: SlashCommandInteractionEvent
) : CommandContextFactory {
    fun getCommand(commandName: String): Command? {
        return discordBot.getCommandByName(commandName)
    }

    private fun transformArguments(command: Command): CommandArguments {
        return SlashCommandArgumentsFactory(discordBot, command, event.options.associateWith { option ->
            command.arguments.first { it.name == option.name }
        }).createArguments()
    }

    override fun createContext(): Command.Context? {
        val command = getCommand(event.interaction.commandString) ?: return null

        return Command.Context(
            command = command,
            guild = event.guild,
            channel = event.channel,
            type = Command.ContextType.SlashContext(event),
            author = event.user,
            authorAsMember = event.member,
            arguments = transformArguments(command)
        )
    }
}
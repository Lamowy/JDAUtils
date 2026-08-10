package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction

class MessageContextContextFactory(
    val discordBot: DiscordBot,
    val messageContextInteraction: MessageContextInteraction
) : CommandContextFactory {
    fun getCommand(commandName: String): Command? {
        discordBot.getCommandByName(commandName)?.let { return it }
        return null
    }

    override fun createContext(): Command.Context? {
        val command = getCommand(messageContextInteraction.name) ?: return null

        return Command.Context(
            command = command,
            guild = messageContextInteraction.guild,
            channel = messageContextInteraction.messageChannel,
            type = Command.ContextType.MessageContext(messageContextInteraction),
            author = messageContextInteraction.user,
            authorAsMember = messageContextInteraction.member,
            arguments = CommandArguments(emptyMap()) // Message context commands do not have arguments
        )
    }
}
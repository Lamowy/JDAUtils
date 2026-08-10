package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction

class UserContextContextFactory(
    val discordBot: DiscordBot,
    val userContextInteraction: UserContextInteraction
) : CommandContextFactory {
    fun getCommand(commandName: String): Command? {
        discordBot.getCommandByName(commandName)?.let { return it }
        return null
    }

    override fun createContext(): Command.Context? {
        val command = getCommand(userContextInteraction.name) ?: return null

        return Command.Context(
            command = command,
            guild = userContextInteraction.guild,
            channel = userContextInteraction.messageChannel,
            type = Command.ContextType.UserContext(userContextInteraction),
            author = userContextInteraction.user,
            authorAsMember = userContextInteraction.member,
            arguments = CommandArguments(emptyMap()) // User context commands do not have arguments
        )
    }
}
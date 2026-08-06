package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.shared.model.CommandArguments
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class PrefixCommandContextFactory(
    private val prefix: String,
    private val commands: List<Command>,
    private val event: MessageReceivedEvent
) : CommandContextFactory {
    data class ContentParts(
        val commandName: String,
        val rawCommandArguments: List<String>,
    )

    fun checkPrefix(): Boolean {
        return event.message.contentRaw.startsWith(prefix)
    }

    fun splitMessageIntoParts(): ContentParts {
        val rawParts = event.message.contentRaw.split(' ')
        return ContentParts(
            commandName = rawParts[0],
            rawCommandArguments = rawParts.drop(1),
        )
    }

    fun getCommand(commandName: String): Command? {
        return commands.find { it.name == commandName }
    }

    fun getArguments(rawArguments: List<String>): CommandArguments {
        return CommandArguments(

        )
    }

    override fun createContext(): Command.Context? {
        if (!checkPrefix()) return null
        val parts = splitMessageIntoParts()
        val command = getCommand(parts.commandName) ?: return null

    }
}
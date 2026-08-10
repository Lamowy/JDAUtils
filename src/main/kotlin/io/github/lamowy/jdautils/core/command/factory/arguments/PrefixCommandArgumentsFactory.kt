package io.github.lamowy.jdautils.core.command.factory.arguments

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.exception.InvalidArgumentException
import io.github.lamowy.jdautils.shared.model.command.CommandArgument
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import io.github.lamowy.jdautils.shared.util.transformTimeArgument

class PrefixCommandArgumentsFactory(
    private val discordBot: DiscordBot,
    private val command: Command,
    private val rawArguments: List<String>
) : CommandArgumentsFactory {
    private fun resolveArgumentType(argumentIndex: Int): CommandArgument {
        return command.arguments[argumentIndex]
    }

    private fun transformMentionableArgumentId(rawArgument: String): Long {
        val mentionableIdRegex = Regex("<@!?&?([0-9]+)>")
        val matchResult = mentionableIdRegex.find(rawArgument)
        return matchResult?.groupValues?.get(1)?.toLong() ?: rawArgument.toLong()
    }

    private fun transformArgument(argument: CommandArgument, rawArgument: String): Any {
        return when (argument.type) {
            CommandArgument.Type.STRING -> rawArgument
            CommandArgument.Type.STRING_MULTILINE -> rawArgument
            CommandArgument.Type.INTEGER -> rawArgument.toLongOrNull() ?: throw InvalidArgumentException("Failed to convert $argument to long.")
            CommandArgument.Type.DOUBLE -> rawArgument.toDoubleOrNull() ?: throw InvalidArgumentException("Failed to convert $argument to double.")
            CommandArgument.Type.BOOLEAN -> rawArgument.toBooleanStrictOrNull() ?: throw InvalidArgumentException("Failed to convert $argument to boolean.")
            CommandArgument.Type.USER -> discordBot.jda.getUserById(transformMentionableArgumentId(rawArgument)) ?: throw InvalidArgumentException("User with ID $rawArgument not found")
            CommandArgument.Type.ROLE -> discordBot.jda.getRoleById(transformMentionableArgumentId(rawArgument)) ?: throw InvalidArgumentException("Role with ID $rawArgument not found")
            CommandArgument.Type.CHANNEL -> discordBot.jda.getTextChannelById(transformMentionableArgumentId(rawArgument)) ?: throw InvalidArgumentException("Channel with ID $rawArgument not found")
            CommandArgument.Type.MENTIONABLE -> {
                val id = transformMentionableArgumentId(rawArgument)
                discordBot.jda.getUserById(id) ?: discordBot.jda.getRoleById(id) ?: throw InvalidArgumentException("Mentionable with ID $rawArgument not found")
            }
            CommandArgument.Type.ATTACHMENT -> throw UnsupportedOperationException("Attachment type is not supported in prefix commands")
            CommandArgument.Type.TIME -> transformTimeArgument(rawArgument)
        }
    }

    override fun createArguments(): CommandArguments {
        if (rawArguments.size < command.arguments.size) {
            throw InvalidArgumentException("Not enough arguments provided. Expected ${command.arguments.size}, got ${rawArguments.size}.")
        }

        val arguments: MutableMap<String, Any> = mutableMapOf()
        for ((index, argument) in rawArguments.withIndex()) {
            val resolvedArgument = resolveArgumentType(index)
            if (resolvedArgument.type == CommandArgument.Type.STRING_MULTILINE) {
                arguments[resolvedArgument.name] = transformArgument(resolvedArgument, rawArguments.subList(index, rawArguments.size).joinToString(" "))
                break
            }
            arguments[resolvedArgument.name] = transformArgument(resolvedArgument, argument)
        }
        return CommandArguments(arguments)
    }
}
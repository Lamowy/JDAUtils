package io.github.lamowy.jdautils.core.command.factory.arguments

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.exception.InvalidArgumentException
import io.github.lamowy.jdautils.shared.model.command.CommandArgument
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import io.github.lamowy.jdautils.shared.util.transformTimeArgument
import net.dv8tion.jda.api.interactions.commands.OptionMapping

class SlashCommandArgumentsFactory(
    private val discordBot: DiscordBot,
    private val command: Command,
    private val arguments: Map<OptionMapping, CommandArgument>
) : CommandArgumentsFactory {
    private fun transformArgument(option: OptionMapping?, argument: CommandArgument): Any {
        if (option == null) throw InvalidArgumentException("Option for argument '${argument.name}' is null")

        return when (argument.type) {
            CommandArgument.Type.STRING -> safeTransform(argument, "string") {
                option.asString
            }

            CommandArgument.Type.STRING_MULTILINE -> safeTransform(argument, "multiline string") {
                option.asString
            }

            CommandArgument.Type.INTEGER -> safeTransform(argument, "integer") {
                option.asInt
            }

            CommandArgument.Type.DOUBLE -> safeTransform(argument, "double") {
                option.asDouble
            }

            CommandArgument.Type.BOOLEAN -> safeTransform(argument, "boolean") {
                option.asBoolean
            }

            CommandArgument.Type.USER -> safeTransform(argument, "user") {
                option.asUser
            }

            CommandArgument.Type.CHANNEL -> safeTransform(argument, "channel") {
                option.asChannel
            }

            CommandArgument.Type.ROLE -> safeTransform(argument, "role") {
                option.asRole
            }

            CommandArgument.Type.MENTIONABLE -> safeTransform(argument, "mentionable") {
                option.asMentionable
            }

            CommandArgument.Type.ATTACHMENT -> safeTransform(argument, "attachment") {
                option.asAttachment
            }

            CommandArgument.Type.TIME -> safeTransform(argument, "time") {
                transformTimeArgument(option.asString)
            }
        }
    }

    private inline fun <T> safeTransform(
        argument: CommandArgument,
        typeName: String,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            throw InvalidArgumentException(
                "Option for argument '${argument.name}' is not a valid $typeName"
            )
        }
    }

    override fun createArguments(): CommandArguments {
        val transformedArguments = arguments.map { (option, argument) ->
            argument.name to transformArgument(option, argument)
        }.toMap()

        return CommandArguments(transformedArguments)
    }
}
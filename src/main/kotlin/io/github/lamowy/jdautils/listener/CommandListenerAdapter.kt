package io.github.lamowy.jdautils.listener

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.core.command.factory.context.MessageContextContextFactory
import io.github.lamowy.jdautils.core.command.factory.context.PrefixCommandContextFactory
import io.github.lamowy.jdautils.core.command.factory.context.SlashCommandContextFactory
import io.github.lamowy.jdautils.core.command.factory.context.UserContextContextFactory
import io.github.lamowy.jdautils.core.command.validator.CommandPermissionsValidator
import io.github.lamowy.jdautils.core.logger.Logger
import io.github.lamowy.jdautils.exception.InvalidArgumentException
import io.github.lamowy.jdautils.shared.model.logger.LoggingLevel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandListenerAdapter(private val discordBot: DiscordBot) : ListenerAdapter() {
    val logger by lazy { Logger(discordBot, this::class.java) }

    private fun executeCommand(command: Command, context: Command.Context) {
        val result = try {
            command.execute(context)
        } catch (e: Exception) {
            logger.log("Exception occurred while executing command ${command.name} by ${context.author.name} (${context.author.id}). Exception: ${e.message}", LoggingLevel.Error(e))
            return
        }
        when (result) {
            is Command.Result.Success -> {
                logger.log("Command ${command.name} executed successfully by ${context.author.name} (${context.author.id}).")
            }
            is Command.Result.Failure -> {
                logger.log("Command ${command.name} execution failed by ${context.author.name} (${context.author.id}). Reason: ${result.reason}", LoggingLevel.Warning)
            }
            is Command.Result.ExecutionError -> {
                logger.log("Command ${command.name} execution error by ${context.author.name} (${context.author.id}). Exception: ${result.exception.message}", LoggingLevel.Error(result.exception))
            }
            is Command.Result.InsufficientPermissions -> {
                logger.log("Command ${command.name} execution failed due to insufficient permissions by ${context.author.name} (${context.author.id}). Missing permissions: ${result.missingPermissions.joinToString(", ")}", LoggingLevel.Warning)
            }
            is Command.Result.InvalidArguments -> {
                logger.log("Command ${command.name} execution failed due to invalid arguments by ${context.author.name} (${context.author.id}). Invalid arguments: ${result.arguments.map { "Position: ${it.key}, argument: ${it.value}" }.joinToString(", ")}", LoggingLevel.Warning)
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val factory = PrefixCommandContextFactory(discordBot, event)

        val command = factory.getCommand(event.message.contentRaw.split(" ")[0].removePrefix(discordBot.commandPrefix ?: "")) ?: return

        if (!validatePermissions(command, event.member, event.author) {
                event.message.reply(it).queue()
            }) {
            return
        }

        val context = try {
            factory.createContext()
        } catch (e: InvalidArgumentException) {
            event.message.replyInvalidArgument(e)
            return
        }

        if (context == null) return

        executeCommand(command, context)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val factory = SlashCommandContextFactory(discordBot, event)

        val command = factory.getCommand(event.interaction.commandString) ?: return

        if (!validatePermissions(command, event.member, event.user) {
                event.reply(it).queue()
            }) {
            return
        }

        val context = try {
            factory.createContext()
        } catch (e: InvalidArgumentException) {
            event.replyInvalidArgument(e)
            return
        }

        if (context == null) return

        executeCommand(command, context)
    }

    override fun onUserContextInteraction(event: UserContextInteractionEvent) {
        val factory = UserContextContextFactory(discordBot, event)

        val command = factory.getCommand(event.name) ?: return

        if (!validatePermissions(command, event.member, event.user) {
                event.reply(it).queue()
            }) {
            return
        }

        val context = factory.createContext() ?: return

        executeCommand(command, context)
    }

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        val factory = MessageContextContextFactory(discordBot, event)

        val command = factory.getCommand(event.name) ?: return

        if (!validatePermissions(command, event.member, event.user) {
                event.reply(it).queue()
            }) {
            return
        }

        val context = factory.createContext() ?: return

        executeCommand(command, context)
    }

    private fun validatePermissions(
        command: Command,
        member: Member?,
        user: User,
        reply: (String) -> Unit
    ): Boolean {
        return when (val result = CommandPermissionsValidator(discordBot, member, user).validateCommand(command)) {
            is Command.Result.InsufficientPermissions -> {
                reply("You don't have the required permissions to use this command.")
                false
            }

            is Command.Result.Failure -> {
                reply(result.reason)
                false
            }

            else -> true
        }
    }

    private fun MessageReceivedEvent.replyInvalidArgument(exception: InvalidArgumentException) {
        message.replyInvalidArgument(exception)
    }

    private fun net.dv8tion.jda.api.entities.Message.replyInvalidArgument(
        exception: InvalidArgumentException
    ) {
        reply(
            """
            Invalid argument
            ```
            ${exception.message}
            ```
            """.trimIndent()
        ).queue()
    }

    private fun SlashCommandInteractionEvent.replyInvalidArgument(
        exception: InvalidArgumentException
    ) {
        reply(
            """
            Invalid argument
            ```
            ${exception.message}
            ```
            """.trimIndent()
        ).queue()
    }
}
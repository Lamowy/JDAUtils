package io.github.lamowy.jdautils.core.command

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.extension.getLanguage
import io.github.lamowy.jdautils.shared.model.command.CommandArgument
import io.github.lamowy.jdautils.shared.model.command.CommandArguments
import io.github.lamowy.jdautils.shared.model.command.ReplyType
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction

abstract class Command @JvmOverloads constructor(
    val discordBot: DiscordBot,

    val name: String,
    val description: String,

    val isPrivate: Boolean = false,

    val arguments: List<CommandArgument> = emptyList(),
    val requiredPermissions: Set<Permission> = emptySet(),

    val environments: Set<Environment> = setOf(Environment.GUILD, Environment.DM),
    val registerTypes: Set<RegisterType> = setOf(RegisterType.SLASH, RegisterType.PREFIX)
) {
    init {
        require(arguments.size <= 25) { "Command cannot have more than 25 arguments" }
        require(registerTypes.isNotEmpty()) { "Command must have at least one register type" }
        require(environments.isNotEmpty()) { "Command must have at least one environment type" }
        require(!(isPrivate && registerTypes.contains(RegisterType.SLASH))) { "Command cannot be private and registered as a slash command" }
        require(!(isPrivate && registerTypes.contains(RegisterType.USER_CONTEXT))) { "Command cannot be private and registered as a user context command" }
        require(!(isPrivate && registerTypes.contains(RegisterType.MENU_CONTEXT))) { "Command cannot be private and registered as a menu context command" }
        require(!(requiredPermissions.isNotEmpty() && environments.contains(Environment.DM))) { "Command cannot have required permissions when executable on DMs." }

        require(!(isPrivate && requiredPermissions.isNotEmpty())) { "Command cannot be private and have required permissions" }
        require(!(registerTypes.contains(RegisterType.USER_CONTEXT) && arguments.isNotEmpty())) { "User context commands cannot have arguments" }
        require(!(registerTypes.contains(RegisterType.MENU_CONTEXT) && arguments.isNotEmpty())) { "Menu context commands cannot have arguments" }
        require(arguments.all { it.name.isNotBlank() }) { "Command arguments must have non-blank names" }

        val multilineIndex = arguments.indexOfFirst { it.type == CommandArgument.Type.STRING_MULTILINE }
        require(multilineIndex == -1 || multilineIndex == arguments.lastIndex) { "STRING_MULTILINE argument must be the last argument in the sequence" }
    }

    enum class RegisterType {
        USER_CONTEXT, MENU_CONTEXT, SLASH, PREFIX
    }

    enum class Environment {
        GUILD, DM
    }

    sealed interface ContextType {
        class PrefixContext(
            val message: Message
        ) : ContextType

        class SlashContext(
            val interaction: CommandInteraction
        ) : ContextType

        class UserContext(
            val interaction: UserContextInteraction
        ) : ContextType

        class MessageContext(
            val interaction: MessageContextInteraction
        ) : ContextType
    }

    class Context(
        val command: Command,
        val guild: Guild?,
        val channel: MessageChannel,
        val type: ContextType,
        val author: User,
        val authorAsMember: Member?,
        val arguments: CommandArguments
    ) {
        val isInGuild: Boolean
            get() = guild != null

        val isInDms: Boolean
            get() = guild == null

        val discordBot: DiscordBot
            get() = command.discordBot

        fun sendTranslatedMessage(key: String, placeholders: Map<String, String> = emptyMap()): MessageCreateAction {
            return sendMessage((command.discordBot.languagesManager.getLanguage(this.author, this.discordBot.userLanguagesManager)
                ?: command.discordBot.languagesManager.getLanguage(command.discordBot.languagesManager.mainLanguage))
                ?.getTranslation(key, placeholders)
                ?: key)
        }

        fun sendMessage(message: String): MessageCreateAction {
            return channel.sendMessage(message)
        }

        fun replyTranslated(key: String, placeholders: Map<String, String> = emptyMap(), replyType: ReplyType = ReplyType.Public): RestAction<*> {
            return reply((command.discordBot.languagesManager.getLanguage(this.author, this.discordBot.userLanguagesManager)
                ?: command.discordBot.languagesManager.getLanguage(command.discordBot.languagesManager.mainLanguage))
                ?.getTranslation(key, placeholders)
                ?: key)
        }

        fun reply(message: String, replyType: ReplyType = ReplyType.Public): RestAction<*> {
            return when (type) {
                is ContextType.PrefixContext -> {
                    type.message.reply(message)
                }
                is ContextType.SlashContext -> {
                    type.interaction.reply(message).setEphemeral(replyType is ReplyType.Ephemeral)
                }
                is ContextType.MessageContext -> {
                    type.interaction.reply(message).setEphemeral(replyType is ReplyType.Ephemeral)
                }
                is ContextType.UserContext -> {
                    type.interaction.reply(message).setEphemeral(replyType is ReplyType.Ephemeral)
                }
            }
        }
    }

    sealed interface Result {
        object Success : Result
        data class Failure(val reason: String) : Result
        data class ExecutionError(val exception: Exception) : Result
        data class InvalidArguments(val arguments: Map<Int, String>) : Result
        data class InsufficientPermissions(val missingPermissions: List<Permission>) : Result
    }

    abstract fun execute(context: Context): Result
}
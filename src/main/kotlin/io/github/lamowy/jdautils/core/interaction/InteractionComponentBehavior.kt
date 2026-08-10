package io.github.lamowy.jdautils.core.interaction

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.extension.getLanguage
import io.github.lamowy.jdautils.shared.model.command.ReplyType
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

abstract class InteractionComponentBehavior<E : GenericInteractionCreateEvent>(
    val discordBot: DiscordBot,
    val componentId: String
) {
    inner class Context (
        val event: E,
        val componentId: String,
        val arguments: List<String>,
        val componentAuthor: User
    ) {
        fun reply(message: String, replyType: ReplyType = ReplyType.Public): ReplyCallbackAction {
            return when (event) {
                is GenericComponentInteractionCreateEvent -> {
                    event.reply(message).setEphemeral(replyType is ReplyType.Ephemeral)
                }
                is ModalInteractionEvent -> {
                    event.reply(message).setEphemeral(replyType is ReplyType.Ephemeral)
                }
                else -> { throw ClassCastException("Unknown Interaction Event Type") }
            }
        }

        fun replyTranslated(key: String, placeholders: Map<String, String> = emptyMap(), replyType: ReplyType = ReplyType.Public): ReplyCallbackAction {
            val languagesManager = discordBot.languagesManager
            return reply((languagesManager.getLanguage(this.event.interaction.user, discordBot.userLanguagesManager)
                ?: languagesManager.getLanguage(languagesManager.mainLanguage))
                ?.getTranslation(key, placeholders)
                ?: key)
        }
    }

    sealed interface Result {
        object Success : Result
        data class Failure(val error: String) : Result
        data class Error(val error: Exception) : Result
        data class InsufficientPermissions(val permissions: Set<Permission>) : Result
        data class InvalidArguments(val arguments: List<String>) : Result
        data class Unauthorized(val user: User) : Result
    }

    fun authenticateUser(context: Context): Boolean {
        return context.event.interaction.user == context.componentAuthor
    }

    abstract fun onInteraction(context: Context): Result
}
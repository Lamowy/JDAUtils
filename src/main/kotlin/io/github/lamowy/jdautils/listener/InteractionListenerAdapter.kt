package io.github.lamowy.jdautils.listener

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.interaction.InteractionComponentBehavior
import io.github.lamowy.jdautils.core.logger.Logger
import io.github.lamowy.jdautils.shared.data.ComponentParts
import io.github.lamowy.jdautils.shared.model.logger.LoggingLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class InteractionListenerAdapter(
    val discordBot: DiscordBot
) : ListenerAdapter() {
    val logger by lazy { Logger(discordBot, this::class.java) }

    private fun toComponentParts(jda: JDA, componentId: String): ComponentParts {
        val regex = Regex("^([^:;]+)(?::([^;]*))?@([^:;]+)\$")
        val match = regex.matchEntire(componentId)
            ?: throw IllegalArgumentException("Invalid component ID format: $componentId")

        val id = match.groupValues[1]
        val arguments = match.groupValues[2].split(":").filter { it.isNotEmpty() }
        val userId = match.groupValues[3]

        val user = jda.getUserById(userId)
            ?: jda.retrieveUserById(userId)
                .complete()

        return ComponentParts(id, arguments, user)
    }

    private fun <T : GenericInteractionCreateEvent> onInteraction(behavior: InteractionComponentBehavior<T>?, context: InteractionComponentBehavior<T>.Context) {
        val result = try {
            behavior?.onInteraction(context)
        } catch (e: Throwable) {
            logger.log(
                "Exception occurred while handling interaction invoked by " +
                        "${context.componentAuthor.name} (${context.componentAuthor.id}) " +
                        "for component ID: ${context.componentId}. Exception: ${e.message}",
                LoggingLevel.Error(e)
            )

            return
        }

        when (result) {
            is InteractionComponentBehavior.Result.Success -> logger.log("Interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) handled successfully for component ID: ${context.componentId}")
            is InteractionComponentBehavior.Result.Failure -> logger.log("Interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) failed for component ID: ${context.componentId}. Error: ${result.error}", LoggingLevel.Warning)
            is InteractionComponentBehavior.Result.Error -> logger.log("Error occurred while handling interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) for component ID: ${context.componentId}", LoggingLevel.Error(result.error))
            is InteractionComponentBehavior.Result.InsufficientPermissions -> logger.log("Insufficient permissions for interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) on component ID: ${context.componentId}. Required permissions: ${result.permissions}", LoggingLevel.Warning)
            is InteractionComponentBehavior.Result.InvalidArguments -> logger.log("Invalid arguments for interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) on component ID: ${context.componentId}. Arguments: ${result.arguments}", LoggingLevel.Warning)
            is InteractionComponentBehavior.Result.Unauthorized -> logger.log("Unauthorized user attempted interaction invoked by ${context.componentAuthor.name} (${context.componentAuthor.id}) on component ID: ${context.componentId}. User: ${result.user}", LoggingLevel.Warning)
            else -> {}
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val componentParts = toComponentParts(event.jda, event.componentId)
        val behavior = discordBot.getButtonBehaviorByComponentId(componentParts.id)

        onInteraction(behavior, behavior?.Context(event, componentParts.id, componentParts.arguments, componentParts.user) ?: return)
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val componentParts = toComponentParts(event.jda, event.modalId)
        val behavior = discordBot.getModalBehaviorByComponentId(componentParts.id)

        onInteraction(behavior, behavior?.Context(event, componentParts.id, componentParts.arguments, componentParts.user) ?: return)
    }

    override fun onEntitySelectInteraction(event: EntitySelectInteractionEvent) {
        val componentParts = toComponentParts(event.jda, event.componentId)
        val behavior = discordBot.getEntitySelectBehaviorByComponentId(componentParts.id)

        onInteraction(behavior, behavior?.Context(event, componentParts.id, componentParts.arguments, componentParts.user) ?: return)
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        val componentParts = toComponentParts(event.jda, event.componentId)
        val behavior = discordBot.getStringSelectBehaviorByComponentId(componentParts.id)

        onInteraction(behavior, behavior?.Context(event, componentParts.id, componentParts.arguments, componentParts.user) ?: return)
    }
}
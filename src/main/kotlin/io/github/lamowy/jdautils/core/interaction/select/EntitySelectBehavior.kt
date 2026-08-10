package io.github.lamowy.jdautils.core.interaction.select

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.interaction.InteractionComponentBehavior
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent

abstract class EntitySelectBehavior(discordBot: DiscordBot, componentId: String) : InteractionComponentBehavior<EntitySelectInteractionEvent>(discordBot, componentId) {
    abstract override fun onInteraction(context: Context): Result
}
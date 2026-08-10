package io.github.lamowy.jdautils.core.interaction.modal

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.interaction.InteractionComponentBehavior
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

abstract class ModalBehavior(discordBot: DiscordBot, componentId: String) : InteractionComponentBehavior<ModalInteractionEvent>(discordBot, componentId) {
     abstract override fun onInteraction(context: Context): Result
}
package io.github.lamowy.jdautils.core.interaction.button

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.interaction.InteractionComponentBehavior
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

abstract class ButtonBehavior(discordBot: DiscordBot, componentId: String) : InteractionComponentBehavior<ButtonInteractionEvent>(discordBot, componentId) {
    abstract override fun onInteraction(context: Context): Result
}

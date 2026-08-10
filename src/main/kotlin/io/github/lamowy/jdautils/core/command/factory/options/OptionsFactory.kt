package io.github.lamowy.jdautils.core.command.factory.options

import net.dv8tion.jda.api.interactions.commands.build.OptionData

interface OptionsFactory {
    fun createOptions(): List<OptionData>
}
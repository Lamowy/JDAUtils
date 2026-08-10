package io.github.lamowy.jdautils.core.command.factory.options

import io.github.lamowy.jdautils.shared.model.command.CommandArgument
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DefaultOptionsFactory(
    private val arguments: List<CommandArgument>
) : OptionsFactory {
    private fun convertArgumentTypeToOptionType(arg: CommandArgument): OptionType {
        return when (arg.type) {
            CommandArgument.Type.STRING -> OptionType.STRING
            CommandArgument.Type.STRING_MULTILINE -> OptionType.STRING
            CommandArgument.Type.INTEGER -> OptionType.INTEGER
            CommandArgument.Type.DOUBLE -> OptionType.NUMBER
            CommandArgument.Type.BOOLEAN -> OptionType.BOOLEAN
            CommandArgument.Type.USER -> OptionType.USER
            CommandArgument.Type.CHANNEL -> OptionType.CHANNEL
            CommandArgument.Type.ROLE -> OptionType.ROLE
            CommandArgument.Type.MENTIONABLE -> OptionType.MENTIONABLE
            CommandArgument.Type.ATTACHMENT -> OptionType.ATTACHMENT
            CommandArgument.Type.TIME -> OptionType.STRING
        }
    }

    override fun createOptions(): List<OptionData> {
        val list: MutableList<OptionData> = mutableListOf()
        for (arg in arguments) {
            list += OptionData(
                convertArgumentTypeToOptionType(arg),
                arg.name,
                arg.description,
                arg.required
            )
        }
        return list
    }
}
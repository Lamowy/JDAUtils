package io.github.lamowy.jdautils.core.command.validator

import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.core.command.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class CommandPermissionsValidator(
    private val discordBot: DiscordBot,
    private val member: Member?,
    private val user: User,
) : CommandValidator {

    override fun validateCommand(command: Command): Command.Result {
        checkPermissions(command)?.let { return it }

        return Command.Result.Success
    }

    private fun checkPermissions(command: Command): Command.Result? {
        if (command.isPrivate) {
            if (user.id !in discordBot.ownersIDs) return Command.Result.Failure("The command is only available for bot owners.")
        }

        val required = command.requiredPermissions
        if (required.isEmpty()) return null

        val m = member ?: return Command.Result.Failure("Command requires permissions but no member available")

        val missing = required.filterNot { m.hasPermission(it) }
        return if (missing.isEmpty()) null else Command.Result.InsufficientPermissions(missing)
    }
}

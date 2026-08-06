package io.github.lamowy.jdautils.core.command.validator

import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.shared.model.CommandArgument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class DefaultCommandValidator(
    private val member: Member?,
    private val user: User,
    private val providedArguments: List<String>
) : CommandValidator {

    override fun validateCommand(command: Command): Command.Result {
        // 1. permissions
        checkPermissions(command)?.let { return it }

        // 2. arguments
        validateArguments(command)?.let { return it }

        return Command.Result.Success
    }

    private fun checkPermissions(command: Command): Command.Result? {
        val required = command.requiredPermissions
        if (required.isEmpty()) return null

        val m = member ?: return Command.Result.Failure("Command requires permissions but no member available")

        val missing = required.filterNot { m.hasPermission(it) }
        return if (missing.isEmpty()) null else Command.Result.InsufficientPermissions(missing)
    }

    private fun validateArguments(command: Command): Command.Result? {
        val defs = command.arguments
        val provided = providedArguments

        // quick pass - no definitions
        if (defs.isEmpty()) {
            if (provided.isNotEmpty()) return Command.Result.InvalidArguments(mapOf(-1 to "Unexpected arguments provided"))
            return null
        }

        val errors = mutableMapOf<Int, String>()

        for (i in defs.indices) {
            val def = defs[i]

            if (def.type == CommandArgument.Type.STRING_MULTILINE) {
                // must be last by Command init; consume the rest of provided arguments
                val remaining = provided.size - i
                if (def.required && remaining <= 0) {
                    errors[i] = "Missing multiline argument: ${def.name}"
                }
                break
            }

            if (i >= provided.size) {
                if (def.required) errors[i] = "Missing required argument: ${def.name}"
                continue
            }

            val value = provided[i]
            if (value.isBlank()) {
                if (def.required) errors[i] = "Argument ${def.name} cannot be blank"
                continue
            }

            when (def.type) {
                CommandArgument.Type.STRING -> { /* always ok */ }
                CommandArgument.Type.INTEGER -> if (value.toIntOrNull() == null) errors[i] = "Argument ${def.name} must be an integer"
                CommandArgument.Type.DOUBLE -> if (value.toDoubleOrNull() == null) errors[i] = "Argument ${def.name} must be a number"
                CommandArgument.Type.BOOLEAN -> if (!(value.equals("true", true) || value.equals("false", true))) errors[i] = "Argument ${def.name} must be true or false"
                CommandArgument.Type.USER,
                CommandArgument.Type.ROLE,
                CommandArgument.Type.CHANNEL,
                CommandArgument.Type.MENTIONABLE,
                CommandArgument.Type.ATTACHMENT,
                CommandArgument.Type.TIME -> { /* perform basic non-blank check already done */ }
                else -> { /* no-op for unknown types */ }
            }
        }

        // extra provided args when no multiline to consume them
        val lastDef = defs.last()
        if (lastDef.type != CommandArgument.Type.STRING_MULTILINE && provided.size > defs.size) {
            for (j in defs.size until provided.size) {
                errors[j] = "Unexpected argument at position $j"
            }
        }

        return if (errors.isEmpty()) null else Command.Result.InvalidArguments(errors)
    }
}

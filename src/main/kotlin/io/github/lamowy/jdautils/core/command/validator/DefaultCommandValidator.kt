package io.github.lamowy.jdautils.core.command.validator.command

import io.github.lamowy.jdautils.core.command.Command

class DefaultCommandValidator(
    private val
) : CommandValidator {
    override fun validateCommand(command: Command): Command.Result {

        return Command.Result.Success
    }
}
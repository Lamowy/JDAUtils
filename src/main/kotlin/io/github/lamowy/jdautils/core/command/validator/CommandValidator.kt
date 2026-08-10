package io.github.lamowy.jdautils.core.command.validator

import io.github.lamowy.jdautils.core.command.Command

interface CommandValidator {
    fun validateCommand(command: Command): Command.Result
}
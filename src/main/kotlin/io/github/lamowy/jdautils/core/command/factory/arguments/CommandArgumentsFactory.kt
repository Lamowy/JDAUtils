package io.github.lamowy.jdautils.core.command.factory.arguments

import io.github.lamowy.jdautils.shared.model.command.CommandArguments

interface CommandArgumentsFactory {
    fun createArguments(): CommandArguments
}
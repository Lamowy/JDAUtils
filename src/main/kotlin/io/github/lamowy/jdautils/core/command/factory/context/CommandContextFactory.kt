package io.github.lamowy.jdautils.core.command.factory.context

import io.github.lamowy.jdautils.core.command.Command

interface CommandContextFactory {
    fun createContext(): Command.Context?
}
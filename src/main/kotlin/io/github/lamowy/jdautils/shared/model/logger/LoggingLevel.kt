package io.github.lamowy.jdautils.shared.model.logger

sealed interface LoggingLevel {
    object Info : LoggingLevel
    object Warning : LoggingLevel
    data class Error(val throwable: Throwable) : LoggingLevel
    object Debug : LoggingLevel
}
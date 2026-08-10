package io.github.lamowy.jdautils.shared.model.command

sealed interface ReplyType {
    object Public : ReplyType
    data class Ephemeral(val options: EphemeralCommandOptions = EphemeralCommandOptions()) : ReplyType
}
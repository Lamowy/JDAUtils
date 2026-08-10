package io.github.lamowy.jdautils.shared.model.command

class CommandArgument(
    val name: String,
    val description: String,
    val type: Type,
    val required: Boolean = true
) {
    enum class Type {
        STRING, STRING_MULTILINE, INTEGER, DOUBLE, BOOLEAN, USER, ROLE, CHANNEL, MENTIONABLE, ATTACHMENT, TIME
    }
}
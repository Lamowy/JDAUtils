package io.github.lamowy.jdautils.shared.model.command

data class CommandArguments(
    private val arguments: Map<String, Any>
) {
    fun <T> get(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return arguments[key] as T
    }
}
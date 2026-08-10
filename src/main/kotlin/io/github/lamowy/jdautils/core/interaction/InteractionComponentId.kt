package io.github.lamowy.jdautils.core.interaction

import net.dv8tion.jda.api.entities.User

data class InteractionComponentId(
    val id: String,
    val arguments: List<String> = emptyList(),
    val authorId: String? = null,
) {
    constructor(id: String, arguments: List<String> = emptyList(), author: User) : this(id, arguments, author.id)

    override fun toString(): String {
        return "$id:${arguments.joinToString(":")}" +
            if (authorId != null) "@$authorId" else ""
    }
}
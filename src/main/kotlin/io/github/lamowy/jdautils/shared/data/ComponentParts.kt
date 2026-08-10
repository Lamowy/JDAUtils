package io.github.lamowy.jdautils.shared.data

import net.dv8tion.jda.api.entities.User

data class ComponentParts(
    val id: String,
    val arguments: List<String>,
    val user: User
)
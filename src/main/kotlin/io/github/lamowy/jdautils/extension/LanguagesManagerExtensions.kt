package io.github.lamowy.jdautils.extension

import io.github.lamowy.jdautils.core.language.UserLanguagesManager
import io.github.lamowy.langutils.core.manager.LanguagesManager
import io.github.lamowy.langutils.shared.model.Language
import net.dv8tion.jda.api.entities.User

fun LanguagesManager.getLanguage(user: User, userLanguagesManager: UserLanguagesManager): Language? {
    return this.getLanguage(userLanguagesManager[user])
}

@JvmSynthetic
operator fun LanguagesManager.invoke(user: User, userLanguagesManager: UserLanguagesManager): Language? {
    return this.getLanguage(userLanguagesManager[user])
}
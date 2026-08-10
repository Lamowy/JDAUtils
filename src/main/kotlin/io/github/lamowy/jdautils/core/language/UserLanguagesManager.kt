package io.github.lamowy.jdautils.core.language

import io.github.lamowy.fileutils.core.filemanager.FileManager
import io.github.lamowy.fileutils.core.storage.FileStorage
import io.github.lamowy.fileutils.format.json.storage.jsonFileStorage
import net.dv8tion.jda.api.entities.User

class UserLanguagesManager(
    private val fileManager: FileManager,
    private val defaultLanguage: String,
    private val storage: FileStorage<String, String> = jsonFileStorage(fileManager.dataFile("user_languages"), fileSystem = fileManager.fileSystem)
) {
    val lock: Any = Any()

    operator fun get(key: String): String = synchronized(lock) {
        return storage[key] ?: run {
            storage[key] = defaultLanguage
            return@synchronized defaultLanguage
        }
    }

    operator fun set(key: String, value: String) = synchronized(lock) {
        storage[key] = value
    }

    operator fun get(key: User): String {
        return get(key.id)
    }

    operator fun set(key: User, value: String) {
        set(key.id, value)
    }
}
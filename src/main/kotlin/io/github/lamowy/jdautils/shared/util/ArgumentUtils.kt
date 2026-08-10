package io.github.lamowy.jdautils.shared.util

import io.github.lamowy.jdautils.exception.InvalidArgumentException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun transformTimeArgument(rawArgument: String): Duration {
    val timeRegex = Regex("(\\d+)([smhd])")
    val matchResult = timeRegex.find(rawArgument)
    if (matchResult != null) {
        val (value, unit) = matchResult.destructured
        return when (unit) {
            "s" -> value.toLong().seconds
            "m" -> value.toLong().minutes
            "h" -> value.toLong().hours
            "d" -> value.toLong().days
            else -> throw InvalidArgumentException("Invalid time unit: $unit")
        }
    } else {
        throw InvalidArgumentException("Invalid time format: $rawArgument")
    }
}
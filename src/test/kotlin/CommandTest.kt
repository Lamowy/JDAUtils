package io.github.lamowy.jdautils

import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.shared.model.CommandArgument
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

class CommandTest {

    @Test
    fun multilineNotLastThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            object : Command(
                name = "test",
                description = "desc",
                arguments = listOf(
                    CommandArgument("first", "desc", CommandArgument.Type.STRING_MULTILINE),
                    CommandArgument("second", "desc", CommandArgument.Type.STRING)
                )
            ) {
                override fun execute(context: Command.Context): Command.Result = Command.Result.Success
            }
        }
    }

    @Test
    fun multilineLastAllowed() {
        assertDoesNotThrow {
            object : Command(
                name = "test",
                description = "desc",
                arguments = listOf(
                    CommandArgument("first", "desc", CommandArgument.Type.STRING),
                    CommandArgument("last", "desc", CommandArgument.Type.STRING_MULTILINE)
                )
            ) {
                override fun execute(context: Command.Context): Command.Result = Command.Result.Success
            }
        }
    }

    @Test
    fun noMultilineAllowed() {
        assertDoesNotThrow {
            object : Command(
                name = "test",
                description = "desc",
                arguments = listOf(
                    CommandArgument("a", "desc", CommandArgument.Type.STRING),
                    CommandArgument("b", "desc", CommandArgument.Type.INTEGER)
                )
            ) {
                override fun execute(context: Command.Context): Command.Result = Command.Result.Success
            }
        }
    }
}

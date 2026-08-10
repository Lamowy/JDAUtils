import io.github.lamowy.jdautils.DiscordBot
import io.github.lamowy.jdautils.config.LanguagesManagerConfig
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.core.interaction.InteractionComponentBehavior
import io.github.lamowy.jdautils.core.interaction.button.ButtonBehavior
import io.github.lamowy.jdautils.config.LoggerConfig
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import kotlin.time.Duration.Companion.milliseconds

class TestBot : DiscordBot("", LoggerConfig("1535240476473561088"), LanguagesManagerConfig("hu_hu"), commandPrefix = ".")

suspend fun main() {
    val bot = TestBot()

    bot.registerCommand(
        object : Command(
            discordBot = bot,
            name = "ping",
            description = "description",
            registerTypes = setOf(RegisterType.USER_CONTEXT, RegisterType.MENU_CONTEXT, RegisterType.PREFIX,
                RegisterType.SLASH),
            environments = setOf(Command.Environment.GUILD),
            requiredPermissions = setOf(Permission.MESSAGE_SEND),
        ) {
            override fun execute(context: Context): Result {
                context.sendMessage("Pong! ${context.arguments.get<String>("first")}")
                    .addComponents(
                        ActionRow.of(
                            Button.primary("test@${context.author.id}", "Test button")
                        )
                    )
                    .queue()
                context.sendTranslatedMessage("hello")
                    .queue()
                context.sendMessage("")
                    .queue()
                return Result.Success
            }
        }
    )

    bot.registerButtonBehavior(
        object : ButtonBehavior(bot, "test") {
            override fun onInteraction(context: InteractionComponentBehavior<ButtonInteractionEvent>.Context): Result {
                context.event.reply("").queue()
                context.event.reply("Button clicked!").queue()
                return Result.Success
            }
        }
    )

    bot.run()

    delay(3000.milliseconds)

    bot.jda.presence.setPresence(net.dv8tion.jda.api.OnlineStatus.ONLINE, Activity.playing("Botutils v3.0 tests..."))
}
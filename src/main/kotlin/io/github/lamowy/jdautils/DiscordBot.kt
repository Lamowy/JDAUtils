package io.github.lamowy.jdautils

import io.github.lamowy.fileutils.core.filemanager.DataLocation
import io.github.lamowy.fileutils.core.filemanager.FileManager
import io.github.lamowy.jdautils.config.LanguagesManagerConfig
import io.github.lamowy.jdautils.core.command.Command
import io.github.lamowy.jdautils.core.command.factory.options.DefaultOptionsFactory
import io.github.lamowy.jdautils.core.interaction.button.ButtonBehavior
import io.github.lamowy.jdautils.core.interaction.modal.ModalBehavior
import io.github.lamowy.jdautils.core.interaction.select.EntitySelectBehavior
import io.github.lamowy.jdautils.core.interaction.select.StringSelectBehavior
import io.github.lamowy.jdautils.core.logger.Logger
import io.github.lamowy.jdautils.listener.CommandListenerAdapter
import io.github.lamowy.jdautils.listener.InteractionListenerAdapter
import io.github.lamowy.jdautils.config.LoggerConfig
import io.github.lamowy.jdautils.core.language.UserLanguagesManager
import io.github.lamowy.langutils.core.manager.LanguagesManager
import io.github.lamowy.langutils.core.manager.YamlLanguagesManager
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.time.Duration.Companion.milliseconds

abstract class DiscordBot @JvmOverloads constructor(
    private val token: String,
    val loggerConfig: LoggerConfig,
    val languagesManagerConfig: LanguagesManagerConfig = LanguagesManagerConfig("en_us"),
    val fileManager: FileManager = FileManager(DataLocation.ProjectFolder),
    val commandPrefix: String? = null,
    val ownersIDs: Set<String> = emptySet(),
    val intents: List<GatewayIntent> = listOf(
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT,
        GatewayIntent.GUILD_MEMBERS
    ),
    val jdaBuilder: JDABuilder = JDABuilder.createDefault(token)
) {
    lateinit var jda: JDA
    private set

    lateinit var logger: Logger
    private set

    lateinit var languagesManager: LanguagesManager
    private set

    lateinit var userLanguagesManager: UserLanguagesManager
    private set

    private val listeners = mutableListOf<ListenerAdapter>()
    private val commands: MutableList<Command> = mutableListOf()

    private val buttonBehaviors = mutableListOf<ButtonBehavior>()
    private val stringSelectBehaviors = mutableListOf<StringSelectBehavior>()
    private val entitySelectBehaviors = mutableListOf<EntitySelectBehavior>()
    private val modalBehaviors = mutableListOf<ModalBehavior>()

    fun registerListenerAdapter(listener: ListenerAdapter) {
        listeners += listener
    }

    fun registerCommand(command: Command) {
        commands += command
    }

    fun registerButtonBehavior(buttonBehavior: ButtonBehavior) {
        buttonBehaviors += buttonBehavior
    }

    fun registerStringSelectBehavior(stringSelectBehavior: StringSelectBehavior) {
        stringSelectBehaviors += stringSelectBehavior
    }

    fun registerEntitySelectBehavior(entitySelectBehavior: EntitySelectBehavior) {
        entitySelectBehaviors += entitySelectBehavior
    }

    fun registerModalBehavior(modalBehavior: ModalBehavior) {
        modalBehaviors += modalBehavior
    }

    fun getCommandByName(commandName: String): Command? {
        return commands.firstOrNull { it.name == commandName }
    }

    fun getButtonBehaviorByComponentId(componentId: String): ButtonBehavior? {
        return buttonBehaviors.firstOrNull { it.componentId == componentId }
    }

    fun getStringSelectBehaviorByComponentId(componentId: String): StringSelectBehavior? {
        return stringSelectBehaviors.firstOrNull { it.componentId == componentId }
    }

    fun getEntitySelectBehaviorByComponentId(componentId: String): EntitySelectBehavior? {
        return entitySelectBehaviors.firstOrNull { it.componentId == componentId }
    }

    fun getModalBehaviorByComponentId(componentId: String): ModalBehavior? {
        return modalBehaviors.firstOrNull { it.componentId == componentId }
    }

    protected fun registerCommands() {
        val commandBuilder = jda.updateCommands()

        val slashCommands = commands
            .filter { Command.RegisterType.SLASH in it.registerTypes }

        val groupedSlashCommands = slashCommands.groupBy {
            it.name.substringBefore(" ")
        }

        for ((commandName, commandGroup) in groupedSlashCommands) {
            val hasSubcommands = commandGroup.any { it.name.contains(" ") }

            if (hasSubcommands) {
                val parentCommand = commandGroup
                    .firstOrNull { it.name == commandName }

                val parentDescription = parentCommand?.description
                    ?: commandGroup.first().description

                val subcommands = commandGroup
                    .filter { it.name.contains(" ") }
                    .map { command ->
                        val subcommandName = command.name.substringAfter(" ")

                        logger.log(
                            "Registering slash subcommand: /$commandName $subcommandName"
                        )

                        SubcommandData(
                            subcommandName,
                            command.description
                        ).addOptions(
                            DefaultOptionsFactory(command.arguments).createOptions()
                        )
                    }

                logger.log("Registering slash command: /$commandName")

                commandBuilder.addCommands(
                    Commands.slash(commandName, parentDescription)
                        .addSubcommands(subcommands)
                        .setIntegrationTypes(IntegrationType.ALL)
                )
            } else {
                val command = commandGroup.first()

                logger.log("Registering slash command: /${command.name}")

                commandBuilder.addCommands(
                    Commands.slash(command.name, command.description)
                        .addOptions(
                            DefaultOptionsFactory(command.arguments).createOptions()
                        )
                        .setIntegrationTypes(IntegrationType.ALL)
                )
            }
        }

        commands
            .filter { Command.RegisterType.MENU_CONTEXT in it.registerTypes }
            .forEach { command ->
                logger.log("Registering menu context command: ${command.name}")

                commandBuilder.addCommands(
                    Commands.message(command.name)
                )
            }

        commands
            .filter { Command.RegisterType.USER_CONTEXT in it.registerTypes }
            .forEach { command ->
                logger.log("Registering user context command: ${command.name}")

                commandBuilder.addCommands(
                    Commands.user(command.name)
                )
            }

        commandBuilder.queue()
    }

    suspend fun run() {
        languagesManager = YamlLanguagesManager.fromResources("languages/", languagesManagerConfig.mainLanguage)
        userLanguagesManager = UserLanguagesManager(fileManager, languagesManagerConfig.mainLanguage)

        jda = jdaBuilder
            .setToken(token)
            .enableIntents(intents)
            .addEventListeners(CommandListenerAdapter(this))
            .addEventListeners(InteractionListenerAdapter(this))
            .addEventListeners(*listeners.toTypedArray())
            .build()

        delay(1500.milliseconds)

        logger = Logger(this, this::class.java, loggerConfig.channelId)
        logger.log("Initializing Bot...")

        registerCommands()

        logger.log("Bot initialized successfully!")

        listeners.clear()
    }
}
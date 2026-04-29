package ru.zelginni.tinycerberusbot.telegram

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.bot.BotAdministrationService
import ru.zelginni.tinycerberusbot.bot.BotState
import ru.zelginni.tinycerberusbot.telegram.command.CommandHandler
import ru.zelginni.tinycerberusbot.telegram.command.CommandParser
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class DefaultTelegramUpdateHandler(
    private val administrationService: BotAdministrationService,
    private val commandParser: CommandParser,
    private val commandHandlers: List<CommandHandler>,
    private val messageSender: TelegramCommandSender,
) : TelegramUpdateHandler {
    override fun handleNewChatMembers(members: IncomingChatMembers) {

    }

    override fun handleMessage(message: IncomingChatMessage) {
        if (administrationService.currentState() != BotState.ENABLED) {
            return
        }

        val command = commandParser.parse(message) ?: return
        logger.info("Received Telegram command '{}' in chat {}", command.command, command.chatId)

        commandHandlers.firstOrNull { it.supports(command) }?.handle(command)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultTelegramUpdateHandler::class.java)
    }
}

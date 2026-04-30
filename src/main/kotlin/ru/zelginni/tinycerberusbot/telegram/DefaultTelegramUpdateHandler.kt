package ru.zelginni.tinycerberusbot.telegram

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.bayan.BayanMessageHandler
import ru.zelginni.tinycerberusbot.rules.NewChatMembersGreetingService
import ru.zelginni.tinycerberusbot.statistics.MessageStatisticsService
import ru.zelginni.tinycerberusbot.telegram.command.CommandHandler
import ru.zelginni.tinycerberusbot.telegram.command.CommandParser
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberRegistry
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class DefaultTelegramUpdateHandler(
    private val bayanMessageHandler: BayanMessageHandler,
    private val newChatMembersGreetingService: NewChatMembersGreetingService,
    private val statisticsService: MessageStatisticsService,
    private val chatMemberRegistry: TelegramChatMemberRegistry,
    private val commandParser: CommandParser,
    private val commandHandlers: List<CommandHandler>,
    private val messageSender: TelegramCommandSender,
) : TelegramUpdateHandler {
    override fun handleNewChatMembers(members: IncomingChatMembers) {
        chatMemberRegistry.rememberMembers(members)
        newChatMembersGreetingService.greet(members)
    }

    override fun handleMessage(message: IncomingChatMessage) {
        chatMemberRegistry.rememberSender(message)
        statisticsService.recordMessage(message)

        bayanMessageHandler.handle(message)

        val command = commandParser.parse(message) ?: return
        logger.info("Received Telegram command '{}' in chat {}", command.command, command.chatId)

        val commandHandler = commandHandlers.firstOrNull { it.supports(command) }
        if (commandHandler == null) {
            messageSender.sendReplyMessage(message.chatId, message.messageId, "Я не понимаю :(")
            return
        }

        commandHandler.handle(command)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultTelegramUpdateHandler::class.java)
    }
}

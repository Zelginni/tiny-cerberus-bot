package ru.zelginni.tinycerberusbot.telegram.command

import org.slf4j.LoggerFactory
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

abstract class AbstractCommandHandler(
    private val telegramCommandSender: TelegramCommandSender,
    private val chatMemberService: TelegramChatMemberService,
) : CommandHandler {

    final override fun handle(command: ChatCommand) {
        if (requiresAdmin && !chatMemberService.isChatAdministrator(command.chatId, command.userId)) {
            return
        }

        try {
            handleCommand(command)
        } catch (e: Exception) {
            logger.error("Command not performed: ${command.command}", e)
            reply(command, "Не получилось :(")
        }
    }

    protected abstract fun handleCommand(command: ChatCommand)

    protected fun reply(command: ChatCommand, text: String) {
        telegramCommandSender.sendReplyMessage(command.chatId, command.messageId, text)
    }

    protected fun banUser(chatId: Long, userId: Long) {
        telegramCommandSender.banChatMember(chatId, userId)
    }

    protected fun isAdmin(chatId: Long, userId: Long): Boolean =
        chatMemberService.isChatAdministrator(chatId, userId)

    protected fun IncomingChatMessage.writableName(): String =
        senderUsername ?: senderDisplayName ?: userId.toString()

    protected fun ChatCommand.authorName(): String =
        senderUsername ?: senderDisplayName ?: userId.toString()

    protected fun getLimitText(warnLimit: Int?): String =
        "Лимит варнов в чате " + if ((warnLimit ?: -1) > 0) "равен $warnLimit" else "не установлен"

    private companion object {
        private val logger = LoggerFactory.getLogger(AbstractCommandHandler::class.java)
    }
}

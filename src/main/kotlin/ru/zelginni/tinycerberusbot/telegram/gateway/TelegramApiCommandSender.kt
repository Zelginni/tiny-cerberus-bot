package ru.zelginni.tinycerberusbot.telegram.gateway

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.Serializable

@Component
class TelegramApiCommandSender(
    private val telegramClient: TelegramClient,
) : TelegramCommandSender {
    override fun sendMessage(chatId: Long, text: String, messageThreadId: Int?) {
        execute(SendMessage(chatId.toString(), text).withMessageThread(messageThreadId))
    }

    override fun sendReplyMessage(chatId: Long, replyToMessageId: Int, text: String, messageThreadId: Int?) {
        execute(
            SendMessage(chatId.toString(), text).apply {
                this.replyToMessageId = replyToMessageId
                this.messageThreadId = messageThreadId
                enableHtml(true)
            }
        )
    }

    override fun sendSilentMessage(chatId: String, text: String, messageThreadId: Int?): Message? {
        return execute(
            SendMessage(chatId, text).apply {
                this.messageThreadId = messageThreadId
                disableNotification = true
            }
        )
    }

    override fun banChatMember(chatId: Long, userId: Long) {
        execute(BanChatMember(chatId.toString(), userId))
    }

    override fun getChatAdministrators(chatId: Long): List<ChatMember> {
        return execute(GetChatAdministrators(chatId.toString())) ?: emptyList()
    }

    override fun pinMessage(chatId: String, messageId: Int, disableNotification: Boolean) {
        execute(PinChatMessage(chatId, messageId, disableNotification, null))
    }

    override fun unpinMessage(chatId: String, messageId: Int) {
        execute(UnpinChatMessage(chatId, messageId, null))
    }

    private fun <T : Serializable> execute(action: BotApiMethod<T>): T? {
        return try {
            telegramClient.execute(action)
        } catch (e: TelegramApiException) {
            logger.error("Problem to perform $action", e)
            null
        }
    }

    private fun execute(action: SendMessage): Message? {
        return try {
            telegramClient.execute(action)
        } catch (e: TelegramApiException) {
            logger.error("Problem to perform $action", e)
            null
        }
    }

    private fun SendMessage.withMessageThread(messageThreadId: Int?): SendMessage =
        apply { this.messageThreadId = messageThreadId }

    private companion object {
        private val logger = LoggerFactory.getLogger(TelegramApiCommandSender::class.java)
    }
}

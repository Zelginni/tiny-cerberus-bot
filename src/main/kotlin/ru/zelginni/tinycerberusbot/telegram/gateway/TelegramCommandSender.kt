package ru.zelginni.tinycerberusbot.telegram.gateway

import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.message.Message

interface TelegramCommandSender {
    fun sendMessage(chatId: Long, text: String, messageThreadId: Int? = null)

    fun sendReplyMessage(chatId: Long, replyToMessageId: Int, text: String, messageThreadId: Int? = null)

    fun sendSilentMessage(chatId: String, text: String, messageThreadId: Int? = null): Message?

    fun banChatMember(chatId: Long, userId: Long)

    fun getChatAdministrators(chatId: Long): List<ChatMember>

    fun pinMessage(chatId: String, messageId: Int, disableNotification: Boolean = true)

    fun unpinMessage(chatId: String, messageId: Int)
}

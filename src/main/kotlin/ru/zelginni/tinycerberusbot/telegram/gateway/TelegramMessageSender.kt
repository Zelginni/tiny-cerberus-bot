package ru.zelginni.tinycerberusbot.telegram.gateway

interface TelegramMessageSender {
    fun sendMessage(chatId: Long, text: String)
}

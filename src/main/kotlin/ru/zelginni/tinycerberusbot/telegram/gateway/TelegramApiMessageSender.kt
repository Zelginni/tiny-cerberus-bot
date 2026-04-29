package ru.zelginni.tinycerberusbot.telegram.gateway

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class TelegramApiMessageSender(
    private val telegramClient: TelegramClient,
) : TelegramMessageSender {
    override fun sendMessage(chatId: Long, text: String) {
        telegramClient.execute(SendMessage(chatId.toString(), text))
    }
}

package ru.zelginni.tinycerberusbot.bayan

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class BayanMessageHandler(
    private val bayanService: BayanService,
    private val telegramCommandSender: TelegramCommandSender,
) {

    fun handle(message: IncomingChatMessage) {
        val text = message.text ?: return
        if (!text.lowercase().contains(BAYAN_MARKER) || message.senderIsBot) {
            return
        }

        bayanService.findResponseForChat(message.chatId)
            ?.response
            ?.let { telegramCommandSender.sendReplyMessage(message.chatId, message.messageId, it) }
    }

    private companion object {
        private const val BAYAN_MARKER = "баян"
    }
}

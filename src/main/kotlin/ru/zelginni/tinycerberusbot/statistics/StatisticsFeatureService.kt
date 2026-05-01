package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage

@Service
class StatisticsFeatureService(
    private val chatService: ChatService,
) {
    fun isEnabled(chatId: Long): Boolean =
        chatService.getEnabledChatByTelegramId(chatId.toString())?.statisticsEnabled == true

    fun shouldRecordMessage(message: IncomingChatMessage): Boolean {
        val chat = chatService.getEnabledChatByTelegramId(message.chatId.toString())
        if (chat?.statisticsEnabled != true) {
            return false
        }

        val messageThreadId = message.messageThreadId ?: return true
        return messageThreadId !in chat.ignoredStatisticsMessageThreadIds
    }

    fun disabledMessage(): String = STATISTICS_DISABLED_MESSAGE

    private companion object {
        private const val STATISTICS_DISABLED_MESSAGE = "Аид запретил мне наблюдать за этим чатом."
    }
}

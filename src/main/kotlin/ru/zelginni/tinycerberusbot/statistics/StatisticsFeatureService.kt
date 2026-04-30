package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.ChatService

@Service
class StatisticsFeatureService(
    private val chatService: ChatService,
) {
    fun isEnabled(chatId: Long): Boolean =
        chatService.getEnabledChatByTelegramId(chatId.toString())?.statisticsEnabled == true

    fun disabledMessage(): String = STATISTICS_DISABLED_MESSAGE

    private companion object {
        private const val STATISTICS_DISABLED_MESSAGE = "Аид запретил мне наблюдать за этим чатом."
    }
}

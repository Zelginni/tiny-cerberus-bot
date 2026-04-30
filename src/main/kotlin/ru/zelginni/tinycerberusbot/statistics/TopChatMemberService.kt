package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService

@Service
class TopChatMemberService(
    private val chatMemberService: TelegramChatMemberService,
    private val statisticsService: MessageStatisticsService,
) {
    fun getTopMembers(chatId: Long): List<TopChatMemberView> {
        val memberNamesById = chatMemberService.getChatMembers(chatId)
            .associate { it.userId to it.displayName }

        return statisticsService.getChatTop(chatId, limit = TOP_LIMIT)
            .map {
                TopChatMemberView(
                    userId = it.userId,
                    displayName = memberNamesById[it.userId] ?: it.userId.toString(),
                    messageCount = it.messageCount,
                )
            }
    }

    private companion object {
        const val TOP_LIMIT = 5
    }
}

data class TopChatMemberView(
    val userId: Long,
    val displayName: String,
    val messageCount: Long,
)

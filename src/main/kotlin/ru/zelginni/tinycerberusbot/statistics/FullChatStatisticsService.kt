package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.chat.DEFAULT_FULL_STATISTICS_LIMIT
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService

@Service
class FullChatStatisticsService(
    private val chatService: ChatService,
    private val chatMemberService: TelegramChatMemberService,
    private val statisticsService: MessageStatisticsService,
) {
    fun getLimitedStatistics(chatId: Long): List<ChatMemberStatisticsView> =
        getStatistics(chatId, chatService.getFullStatisticsLimit(chatId))

    fun getCompleteStatistics(chatId: Long): List<ChatMemberStatisticsView> =
        getStatistics(chatId, DEFAULT_FULL_STATISTICS_LIMIT)

    private fun getStatistics(chatId: Long, limit: Int): List<ChatMemberStatisticsView> {
        val memberNamesById = chatMemberService.getChatMembers(chatId)
            .associate { it.userId to it.displayName }

        return statisticsService.getChatStatistics(chatId, limit)
            .map {
                ChatMemberStatisticsView(
                    userId = it.userId,
                    displayName = memberNamesById[it.userId] ?: it.userId.toString(),
                    messageCount = it.messageCount,
                )
            }
    }
}

data class ChatMemberStatisticsView(
    val userId: Long,
    val displayName: String,
    val messageCount: Long,
)

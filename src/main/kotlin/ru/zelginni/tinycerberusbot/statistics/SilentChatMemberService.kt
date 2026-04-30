package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class SilentChatMemberService(
    private val chatMemberService: TelegramChatMemberService,
    private val statisticsService: MessageStatisticsService,
    private val clock: Clock,
) {
    fun getSilentMembers(chatId: Long, days: Long = DEFAULT_SILENCE_DAYS): List<SilentChatMemberView> {
        val threshold = Instant.now(clock).minus(Duration.ofDays(days))
        val activeUserIds = statisticsService.getActiveUserIdsSince(chatId, threshold)

        return chatMemberService.getChatMembers(chatId)
            .filterNot { it.userId in activeUserIds }
            .map {
                SilentChatMemberView(
                    userId = it.userId,
                    displayName = it.displayName,
                )
            }
    }

    companion object {
        const val DEFAULT_SILENCE_DAYS = 30L
    }
}

data class SilentChatMemberView(
    val userId: Long,
    val displayName: String,
)

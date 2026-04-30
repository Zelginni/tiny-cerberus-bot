package ru.zelginni.tinycerberusbot.statistics

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zelginni.tinycerberusbot.chat.DEFAULT_FULL_STATISTICS_LIMIT
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import java.time.Instant

@Service
class MessageStatisticsService(
    private val repository: MessageStatisticRepository,
    private val statisticsFeatureService: StatisticsFeatureService,
) {
    @Transactional
    fun recordMessage(message: IncomingChatMessage) {
        if (!statisticsFeatureService.isEnabled(message.chatId)) {
            logger.debug("Skip message statistics recording chatId={} because statistics feature is disabled", message.chatId)
            return
        }

        val statistic = repository.findByChatIdAndUserId(message.chatId, message.userId)
            ?: MessageStatistic(chatId = message.chatId, userId = message.userId)

        statistic.messageCount += 1
        statistic.lastMessageAt = message.sentAt
        logger.debug(
            "Incremented message counter chatId={} userId={} messageCount={}",
            message.chatId,
            message.userId,
            statistic.messageCount,
        )

        repository.save(statistic)
    }

    @Transactional(readOnly = true)
    fun getChatTop(chatId: Long, limit: Int = 20): List<MessageStatisticView> =
        repository.findByChatIdOrderByMessageCountDesc(chatId, PageRequest.of(0, limit))
            .map { it.toView() }

    @Transactional(readOnly = true)
    fun getChatStatistics(chatId: Long, limit: Int): List<MessageStatisticView> {
        val statistics = if (limit == DEFAULT_FULL_STATISTICS_LIMIT) {
            repository.findByChatIdOrderByMessageCountDesc(chatId)
        } else {
            repository.findByChatIdOrderByMessageCountDesc(chatId, PageRequest.of(0, limit))
        }

        return statistics.map { it.toView() }
    }

    @Transactional(readOnly = true)
    fun getActiveUserIdsSince(chatId: Long, since: Instant): Set<Long> =
        repository.findByChatIdAndLastMessageAtGreaterThanEqual(chatId, since)
            .map { it.userId }
            .toSet()

    private companion object {
        private val logger = LoggerFactory.getLogger(MessageStatisticsService::class.java)
    }
}

data class MessageStatisticView(
    val chatId: Long,
    val userId: Long,
    val messageCount: Long,
    val lastMessageAt: Instant?,
)

private fun MessageStatistic.toView(): MessageStatisticView =
    MessageStatisticView(
        chatId = chatId,
        userId = userId,
        messageCount = messageCount,
        lastMessageAt = lastMessageAt,
    )

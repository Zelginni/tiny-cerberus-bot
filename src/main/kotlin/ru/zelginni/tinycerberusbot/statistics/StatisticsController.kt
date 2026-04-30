package ru.zelginni.tinycerberusbot.statistics

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.zelginni.tinycerberusbot.statistics.SilentChatMemberService.Companion.DEFAULT_SILENCE_DAYS

@RestController
@RequestMapping("/admin/bot/statistics")
class StatisticsController(
    private val statisticsService: MessageStatisticsService,
    private val silentChatMemberService: SilentChatMemberService,
    private val topChatMemberService: TopChatMemberService,
    private val fullChatStatisticsService: FullChatStatisticsService,
) {
    @GetMapping("/chats/{chatId}")
    fun getChatStatistics(@PathVariable chatId: Long): List<ChatMemberStatisticsView> {
        logger.info("Admin API call get chat statistics chatId={}", chatId)
        return fullChatStatisticsService.getLimitedStatistics(chatId)
    }

    @GetMapping("/chats/{chatId}/all")
    fun getCompleteChatStatistics(@PathVariable chatId: Long): List<ChatMemberStatisticsView> {
        logger.info("Admin API call get complete chat statistics chatId={}", chatId)
        return fullChatStatisticsService.getCompleteStatistics(chatId)
    }

    @GetMapping("/chats/{chatId}/top")
    fun getChatTop(@PathVariable chatId: Long): List<TopChatMemberView> {
        logger.info("Admin API call get chat top chatId={}", chatId)
        return topChatMemberService.getTopMembers(chatId)
    }

    @GetMapping("/chats/{chatId}/silent-members")
    fun getSilentMembers(
        @PathVariable chatId: Long,
        @RequestParam(required = false) days: Long?,
    ): List<SilentChatMemberView> {
        logger.info("Admin API call get silent members chatId={} days={}", chatId, days)
        return silentChatMemberService.getSilentMembers(chatId, days = days.silenceDays())
    }

    private fun Long?.silenceDays(): Long =
        this?.takeIf { it > 0 } ?: DEFAULT_SILENCE_DAYS

    private companion object {
        private val logger = LoggerFactory.getLogger(StatisticsController::class.java)
    }
}

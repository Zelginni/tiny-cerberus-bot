package ru.zelginni.tinycerberusbot.statistics

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.zelginni.tinycerberusbot.statistics.MessageStatistic
import java.time.Instant

interface MessageStatisticRepository : JpaRepository<MessageStatistic, Long> {
    fun findByChatIdAndUserId(chatId: Long, userId: Long): MessageStatistic?

    fun findByChatIdAndLastMessageAtGreaterThanEqual(chatId: Long, since: Instant): List<MessageStatistic>

    fun findByChatIdOrderByMessageCountDesc(chatId: Long): List<MessageStatistic>

    fun findByChatIdOrderByMessageCountDesc(chatId: Long, pageable: Pageable): List<MessageStatistic>
}

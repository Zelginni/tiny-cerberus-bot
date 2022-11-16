package ru.zelginni.tinycerberusbot.digest

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface DigestRepository: JpaRepository<Digest, Long> {
    fun findByLinkToMessage(linkToMessage: String): Digest?
    fun findAllByChatIdAndCreatedOnBetween(chatId: Long, past: LocalDateTime, now: LocalDateTime): List<Digest>?
    fun deleteAllByChatId(chatId: Long)
}



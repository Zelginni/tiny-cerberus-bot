package ru.zelginni.tinycerberusbot.warn

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WarnRepository: JpaRepository<Warn, Long> {
    fun findAllByUserId(userId: Long): List<Warn>
    @Query("FROM Warn as w WHERE w.user.chat.id = ?1")
    fun findAllByChatId(chatId: Long): List<Warn>
}
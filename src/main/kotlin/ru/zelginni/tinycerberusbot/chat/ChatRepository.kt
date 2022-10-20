package ru.zelginni.tinycerberusbot.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository: JpaRepository<Chat, Long> {
    fun findByTelegramId(telegramId: String): Chat?

    fun findByTelegramIdAndEnabledTrue(telegramId: String): Chat?
}
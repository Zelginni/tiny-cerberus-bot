package ru.zelginni.tinycerberusbot.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByTelegramIdAndChatId(telegramId: String, chatId: Long): User?
}
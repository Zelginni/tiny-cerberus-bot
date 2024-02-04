package ru.zelginni.tinycerberusbot.rules

import org.springframework.data.jpa.repository.JpaRepository

interface RulesRepository: JpaRepository<Rules, Long> {
    fun findByChatId(chatId: Long): Rules?

    fun deleteByChatId(chatId: Long)
}
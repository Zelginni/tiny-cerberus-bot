package ru.zelginni.tinycerberusbot.warn

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WarnRepository: JpaRepository<Warn, Long> {
    fun findAllByUserId(userId: Long): List<Warn>

    @Query("delete from cerberus.warn " +
            "where id in (select cw.id from cerberus.warn cw " +
            "inner join cerberus.chat_user ccu on cw.user_id = ccu.id " +
            "inner join cerberus.chat cc on cc.id = ccu.chat_id where cc.telegram_id = ?1)", nativeQuery = true)
    fun removeAllByChatId(chatId: Long)
}
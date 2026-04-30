package ru.zelginni.tinycerberusbot.telegram.gateway

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TelegramKnownChatMemberRepository : JpaRepository<TelegramKnownChatMember, TelegramKnownChatMemberId> {
    @Query(
        """
        select chatMember
        from TelegramKnownChatMember chatMember
        where chatMember.id.chatId = :chatId
        order by chatMember.displayName asc, chatMember.id.userId asc
        """,
    )
    fun findKnownMembers(chatId: Long): List<TelegramKnownChatMember>
}

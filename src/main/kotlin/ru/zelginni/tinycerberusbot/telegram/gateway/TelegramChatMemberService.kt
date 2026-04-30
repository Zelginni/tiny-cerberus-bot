package ru.zelginni.tinycerberusbot.telegram.gateway

interface TelegramChatMemberService {
    fun isChatAdministrator(chatId: Long, userId: Long): Boolean

    fun getChatMembers(chatId: Long): List<TelegramChatMember>
}

data class TelegramChatMember(
    val userId: Long,
    val displayName: String,
)

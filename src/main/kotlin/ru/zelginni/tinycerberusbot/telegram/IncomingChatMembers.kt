package ru.zelginni.tinycerberusbot.telegram

data class IncomingChatMembers(
    val chatId: Long,
    val messageId: Int,
    val messageThreadId: Int? = null,
    val members: List<IncomingChatMemberProfile>,
)

data class IncomingChatMemberLeft(
    val chatId: Long,
    val userId: Long,
)

data class IncomingChatMemberProfile(
    val userId: Long,
    val displayName: String,
    val username: String?,
    val isBot: Boolean,
)

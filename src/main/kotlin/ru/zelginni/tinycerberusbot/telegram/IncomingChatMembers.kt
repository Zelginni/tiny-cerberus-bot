package ru.zelginni.tinycerberusbot.telegram

data class IncomingChatMembers(
    val chatId: Long,
    val members: List<IncomingChatMemberProfile>,
)

data class IncomingChatMemberProfile(
    val userId: Long,
    val displayName: String,
)

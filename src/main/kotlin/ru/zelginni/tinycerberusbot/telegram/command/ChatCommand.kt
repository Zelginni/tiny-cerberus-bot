package ru.zelginni.tinycerberusbot.telegram.command

data class ChatCommand(
    val chatId: Long,
    val userId: Long,
    val command: String,
    val arguments: String?,
)

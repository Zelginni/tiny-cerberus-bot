package ru.zelginni.tinycerberusbot.telegram.command

import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import java.time.Instant

data class ChatCommand(
    val chatId: Long,
    val userId: Long,
    val messageId: Int,
    val messageThreadId: Int?,
    val command: String,
    val arguments: String?,
    val sentAt: Instant,
    val senderDisplayName: String?,
    val senderUsername: String?,
    val replyTo: IncomingChatMessage?
)

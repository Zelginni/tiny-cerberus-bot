package ru.zelginni.tinycerberusbot.telegram

import java.time.Instant

data class IncomingChatMessage(
    val chatId: Long,
    val userId: Long,
    val messageId: Int,
    val messageThreadId: Int? = null,
    val text: String?,
    val sentAt: Instant,
    val senderDisplayName: String? = null,
    val senderUsername: String? = null,
    val senderIsBot: Boolean = false,
    val automaticForward: Boolean = false,
    val replyTo: IncomingChatMessage? = null,
    val hasText: Boolean = text != null,
    val hasPhoto: Boolean = false,
    val hasDocument: Boolean = false,
    val hasOnlyText: Boolean = true,
)

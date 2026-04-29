package ru.zelginni.tinycerberusbot.telegram

import java.time.Instant

data class IncomingChatMessage(
    val chatId: Long,
    val userId: Long,
    val messageId: Long,
    val text: String?,
    val sentAt: Instant,
    val senderDisplayName: String? = null,
)

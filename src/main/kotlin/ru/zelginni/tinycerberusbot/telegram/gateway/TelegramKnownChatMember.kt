package ru.zelginni.tinycerberusbot.telegram.gateway

import javax.persistence.*
import java.io.Serializable
import java.time.Instant

@Entity
@Table(schema = "cerberus", name = "telegram_known_chat_members")
class TelegramKnownChatMember(
    @EmbeddedId
    val id: TelegramKnownChatMemberId,
    @Column(nullable = false)
    var displayName: String,
    @Column(nullable = false)
    var lastSeenAt: Instant,
    @Column
    var username: String? = null,
)

@Embeddable
data class TelegramKnownChatMemberId(
    @Column(name = "chat_id", nullable = false)
    val chatId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
) : Serializable

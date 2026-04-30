package ru.zelginni.tinycerberusbot.statistics

import javax.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "message_statistics",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_message_statistics_chat_user",
            columnNames = ["chat_id", "user_id"],
        ),
    ],
)
class MessageStatistic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "chat_id", nullable = false)
    val chatId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false)
    var messageCount: Long = 0,
    var lastMessageAt: Instant? = null,
)

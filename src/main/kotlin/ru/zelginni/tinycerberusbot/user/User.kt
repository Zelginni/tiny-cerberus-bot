package ru.zelginni.tinycerberusbot.user

import ru.zelginni.tinycerberusbot.chat.Chat
import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "chat_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long? = null,
    @Column(name = "telegram_id")
    var telegramId: String? = null,
    @Column
    var username: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    var chat: Chat? = null,
)
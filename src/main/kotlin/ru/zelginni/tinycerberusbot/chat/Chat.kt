package ru.zelginni.tinycerberusbot.chat

import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "chat")
data class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long? = null,
    @Column
    var name: String? = null,
    @Column(name = "telegram_id")
    var telegramId: String? = null,
    @Column(name = "enabled")
    var enabled: Boolean? = true,
    @Column(name = "warn_limit")
    var warnLimit: Int? = null
)
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
    var warnLimit: Int? = null,
    @Column(name = "bayan_enabled")
    var bayanEnabled: Boolean? = false,
    @Column(name = "digest_enabled")
    var digestEnabled: Boolean? = false
)
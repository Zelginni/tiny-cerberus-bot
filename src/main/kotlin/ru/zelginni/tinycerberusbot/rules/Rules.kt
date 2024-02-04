package ru.zelginni.tinycerberusbot.rules

import ru.zelginni.tinycerberusbot.chat.Chat
import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "rules")
data class Rules(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        var id: Long? = null,
        @Column
        var ruleset:String? = null,
        @OneToOne
        @JoinColumn(name = "chat_id", referencedColumnName = "id")
        var chat: Chat? = null,
)

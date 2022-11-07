package ru.zelginni.tinycerberusbot.digest

import ru.zelginni.tinycerberusbot.chat.Chat
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "digest")
data class Digest(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        var id: Long? = null,
        @OneToOne
        @JoinColumn(name = "chat_id", referencedColumnName = "id")
        var chat: Chat? = null,
        @Column(name = "link_to_message")
        var linkToMessage: String? = null,
        @Column
        var description:String? = null,
        @Column(name = "created_on")
        var createdOn:LocalDateTime? = null
        )
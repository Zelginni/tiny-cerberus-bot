package ru.zelginni.tinycerberusbot.warn

import ru.zelginni.tinycerberusbot.user.User
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "warn")
data class Warn(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long? = null,
    @Column(name = "date_created")
    var dateCreated: Timestamp? = null,
    @Column(name = "author_telegram_id")
    var authorTelegramId: String? = null,
    @Column(name = "author_username")
    var authorUsername: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
)

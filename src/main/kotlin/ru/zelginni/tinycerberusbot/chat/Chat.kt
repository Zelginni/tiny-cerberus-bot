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
    var digestEnabled: Boolean? = false,
    @Column(name = "rules_enabled")
    var rulesEnabled: Boolean? = false,
    @Column(name = "statistics_enabled")
    var statisticsEnabled: Boolean? = false,
    @Column(nullable = false)
    var fullStatisticsLimit: Int = DEFAULT_FULL_STATISTICS_LIMIT,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        schema = "cerberus",
        name = "chat_ignored_statistics_topics",
        joinColumns = [JoinColumn(name = "chat_id")]
    )
    @Column(name = "message_thread_id", nullable = false)
    var ignoredStatisticsMessageThreadIds: MutableSet<Int> = mutableSetOf(),
)

const val DEFAULT_FULL_STATISTICS_LIMIT = -1

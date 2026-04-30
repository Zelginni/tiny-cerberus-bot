package ru.zelginni.tinycerberusbot.bot

import javax.persistence.*
import java.time.Instant

@Entity
@Table(name = "bot_settings")
class BotSettings(
    @Id
    val id: String = DEFAULT_ID,
    @Enumerated(EnumType.STRING)
    var state: BotState = BotState.DISABLED,
    var updatedAt: Instant = Instant.now(),
) {
    companion object {
        const val DEFAULT_ID = "default"
    }
}

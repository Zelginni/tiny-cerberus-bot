package ru.zelginni.tinycerberusbot.bot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot")
data class TelegramBotProperties(
    var enabled: Boolean = false,
    var token: String = "",
    var name: String = "tinycerberus_bot",
)

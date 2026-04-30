package ru.zelginni.tinycerberusbot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient
import ru.zelginni.tinycerberusbot.bot.TelegramBotProperties

@Configuration
class TelegramClientConfig {
    @Bean
    fun telegramClient(properties: TelegramBotProperties): TelegramClient =
        OkHttpTelegramClient(properties.token)
}

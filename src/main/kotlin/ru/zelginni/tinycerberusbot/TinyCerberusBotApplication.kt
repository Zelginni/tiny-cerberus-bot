package ru.zelginni.tinycerberusbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import ru.zelginni.tinycerberusbot.bot.TelegramBotProperties

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TelegramBotProperties::class)
class TinyCerberusBotApplication

fun main(args: Array<String>) {
	runApplication<TinyCerberusBotApplication>(*args)
}

package ru.zelginni.tinycerberusbot.bot

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update

@Component
@ConditionalOnProperty(prefix = "bot", name = ["enabled"], havingValue = "true")
class TinyCerberusLongPollingBot(
    private val botProperties: TelegramBotProperties,
    private val tinyCerberusBot: TinyCerberusBot,
) : SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    override fun getBotToken(): String = botProperties.token

    override fun getUpdatesConsumer() = this

    override fun consume(update: Update?) {
        tinyCerberusBot.consume(update)
    }
}

package ru.zelginni.tinycerberusbot.statistics

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatRepository
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import java.time.Instant

@SpringBootTest
class StatisticsFeatureServiceTest {

    @Autowired
    private lateinit var statisticsFeatureService: StatisticsFeatureService

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Test
    fun skipAutomaticForwardedChannelPost() {
        val chat = chatRepository.saveAndFlush(
            Chat(
                name = "Automatic forward chat",
                telegramId = "-100100100",
                statisticsEnabled = true,
            )
        )

        val message = message(chat.telegramId!!.toLong(), automaticForward = true)

        assertFalse(statisticsFeatureService.shouldRecordMessage(message))
    }

    @Test
    fun recordRegularMessage() {
        val chat = chatRepository.saveAndFlush(
            Chat(
                name = "Regular statistics chat",
                telegramId = "-100200200",
                statisticsEnabled = true,
            )
        )

        val message = message(chat.telegramId!!.toLong())

        assertTrue(statisticsFeatureService.shouldRecordMessage(message))
    }

    private fun message(chatId: Long, automaticForward: Boolean = false): IncomingChatMessage =
        IncomingChatMessage(
            chatId = chatId,
            userId = 777000,
            messageId = 1,
            text = "test",
            sentAt = Instant.EPOCH,
            senderDisplayName = "Telegram",
            automaticForward = automaticForward,
        )
}

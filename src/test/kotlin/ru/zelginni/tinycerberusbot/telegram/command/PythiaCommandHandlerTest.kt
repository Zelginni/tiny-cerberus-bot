package ru.zelginni.tinycerberusbot.telegram.command

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatRepository
import ru.zelginni.tinycerberusbot.pythia.PythiaPrediction
import ru.zelginni.tinycerberusbot.pythia.PythiaPredictionRepository
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramApiCommandSender
import java.time.Instant

@SpringBootTest
class PythiaCommandHandlerTest {
    @Autowired
    private lateinit var handler: PythiaCommandHandler

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    private lateinit var predictionRepository: PythiaPredictionRepository

    @MockBean
    private lateinit var telegramCommandSender: TelegramApiCommandSender

    @Test
    fun handlePythiaCommandForRegularChatMember() {
        val chatId = -4242424242L
        val messageId = 100
        val predictionText = "Пифия видит зеленый билд."
        chatRepository.saveAndFlush(Chat(name = "Pythia chat", telegramId = chatId.toString(), pythiaEnabled = true))
        predictionRepository.deleteAll()
        predictionRepository.saveAndFlush(PythiaPrediction(text = predictionText))

        handler.handle(
            ChatCommand(
                chatId = chatId,
                userId = 1L,
                messageId = messageId,
                messageThreadId = null,
                command = "pythia",
                arguments = null,
                sentAt = Instant.EPOCH,
                senderDisplayName = "Regular Member",
                senderUsername = "regular_member",
                replyTo = null,
            )
        )

        verify(telegramCommandSender).sendReplyMessage(chatId, messageId, predictionText, null)
    }
}

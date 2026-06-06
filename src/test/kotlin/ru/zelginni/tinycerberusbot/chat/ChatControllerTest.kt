package ru.zelginni.tinycerberusbot.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.zelginni.tinycerberusbot.bot.BotState
import ru.zelginni.tinycerberusbot.bot.SetBotStateRequest
import ru.zelginni.tinycerberusbot.user.User
import ru.zelginni.tinycerberusbot.user.UserRepository
import ru.zelginni.tinycerberusbot.warn.Warn
import ru.zelginni.tinycerberusbot.warn.WarnRepository
import java.sql.Timestamp
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var warnRepository: WarnRepository

    @Test
    @WithMockUser
    fun createChat() {
        val chatName = "Web test chat"
        val telegramId = "-12321"
        mockMvc.post("/admin/chat") {
            content = objectMapper.writeValueAsString(
                ChatInsertDto(
                    name = chatName,
                    telegramId = telegramId,
                    fullStatisticsLimit = 10,
                    ignoredStatisticsMessageThreadIds = listOf(100, 200),
                )
            )
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect { status().isOk }

        val result = chatRepository.findByTelegramId(telegramId)
        assertEquals(chatName, result?.name)
        assertEquals(10, result?.fullStatisticsLimit)
        assertEquals(setOf(100, 200), result?.ignoredStatisticsMessageThreadIds)
    }

    @Test
    @WithMockUser
    fun getAllChats() {
        val chatName = "Web test chat 2"
        val telegramId = "-123213333"
        chatRepository.saveAndFlush(Chat(name = chatName, telegramId = telegramId))

        mockMvc.get("/admin/chat/all")
            .andDo { print() }
            .andExpect { status().isOk }
            .andExpect { jsonPath("$.chats[?(@.telegramId == '$telegramId')].name") { value(chatName) } }
    }

    @Test
    @WithMockUser
    fun disableChat() {
        val chatName = "Web test chat 3"
        val telegramId = "-33333333"
        val chat = Chat(name = chatName, telegramId = telegramId)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/disable?telegramId=$telegramId")
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(false, modifiedChat.enabled)
    }

    @Test
    @WithMockUser
    fun enableChat() {
        val chatName = "Web test chat 4"
        val telegramId = "-444444444"
        val chat = Chat(name = chatName, telegramId = telegramId, enabled = false)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/enable?telegramId=$telegramId")
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(true, modifiedChat.enabled)
    }

    @Test
    @WithMockUser
    fun changeWarnLimit() {
        val chatName = "Web test chat 5"
        val telegramId = "-555555555"
        val chat = Chat(name = chatName, telegramId = telegramId, warnLimit = 3)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/warn-limit?telegramId=$telegramId&warnLimit=5")
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(5, modifiedChat.warnLimit)
    }

    @Test
    @WithMockUser
    fun changeFullStatisticsLimit() {
        val chatName = "Web test chat 6"
        val telegramId = "-666666666"
        val chat = Chat(name = chatName, telegramId = telegramId, fullStatisticsLimit = 3)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/full-statistics-limit?telegramId=$telegramId&fullStatisticsLimit=15")
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(15, modifiedChat.fullStatisticsLimit)
    }

    @Test
    @WithMockUser
    fun changeIgnoredStatisticsTopics() {
        val chatName = "Web test chat ignored topics"
        val telegramId = "-888888888"
        val chat = Chat(name = chatName, telegramId = telegramId)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/ignored-statistics-topics?telegramId=$telegramId") {
            content = objectMapper.writeValueAsString(
                IgnoredStatisticsTopicsRequest(
                    ignoredStatisticsMessageThreadIds = listOf(11, 22, 11),
                )
            )
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(setOf(11, 22), modifiedChat.ignoredStatisticsMessageThreadIds)
    }

    @Test
    @WithMockUser
    fun enableStatisticsFeature() {
        val chatName = "Web test chat 7"
        val telegramId = "-777777777"
        val chat = Chat(name = chatName, telegramId = telegramId, statisticsEnabled = false)
        chatRepository.saveAndFlush(chat)

        mockMvc.put("/admin/chat/enable/STATISTICS?telegramId=$telegramId")
            .andDo { print() }
            .andExpect { status().isOk }

        val modifiedChat = chatRepository.findById(chat.id!!).orElse(null)
        assertEquals(true, modifiedChat.statisticsEnabled)
    }

    @Test
    @WithMockUser
    fun getChatWarnStatistics() {
        val telegramId = "-999999999"
        val chat = chatRepository.saveAndFlush(Chat(name = "Warn statistics chat", telegramId = telegramId, warnLimit = 3))
        val user = userRepository.saveAndFlush(User(telegramId = "12345", username = "warned-user", chat = chat))
        warnRepository.saveAllAndFlush(
            listOf(
                Warn(
                    dateCreated = Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 12, 0)),
                    authorTelegramId = "1",
                    authorUsername = "admin",
                    user = user,
                ),
                Warn(
                    dateCreated = Timestamp.valueOf(LocalDateTime.of(2026, 1, 2, 12, 0)),
                    authorTelegramId = "2",
                    authorUsername = "admin-2",
                    user = user,
                ),
            )
        )

        mockMvc.get("/admin/bot/statistics/chats/$telegramId/warns")
            .andDo { print() }
            .andExpect { status().isOk }
            .andExpect { jsonPath("$.chatId") { value(telegramId.toLong()) } }
            .andExpect { jsonPath("$.warnLimit") { value(3) } }
            .andExpect { jsonPath("$.users[0].telegramId") { value("12345") } }
            .andExpect { jsonPath("$.users[0].username") { value("warned-user") } }
            .andExpect { jsonPath("$.users[0].warnCount") { value(2) } }
    }

    @Test
    @WithMockUser
    fun setBotState() {
        mockMvc.put("/admin/bot/state") {
            content = objectMapper.writeValueAsString(SetBotStateRequest(BotState.ENABLED))
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect { status().isOk }
            .andExpect { jsonPath("$.state") { value(BotState.ENABLED.name) } }
    }
}

package ru.zelginni.tinycerberusbot.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Test
    @WithMockUser
    fun createChat() {
        val chatName = "Web test chat"
        val telegramId = "-12321"
        mockMvc.post("/admin/chat") {
            content = objectMapper.writeValueAsString(ChatInsertDto(chatName, telegramId))
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect { status().isOk }

        val example = Chat(name = chatName, telegramId = telegramId)
        val result = chatRepository.findOne(Example.of(example))
        assertEquals(true, result.isPresent)
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
            .andExpect { jsonPath("$.chats[0].name") {value(chatName)} }
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
}
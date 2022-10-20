package ru.zelginni.tinycerberusbot.chat

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatServiceTest {

    @Autowired
    private lateinit var chatService: ChatService
    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Test
    fun whenCreateChatThenChatStoredInDb() {
        val chatDto = ChatInsertDto(telegramId = "-100500", name = "Test chat")
        chatService.createNewChat(chatDto)

        val resultChat = chatRepository.findAll().find { chat -> chatDto.telegramId == chat.telegramId }
        assertNotNull(resultChat)
        assertEquals(resultChat?.name, chatDto.name)
        assertEquals(true, resultChat?.enabled)
    }

    @Test
    fun whenGetChatListThenReturnChats() {
        val firstChat = Chat(name = "first", telegramId = "-111")
        val secondChat = Chat(name = "second", telegramId = "-222")
        chatRepository.saveAllAndFlush(mutableListOf(firstChat, secondChat))

        val resultList = chatService.getAllChats()
        assertTrue(resultList.isNotEmpty())
        val resultFirstChat = resultList.find { chatViewDto -> chatViewDto.telegramId == firstChat.telegramId }
        assertEquals(firstChat.name, resultFirstChat?.name)
        assertEquals(true, resultFirstChat?.enabled)
        val resultSecondChat = resultList.find { chatViewDto -> chatViewDto.telegramId == secondChat.telegramId }
        assertEquals(secondChat.name, resultSecondChat?.name)
        assertEquals(true, resultSecondChat?.enabled)
    }
}
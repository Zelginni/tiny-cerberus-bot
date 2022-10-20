package ru.zelginni.tinycerberusbot.chat

import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ChatService(
    private val chatRepository: ChatRepository
) {

    fun createNewChat(chat: ChatInsertDto) {
        chatRepository.saveAndFlush(chat.toDbModel())
    }

    fun getAllChats(): List<ChatViewDto> {
        return chatRepository.findAll().map { chat -> chat.toViewModel() }
    }

    fun disableChat(telegramId: String) {
        changeChatAvailability(telegramId, false)
    }

    fun enableChat(telegramId: String) {
        changeChatAvailability(telegramId, true)
    }

    private fun changeChatAvailability(telegramId: String, enableChat: Boolean) {
        val chat = chatRepository.findByTelegramId(telegramId)
            ?: throw IllegalArgumentException("Chat with telegram id $telegramId not fount")
        if (chat.enabled == enableChat) {
            return
        }
        chat.enabled = enableChat
        chatRepository.saveAndFlush(chat)
    }

    fun getEnabledChatByTelegramId(telegramId: String): Chat? {
        return chatRepository.findByTelegramIdAndEnabledTrue(telegramId)
    }
}
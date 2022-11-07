package ru.zelginni.tinycerberusbot.chat

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
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
        changeChat(telegramId) {enabled = false}
    }

    fun enableChat(telegramId: String) {
        changeChat(telegramId) {enabled = true}
    }

    fun disableBayanInChat(telegramId: String) {
        changeChat(telegramId) {bayanEnabled = false}
    }

    fun enableBayanInChat(telegramId: String) {
        changeChat(telegramId) {bayanEnabled = true}
    }

    private fun changeChat(telegramId: String, change: Chat.() -> Unit) {
        val chat = chatRepository.findByTelegramId(telegramId)
            ?: throw IllegalArgumentException("Chat with telegram id $telegramId not fount")
        chat.apply(change)
        chatRepository.saveAndFlush(chat)
    }

    @Cacheable(CHAT_CACHE)
    fun getEnabledChatByTelegramId(telegramId: String): Chat? {
        return chatRepository.findByTelegramIdAndEnabledTrue(telegramId)
    }

    @CacheEvict(CHAT_CACHE)
    fun cleanCache() {}
}

const val CHAT_CACHE = "CHAT_CACHE"
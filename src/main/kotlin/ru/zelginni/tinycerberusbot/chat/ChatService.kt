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

    @Cacheable(ALL_CHAT_CACHE)
    fun getAllChats(): List<ChatViewDto> {
        return chatRepository.findAll().map { chat -> chat.toViewModel() }
    }

    fun disableChat(telegramId: String) {
        changeChat(telegramId) {enabled = false}
    }

    fun enableChat(telegramId: String) {
        changeChat(telegramId) {enabled = true}
    }

    fun disableFeatureInChat(telegramId: String, chatFeature: ChatFeature) {
        changeChat(telegramId) {chatFeature.disable(this)}
    }

    fun enableFeatureInChat(telegramId: String, chatFeature: ChatFeature) {
        changeChat(telegramId) {chatFeature.enable(this)}
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

    @CacheEvict(ALL_CHAT_CACHE, CHAT_CACHE)
    fun cleanCache() {}
}

const val ALL_CHAT_CACHE = "ALL_CHAT_CACHE"
const val CHAT_CACHE = "CHAT_CACHE"
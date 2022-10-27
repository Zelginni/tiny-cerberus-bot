package ru.zelginni.tinycerberusbot.digest

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatViewDto

import java.time.LocalDateTime

@Service
class DigestService(
        private val digestRepository: DigestRepository
) {
    fun addDigest(chat: Chat, linkToMessage: String, description: String): Digest {
        val newDigest = Digest(chat = chat, linkToMessage = linkToMessage,
                description = description, createdOn = LocalDateTime.now())
        digestRepository.saveAndFlush(newDigest)
        return newDigest
    }

    fun fetchDigest(chat: ChatViewDto): List<Digest>? {
        return chat.telegramId?.let { digestRepository.findAllByChatIdAndCreatedOnBetween(it.toLong(), LocalDateTime.now().minusHours(24L), LocalDateTime.now()) }
    }
}
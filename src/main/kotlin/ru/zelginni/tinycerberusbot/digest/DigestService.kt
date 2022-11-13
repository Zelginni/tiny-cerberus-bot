package ru.zelginni.tinycerberusbot.digest

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatViewDto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DigestService(
        private val digestRepository: DigestRepository
) {
    private val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun addDigest(chat: Chat, linkToMessage: String, description: String): Digest {
        val newDigest = Digest(chat = chat, linkToMessage = linkToMessage,
                description = description, createdOn = LocalDateTime.now())
        digestRepository.saveAndFlush(newDigest)
        return newDigest
    }

    fun compileDigest(chatId: Long): String? {
        val digest: List<Digest> = fetchDigest(chatId)?.takeIf { it.isNotEmpty() } ?: return null
        val digestList = StringBuilder()
        for (digestEntry in digest) {
            digestList.append("\n")
            digestList.append(digestEntry.linkToMessage)
            digestList.append(" ")
            digestList.append(digestEntry.createdOn?.format(format))
            digestList.append("\n")
            digestList.append(digestEntry.description)
            digestList.append("\n")
        }
        return "Дайджест за сутки:\n$digestList"
    }

    fun fetchDigest(chatId: Long): List<Digest>? {
        return digestRepository.findAllByChatIdAndCreatedOnBetween(
            chatId, LocalDateTime.now().minusHours(24L), LocalDateTime.now())
    }
}


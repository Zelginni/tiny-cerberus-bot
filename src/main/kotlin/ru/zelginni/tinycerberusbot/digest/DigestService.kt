package ru.zelginni.tinycerberusbot.digest

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatService
import java.time.Instant

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class DigestService(
        private val digestRepository: DigestRepository,
        private val pinnedDigestRepository: PinnedDigestRepository,
        private val chatService: ChatService
) {
    private val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun addDigest(chat: Chat, linkToMessage: String, description: String, repliedMessageDate: Int): Digest {
        val newDigest = Digest(chat = chat, linkToMessage = linkToMessage,
                description = description, createdOn = LocalDateTime.ofInstant(Instant.ofEpochSecond(repliedMessageDate.toLong()), ZoneId.systemDefault()))
        digestRepository.saveAndFlush(newDigest)
        return newDigest
    }

    fun compileDigest(chatId: Long): String? {
        val digest: List<Digest>? = fetchDigest(chatId)
        if (digest.isNullOrEmpty()) {
            return ""
        }
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

    @Transactional
    fun addPinnedDigest(telegramChatId: String, pinnedMessageId: Int): PinnedDigest {
        val chat = chatService.getEnabledChatByTelegramId(telegramChatId)
        val newPinnedDigest = PinnedDigest(chat = chat, pinnedMessageId = pinnedMessageId,
                createdOn = LocalDateTime.now())
        pinnedDigestRepository.saveAndFlush(newPinnedDigest)
        return newPinnedDigest
    }

    @Transactional
    fun deleteDigest(telegramChatId: String) {
        val chat = chatService.getEnabledChatByTelegramId(telegramChatId) ?: return
        digestRepository.deleteAllByChatId(chat.id ?: throw IllegalStateException("How does chat not have its id?!"))
    }

    fun fetchOutdatedDigests(): List<PinnedDigest>? {
        return pinnedDigestRepository.findByCreatedOnBefore(LocalDateTime.now().minusHours(30L))
    }

    @Transactional
    fun deletePinnedDigests() {
        pinnedDigestRepository.deleteByCreatedOnBefore(LocalDateTime.now().minusHours(30L))
    }
}


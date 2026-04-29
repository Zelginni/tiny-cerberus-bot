package ru.zelginni.tinycerberusbot.digest

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.chat.ChatViewDto
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Service
class DigestPublisher(
    private val chatService: ChatService,
    private val digestService: DigestService,
    private val telegramCommandSender: TelegramCommandSender,
) {

    @Scheduled(cron = "\${bot.digest.cron}")
    fun publishDailyDigest() {
        chatService.getAllChats()
            .filter { chat ->
                chat.enabled == true
                    && chat.digestEnabled == true
                    && chat.id != null
                    && chat.telegramId != null
            }
            .forEach { chat -> publishDigest(chat) }

        cleanDigestPins()
    }

    private fun publishDigest(chat: ChatViewDto) {
        val chatId = chat.telegramId ?: return
        val digest = digestService.compileDigest(chat.id ?: return) ?: return
        if (digest.isBlank()) {
            telegramCommandSender.sendMessage(chatId.toLong(), "За прошедшие сутки в дайджест ничего не добавили.")
            return
        }

        val message = telegramCommandSender.sendSilentMessage(chatId, digest) ?: return
        val messageId = message.messageId ?: return
        telegramCommandSender.pinMessage(message.chatId.toString(), messageId, true)
        digestService.addPinnedDigest(chatId, messageId)
        digestService.deleteDigest(chatId)
    }

    private fun cleanDigestPins() {
        digestService.fetchOutdatedDigests()
            ?.filter { it.chat?.telegramId != null && it.pinnedMessageId != null }
            ?.forEach {
                telegramCommandSender.unpinMessage(it.chat?.telegramId ?: "", it.pinnedMessageId ?: return@forEach)
            }
        digestService.deletePinnedDigests()
    }
}

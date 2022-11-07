package ru.zelginni.tinycerberusbot.bayan

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import ru.zelginni.tinycerberusbot.chat.ChatService

@Service
class BayanService(
        private val bayanRepository: BayanRepository,
        private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(BayanService::class.java)

    fun respondToBayan(update: Update): Bayan? {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        logger.debug("Try to answer to bayan in chat $chat")
        return if(chat?.bayanEnabled == true) bayanRepository.fetchBayanResponse() else null
    }
}
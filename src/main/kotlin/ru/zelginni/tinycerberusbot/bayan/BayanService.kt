package ru.zelginni.tinycerberusbot.bayan

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import ru.zelginni.tinycerberusbot.chat.ChatService

@Service
class BayanService(
        private val bayanRepository: BayanRepository,
        private val chatService: ChatService
) {
    fun respondToBayan(update: Update): Bayan? {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        return if(chat?.bayanEnabled == true) bayanRepository.fetchBayanResponse() else null
    }
}
package ru.zelginni.tinycerberusbot.voice

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import ru.zelginni.tinycerberusbot.chat.ChatService

@Service
class VoiceResponseService(
        private val voiceResponseRepository: VoiceResponseRepository,
        private val chatService: ChatService
) {
    fun respondToVoice(update: Update): VoiceResponse? {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        return if(chat?.voiceResponseEnabled == true) voiceResponseRepository.fetchVoiceResponse() else null
    }
}
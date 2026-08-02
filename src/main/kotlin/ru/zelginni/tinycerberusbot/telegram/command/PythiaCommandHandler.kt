package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.pythia.PythiaPredictionService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class PythiaCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val predictionService: PythiaPredictionService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {
    override val commandName = "pythia"

    override val requiresAdmin = false

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null || chat.pythiaEnabled != true) {
            reply(command, "Пифия сейчас спит и не дает предсказаний в этом чате.")
            return
        }

        val prediction = predictionService.getRandomPrediction()
            ?: return reply(command, "Пифия молчит: пока нет ни одного предсказания.")

        reply(command, prediction.text)
    }
}

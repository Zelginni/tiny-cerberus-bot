package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatFeature
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class FeatureCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "feature"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null) {
            reply(command, "Аид запретил мне кусаться в этом чате.")
            return
        }

        val features = ChatFeature.values().joinToString(separator = "\n") { feature ->
            "- ${feature.displayName}: ${feature.statusText(chat)}"
        }

        reply(command, "Фичи чата:\n$features")
    }

    private fun ChatFeature.statusText(chat: Chat): String =
        if (isEnabled(chat)) "включено" else "выключено"
}

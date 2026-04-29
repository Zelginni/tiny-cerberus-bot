package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class StatusCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    administrationService: TelegramChatAdministrationService,
    private val chatService: ChatService,
) : AbstractCommandHandler(telegramCommandSender, administrationService) {

    override val commandName = "status"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        reply(command, if (chat == null) "Аид запретил мне кусаться в этом чате." else "Здесь я могу кусаться.")
    }
}

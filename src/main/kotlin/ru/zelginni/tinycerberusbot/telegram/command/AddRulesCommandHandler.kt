package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class AddRulesCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    administrationService: TelegramChatAdministrationService,
    private val chatService: ChatService,
    private val rulesService: RulesService,
) : AbstractCommandHandler(telegramCommandSender, administrationService) {

    override val commandName = "addrules"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            reply(command, "Аид запретил мне оперировать правилами в этом чате.")
            return
        }
        if (!command.hasOnlyText) {
            reply(command, "Правила не могут быть без текста или содержать в себе что-либо, кроме текста.")
            return
        }

        val text = command.arguments
        if (text.isNullOrBlank()) {
            reply(command, "Правила не могут быть без текста.")
            return
        }
        if (text.length > 3000) {
            reply(command, "Объем правил не может превышать 3000 символов.")
            return
        }

        rulesService.addRules(chat, text)
        reply(command, "Правила чата обновлены.")
    }
}

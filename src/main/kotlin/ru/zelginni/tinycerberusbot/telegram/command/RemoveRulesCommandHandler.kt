package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class RemoveRulesCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val rulesService: RulesService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "removerules"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            reply(command, "Аид запретил мне оперировать правилами в этом чате.")
            return
        }

        if (rulesService.getRules(chat) == null) {
            reply(command, "В этом чате уже отсутствуют правила.")
        } else {
            rulesService.removeRules(chat)
            reply(command, "Правила чата успешно удалены. Анархия, ня ^_^")
        }
    }
}

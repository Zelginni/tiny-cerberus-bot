package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class RulesCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val rulesService: RulesService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "rules"

    override val requiresAdmin = false

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            reply(command, "Аид запретил мне оперировать правилами в этом чате.")
            return
        }

        val rules = rulesService.getRules(chat)
            ?: return reply(command, "В этом чате отсутствуют правила.")

        reply(command, "Правила чата:\n\n${rules.ruleset}")
    }
}

package ru.zelginni.tinycerberusbot.rules

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMembers
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Service
class NewChatMembersGreetingService(
    private val chatService: ChatService,
    private val rulesService: RulesService,
    private val telegramCommandSender: TelegramCommandSender,
) {

    fun greet(members: IncomingChatMembers) {
        if (members.members.isEmpty() || members.members.any { it.isBot }) {
            return
        }

        val chat = chatService.getEnabledChatByTelegramId(members.chatId.toString())
        if (chat?.rulesEnabled == false) {
            return
        }

        val ruleSet = chat?.let { rulesService.getRules(it)?.ruleset }
        val rules = if (ruleSet == null) {
            "Правил у нас пока нет, располагайся."
        } else {
            "Ознакомься с правилами чата:\n$ruleSet"
        }
        val names = members.members.joinToString(" ") { "@${it.username}" }
        telegramCommandSender.sendReplyMessage(
            members.chatId,
            members.messageId,
            "Привет, $names!\n\n$rules",
            members.messageThreadId,
        )
    }
}

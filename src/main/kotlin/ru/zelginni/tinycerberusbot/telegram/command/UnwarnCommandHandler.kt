package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import ru.zelginni.tinycerberusbot.user.UserService

@Component
class UnwarnCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val userService: UserService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "unwarn"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
            ?: return reply(command, "Аид запретил мне кусаться в этом чате.")
        val repliedMessage = command.replyTo
            ?: return reply(command, "Не вижу реплай. Если он есть, попробуйте сообщение посвежее")

        val user = userService.createOrGetUser(repliedMessage.userId.toString(), repliedMessage.writableName(), chat)
        val warnCount = userService.deleteOneWarnAndReturnWarnCount(user)
        if (warnCount < 0) {
            reply(command, "У @${repliedMessage.writableName()} не было варнов.")
        } else {
            reply(command, "Один варн для @${repliedMessage.writableName()} удален. Количество варнов: $warnCount.")
        }
    }
}

package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import ru.zelginni.tinycerberusbot.user.UserService

@Component
class WarnCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    administrationService: TelegramChatAdministrationService,
    private val chatService: ChatService,
    private val userService: UserService,
) : AbstractCommandHandler(telegramCommandSender, administrationService) {

    override val commandName = "warn"

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
            ?: return reply(command, "Аид запретил мне кусаться в этом чате.")
        val repliedMessage = command.replyTo
            ?: return reply(command, "Не вижу реплай. Если он есть, попробуйте сообщение посвежее")

        if (isAdmin(command.chatId, repliedMessage.userId)) {
            reply(command, "Админов я кусать не буду.")
            return
        }

        val user = userService.createOrGetUser(repliedMessage.userId.toString(), repliedMessage.writableName(), chat)
        val warnCount = userService.makeNewWarnAndReturnWarnCount(
            user,
            command.userId.toString(),
            command.authorName()
        )
        val warnLimit = chat.warnLimit ?: -1

        if (warnLimit > 0 && warnLimit <= warnCount) {
            reply(command, "Это был последний варн, @${repliedMessage.writableName()} получает бан.")
            banUser(command.chatId, repliedMessage.userId)
        } else {
            reply(command, "@${repliedMessage.writableName()} получает варн №$warnCount. ${getLimitText(warnLimit)}.")
        }
    }
}

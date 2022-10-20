package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.user.UserService

@Service
class CommandService(
    private val chatService: ChatService,
    private val userService: UserService
) {

    fun warn(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
            ?: return CommandResult(
                CommandStatus.Error,
                "Аид запретил мне кусаться в этом чате.",
                ResultAction.Print
            )
        val warnedUser = update.message.replyToMessage.from
        val user = userService.createOrGetUser(warnedUser.id.toString(), warnedUser.userName, chat)

        val warnAuthor = update.message.from
        val warnCount = userService.makeNewWarnAndReturnWarnCount(user, warnAuthor.id.toString(), warnAuthor.userName)
        val warnLimit = chat.warnLimit ?: -1
        return if (warnLimit > 0 && chat.warnLimit!! <= warnCount) {
            CommandResult(
                CommandStatus.Success,
                "Это был последний варн, @${warnedUser.userName} получает бан.",
                ResultAction.Ban
            )
        } else {
            val limitText = if (warnLimit > 0) "равен $warnLimit" else "не установлен"
            CommandResult(
                CommandStatus.Success,
                "@${warnedUser.userName} получает варн №$warnCount. Лимит варнов в чате $limitText.",
                ResultAction.Print
            )
        }
    }

    fun status(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        return CommandResult(
            CommandStatus.Success,
            if (chat == null) "Аид запретил мне кусаться в этом чате." else "Здесь я могу кусаться.",
            ResultAction.Print
        )
    }

}
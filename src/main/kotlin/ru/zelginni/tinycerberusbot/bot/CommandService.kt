package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.digest.DigestService
import ru.zelginni.tinycerberusbot.user.UserService

@Service
class CommandService(
    private val chatService: ChatService,
    private val userService: UserService,
    private val digestService: DigestService
) {

    fun warn(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
            ?: return CommandResult(
                CommandStatus.Error,
                "Аид запретил мне кусаться в этом чате."
            )
        val repliedMessage = update.message.replyToMessage
            ?: return CommandResult(
                CommandStatus.Error,
                "Не вижу реплай. Если он есть, попробуйте сообщение посвежее"
            )
        val warnedUser = repliedMessage.from
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
                "@${warnedUser.userName} получает варн №$warnCount. Лимит варнов в чате $limitText."
            )
        }
    }

    fun status(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        return CommandResult(
            CommandStatus.Success,
            if (chat == null) "Аид запретил мне кусаться в этом чате." else "Здесь я могу кусаться."
        )
    }

    fun digest(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat == null || chat.digestEnabled == false) {
            return CommandResult(
                CommandStatus.Error,
                "Аид запретил мне собирать дайджест в этом чате."
            )
        }
        val repliedMessage = update.message.replyToMessage
                ?: return CommandResult(
                        CommandStatus.Error,
                        "Не вижу реплай. Если он есть, попробуйте сообщение посвежее"
                )
        val linkToMessage = "https://t.me/c/${getChatIdForLink(chat.telegramId)}/${repliedMessage.messageId}"
        digestService.addDigest(chat, linkToMessage, getDescription(update))
        return CommandResult(
                CommandStatus.Success, "Добавлено."
        )
    }

    private fun getDescription(update: Update): String {
        val text = update.message.text
        val descriptionStart = text.indexOf(' ')
        if (descriptionStart < 0) {
            return "добавил(а) @${update.message.from.userName}"
        }
        return text.substring(descriptionStart)
    }

    private fun getChatIdForLink(telegramId: String?): String? {
        return telegramId?.substring(4)
    }
}
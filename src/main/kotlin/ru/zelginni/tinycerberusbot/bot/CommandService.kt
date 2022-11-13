package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
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
                "Эта команда должна быть использована ответом на сообщение. Если оно есть, попробуйте сообщение посвежее."
            )
        if (repliedMessage.from.isBot
            && repliedMessage.text.contains("Дайджест")) {
            return CommandResult(
                CommandStatus.Error,
                "Наркоман штоле?"
            )
        }
        val linkToMessage = "https://t.me/c/${getChatIdForLink(chat.telegramId)}/${repliedMessage.messageId}"
        digestService.addDigest(chat, linkToMessage, getDescription(update), repliedMessage.date)
        return CommandResult(
                CommandStatus.Success, "Добавлено."
        )
    }

    private fun getDescription(update: Update): String {
        val text = update.message.text
        val beginOfDescriptionIndex = text.indexOf(' ')
        return if (beginOfDescriptionIndex == -1
                || beginOfDescriptionIndex == text.length) {
            createDescription(update.message.replyToMessage)
        } else {
            text.substring(beginOfDescriptionIndex)
        }
    }

    private fun createDescription(repliedMessage: Message): String {
        val firstName = repliedMessage.from.firstName
        val lastName = repliedMessage.from.lastName ?: ""
        val author = "${firstName.ifBlank { "" }} ${lastName.ifBlank { "" }}".trim()
        return if (repliedMessage.hasPhoto() && !repliedMessage.hasText()) {
            "Фото от $author"
        } else if (repliedMessage.hasDocument() && !repliedMessage.hasText()) {
            "Файл от $author"
        } else {
            val text = repliedMessage.text
            text.substring(0, if (text.length < 101) text.length else 101)
        }
    }

    private fun getChatIdForLink(telegramId: String?): String? {
        return telegramId?.substring(4)
    }
}
package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.digest.DigestService
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.user.UserService
import ru.zelginni.tinycerberusbot.warn.Warn
import java.time.format.DateTimeFormatter

import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

@Service
class CommandService(
    private val chatService: ChatService,
    private val userService: UserService,
    private val digestService: DigestService,
    private val rulesService: RulesService
) {

    private val warnTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

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
        val user = userService.createOrGetUser(warnedUser.id.toString(), warnedUser.writableName(), chat)

        val warnAuthor = update.message.from
        val warnCount = userService.makeNewWarnAndReturnWarnCount(user, warnAuthor.id.toString(), warnAuthor.userName)
        val warnLimit = chat.warnLimit ?: -1
        return if (warnLimit > 0 && chat.warnLimit!! <= warnCount) {
            CommandResult(
                CommandStatus.Success,
                "Это был последний варн, @${warnedUser.writableName()} получает бан.",
                ResultAction.Ban
            )
        } else {
            CommandResult(
                CommandStatus.Success,
                "@${warnedUser.writableName()} получает варн №$warnCount. ${getLimitText(warnLimit)}."
            )
        }
    }

    private fun TelegramUser.writableName(): String {
        if (userName != null) {
            return userName
        }
        return "$firstName ${lastName ?: ""}".trim()
    }

    private fun getLimitText(warnLimit: Int?): String {
        return "Лимит варнов в чате " + if ((warnLimit ?: -1) > 0) "равен $warnLimit" else "не установлен"
    }

    fun unWarn(update: Update): CommandResult {
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
        val user = userService.createOrGetUser(warnedUser.id.toString(), warnedUser.writableName(), chat)
        val warnCount = userService.deleteOneWarnAndReturnWarnCount(user)
        return if (warnCount < 0) {
            CommandResult(
                CommandStatus.Success,
                "У @${warnedUser.writableName()} не было варнов."
            )
        } else {
            CommandResult(
                CommandStatus.Success,
                "Один варн для @${warnedUser.writableName()} удален. Количество варнов: $warnCount."
            )
        }
    }

    fun statWarn(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
            ?: return CommandResult(
                CommandStatus.Error,
                "Аид запретил мне кусаться в этом чате."
            )
        val repliedMessage = update.message.replyToMessage
        val text = if (repliedMessage == null) {
            getAllStatWarn(chat)
        } else {
            getStatWarnByUser(chat, repliedMessage)
        }
        return CommandResult(CommandStatus.Success, text)
    }

    private fun getAllStatWarn(chat: Chat): String {
        val warns = userService.getWarnsByChat(chat)
        if (warns.isEmpty()) {
            return "В этом чате нет варнов."
        }
        val warnsByUser: Map<Long, List<Warn>> = warns.groupBy { warn -> warn.user?.id ?: -1 }
        val warnStatSb = StringBuilder()
        warnStatSb.append("Варны в чате:\n")
        warnsByUser.values.forEach {
            val user = it.first().user
            if (user != null) {
                warnStatSb.append("${user.username} — ${it.size}\n")
            }
        }
        warnStatSb.append(getLimitText(chat.warnLimit))
        return warnStatSb.toString()
    }

    private fun getStatWarnByUser(chat: Chat, repliedMessage: Message): String {
        val statUser = repliedMessage.from
        val user = userService.createOrGetUser(statUser.id.toString(), statUser.writableName(), chat)
        val warns = userService.getWarnsByUser(user)
        if (warns.isEmpty()) {
            return "У @${statUser.writableName()} нет варнов."
        }
        val warnStatSb = StringBuilder()
        warnStatSb.append("Варны @${statUser.writableName()}:\n")
        warns.sortedBy { warn -> warn.dateCreated }.forEach { warn ->
            warnStatSb.append("${warnTimeFormatter.format(warn.dateCreated?.toLocalDateTime())} от ${warn.authorUsername}.\n")
        }
        warnStatSb.append("Всего варнов: ${warns.size}.\n")
        warnStatSb.append(getLimitText(chat.warnLimit))
        return warnStatSb.toString()
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
                ?: return CommandResult(
                CommandStatus.Error,
                "Это сообщение уже добавили в дайджест. Вы опоздали :("
        )
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

    fun addRules(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            return CommandResult(
                    CommandStatus.Error,
                    "Аид запретил мне оперировать правилами в этом чате."
            )
        }
        if (!makeSureTheresOnlyTextInRules(update)) {
            return CommandResult(
                    CommandStatus.Error,
                    "Правила не могут быть без текста или содержать в себе что-либо, кроме текста."
            )
        }
        if (update.message.text.length > 3000) {
            return CommandResult(
                    CommandStatus.Error,
                    "Объем правил не может превышать 3000 символов."
            )
        }
        val text = getTextForRules(update)
        if (text.isNullOrBlank()) {
            return CommandResult(
                    CommandStatus.Error,
                    "Правила не могут быть без текста."
            )
        }
        rulesService.addRules(chat, text)
        return CommandResult(
                CommandStatus.Success,
                "Правила чата обновлены."
        )
    }

    private fun getTextForRules(update: Update): String? {
        val text = update.message.text
        val beginOfDescriptionIndex = text.indexOf(' ')
        return if(beginOfDescriptionIndex == -1
                || beginOfDescriptionIndex == text.length) {
            null
        } else {
            text.substring(beginOfDescriptionIndex + 1)
        }
    }

    @Transactional
    fun removeRules(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            return CommandResult(
                    CommandStatus.Error,
                    "Аид запретил мне оперировать правилами в этом чате."
            )
        }
        return if (rulesService.getRules(chat) == null) {
            CommandResult(
                    CommandStatus.Error,
                    "В этом чате уже отсутствуют правила."
            )
        } else {
            rulesService.removeRules(chat)
            CommandResult(
                    CommandStatus.Success,
                    "Правила чата успешно удалены. Анархия, ня ^_^"
            )
        }
    }

    fun getRules(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat == null || chat.rulesEnabled == false) {
            return CommandResult(
                    CommandStatus.Error,
                    "Аид запретил мне оперировать правилами в этом чате."
            )
        }
        val rules = rulesService.getRules(chat)
                ?: return CommandResult(
                        CommandStatus.Error,
                        "В этом чате отсутствуют правила."
                )
        return CommandResult(
                CommandStatus.Success,
                "Правила чата:\n\n${rules.ruleset}"
        )
    }

    private fun makeSureTheresOnlyTextInRules(update: Update): Boolean {
        return update.message.hasText()
                && !(update.message.hasDocument()
                || update.message.hasPhoto()
                || update.message.hasDice()
                || update.message.hasPoll()
                || update.message.hasAnimation()
                || update.message.hasAudio()
                || update.message.hasContact()
                || update.message.hasInvoice()
                || update.message.hasLocation()
                || update.message.hasPassportData()
                || update.message.hasReplyMarkup()
                || update.message.hasSticker()
                || update.message.hasSuccessfulPayment()
                || update.message.hasViaBot()
                || update.message.hasVideo()
                || update.message.hasVideoNote()
                || update.message.hasVoice())
    }
}
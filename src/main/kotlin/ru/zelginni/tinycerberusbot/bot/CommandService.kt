package ru.zelginni.tinycerberusbot.bot

import org.apache.commons.collections4.map.PassiveExpiringMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.user.UserService
import ru.zelginni.tinycerberusbot.warn.WarnRepository
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

@Service
class CommandService(
        private val chatService: ChatService,
        private val userService: UserService,
        private val warnRepository: WarnRepository
) {

    @Value("\${bot.amnestyopportunity:600}")
    private val amnestyOpportunity: Long = 600

    @Value(("\${bot.needvotesforamnesty:3}"))
    private val needVotesForAmnesty: Int = 3

    private val amnesty =
            Collections.synchronizedMap(PassiveExpiringMap(
                    PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy(
                            amnestyOpportunity, TimeUnit.SECONDS),
                    HashMap<Long, User>()))

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

    fun amnesty(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        val voterForAmnesty = update.message.from
        if (amnesty.size >= needVotesForAmnesty) {
            if (chat != null) {
                chat.id?.let { warnRepository.removeAllByChatId(it) }
            }
            return CommandResult(
                    CommandStatus.Success,
                    "Объявлена амнистия! Количество варнов уменьшено до нуля!"
            )
        } else {
            if (chat != null) {
                amnesty[chat.id] = voterForAmnesty
            }
            return CommandResult(
                    CommandStatus.Success,
                    "@${voterForAmnesty.userName} голосует за амнистию! До окончания голосования осталось $amnestyOpportunity секунд! " +
                            "Голосов за амнистию должно быть: $needVotesForAmnesty. Сейчас голосов: ${amnesty.size}."
            )
        }
    }
}
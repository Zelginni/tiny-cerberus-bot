package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.statistics.ChatUserWarnDetailsView
import ru.zelginni.tinycerberusbot.statistics.ChatWarnStatisticsView
import ru.zelginni.tinycerberusbot.statistics.WarnStatisticsService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import java.time.format.DateTimeFormatter

@Component
class StatWarnCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val warnStatisticsService: WarnStatisticsService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "statwarn"

    private val warnTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
            ?: return reply(command, "Аид запретил мне кусаться в этом чате.")

        val text = command.replyTo
            ?.let { getStatWarnByUser(warnStatisticsService.getUserWarnStatistics(chat, it)) }
            ?: getAllStatWarn(warnStatisticsService.getChatWarnStatistics(chat))
        reply(command, text)
    }

    private fun getAllStatWarn(statistics: ChatWarnStatisticsView): String {
        if (statistics.users.isEmpty()) {
            return "В этом чате нет варнов."
        }

        val warnStat = StringBuilder()
        warnStat.append("Варны в чате:\n")
        statistics.users.forEach { user ->
            warnStat.append("${user.username} — ${user.warnCount}\n")
        }
        warnStat.append(getLimitText(statistics.warnLimit))
        return warnStat.toString()
    }

    private fun getStatWarnByUser(statistics: ChatUserWarnDetailsView): String {
        if (statistics.warns.isEmpty()) {
            return "У @${statistics.username} нет варнов."
        }

        val warnStat = StringBuilder()
        warnStat.append("Варны @${statistics.username}:\n")
        statistics.warns.forEach { warn ->
            val date = warn.dateCreated?.let { warnTimeFormatter.format(it) } ?: ""
            warnStat.append("$date от ${warn.authorUsername}.\n")
        }
        warnStat.append("Всего варнов: ${statistics.warnCount}.\n")
        warnStat.append(getLimitText(statistics.warnLimit))
        return warnStat.toString()
    }
}

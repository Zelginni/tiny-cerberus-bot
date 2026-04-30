package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import ru.zelginni.tinycerberusbot.user.UserService
import ru.zelginni.tinycerberusbot.warn.Warn
import java.time.format.DateTimeFormatter

@Component
class StatWarnCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val userService: UserService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "statwarn"

    private val warnTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
            ?: return reply(command, "Аид запретил мне кусаться в этом чате.")

        val text = command.replyTo?.let { getStatWarnByUser(chat, it) } ?: getAllStatWarn(chat)
        reply(command, text)
    }

    private fun getAllStatWarn(chat: Chat): String {
        val warns = userService.getWarnsByChat(chat)
        if (warns.isEmpty()) {
            return "В этом чате нет варнов."
        }

        val warnsByUser: Map<Long, List<Warn>> = warns.groupBy { warn -> warn.user?.id ?: -1 }
        val warnStat = StringBuilder()
        warnStat.append("Варны в чате:\n")
        warnsByUser.values.forEach {
            val user = it.first().user
            if (user != null) {
                warnStat.append("${user.username} — ${it.size}\n")
            }
        }
        warnStat.append(getLimitText(chat.warnLimit))
        return warnStat.toString()
    }

    private fun getStatWarnByUser(chat: Chat, repliedMessage: IncomingChatMessage): String {
        val user = userService.createOrGetUser(repliedMessage.userId.toString(), repliedMessage.writableName(), chat)
        val warns = userService.getWarnsByUser(user)
        if (warns.isEmpty()) {
            return "У @${repliedMessage.writableName()} нет варнов."
        }

        val warnStat = StringBuilder()
        warnStat.append("Варны @${repliedMessage.writableName()}:\n")
        warns.sortedBy { warn -> warn.dateCreated }.forEach { warn ->
            val date = warn.dateCreated?.toLocalDateTime()?.let { warnTimeFormatter.format(it) } ?: ""
            warnStat.append("$date от ${warn.authorUsername}.\n")
        }
        warnStat.append("Всего варнов: ${warns.size}.\n")
        warnStat.append(getLimitText(chat.warnLimit))
        return warnStat.toString()
    }
}

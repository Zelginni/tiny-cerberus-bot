package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.statistics.StatisticsFeatureService
import ru.zelginni.tinycerberusbot.statistics.TopChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramApiCommandSender
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class TopChatMembersCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val statisticsFeatureService: StatisticsFeatureService,
    private val topChatMemberService: TopChatMemberService,
    private val messageSender: TelegramApiCommandSender,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "top"

    override fun handleCommand(command: ChatCommand) {
        if (!statisticsFeatureService.isEnabled(command.chatId)) {
            messageSender.sendMessage(command.chatId, statisticsFeatureService.disabledMessage(), command.messageThreadId)
            return
        }

        val top = topChatMemberService.getTopMembers(command.chatId)
        val text = CommandResponseFormatter.formatNumberedStatistics(
            title = "Топ-5 участников по сообщениям:",
            items = top,
            emptyText = "Пока никто ничего не написал",
            displayName = { it.displayName },
            messageCount = { it.messageCount },
        )

        messageSender.sendMessage(command.chatId, text, command.messageThreadId)
    }
}

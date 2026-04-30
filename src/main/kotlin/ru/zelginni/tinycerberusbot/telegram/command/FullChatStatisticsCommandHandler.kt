package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.statistics.FullChatStatisticsService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramApiCommandSender
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class FullChatStatisticsCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val fullChatStatisticsService: FullChatStatisticsService,
    private val messageSender: TelegramApiCommandSender
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName: String = "stats"

    override fun handleCommand(command: ChatCommand) {
        val statistics = fullChatStatisticsService.getLimitedStatistics(command.chatId)
        val text = CommandResponseFormatter.formatNumberedStatistics(
            title = "Статистика участников:",
            items = statistics,
            emptyText = "Пока никто ничего не написал",
            displayName = { it.displayName },
            messageCount = { it.messageCount },
        )

        messageSender.sendMessage(command.chatId, text)
    }
}

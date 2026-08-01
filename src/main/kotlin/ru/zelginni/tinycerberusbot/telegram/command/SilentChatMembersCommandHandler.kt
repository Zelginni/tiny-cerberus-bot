package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.statistics.SilentChatMemberView
import ru.zelginni.tinycerberusbot.statistics.StatisticsFeatureService
import ru.zelginni.tinycerberusbot.statistics.SilentChatMemberService
import ru.zelginni.tinycerberusbot.statistics.SilentChatMemberService.Companion.DEFAULT_SILENCE_DAYS
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramApiCommandSender
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class SilentChatMembersCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val statisticsFeatureService: StatisticsFeatureService,
    private val silentChatMemberService: SilentChatMemberService,
    private val messageSender: TelegramApiCommandSender,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService)  {
    override val commandName = "silent"

    override fun handleCommand(command: ChatCommand) {
        if (!statisticsFeatureService.isEnabled(command.chatId)) {
            messageSender.sendMessage(command.chatId, statisticsFeatureService.disabledMessage(), command.messageThreadId)
            return
        }

        val days = command.silenceDays()
        val silentMembers = silentChatMemberService.getSilentMembers(command.chatId, days)

        val text = if (silentMembers.isEmpty()) {
            "Все писали последние $days дней"
        } else {
            silentMembers.joinToString(
                separator = "\n",
                prefix = "Не писали последние $days дней:\n",
            ) { it.displayNameWithUsername() }
        }

        messageSender.sendMessage(command.chatId, text, command.messageThreadId)
    }

    private fun SilentChatMemberView.displayNameWithUsername(): String =
        username?.let { "$displayName (@$it)" } ?: displayName

    private fun ChatCommand.silenceDays(): Long =
        arguments
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it > 0 }
            ?: DEFAULT_SILENCE_DAYS
}

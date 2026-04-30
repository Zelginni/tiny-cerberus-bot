package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.chat.DEFAULT_FULL_STATISTICS_LIMIT
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramApiCommandSender
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class HelpCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val messageSender: TelegramApiCommandSender,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService)  {

    override val commandName = "help"

    override val requiresAdmin = false

    override fun handleCommand(command: ChatCommand) {
        val fullStatisticsLimit = chatService.getFullStatisticsLimit(command.chatId)
        val limitText = if (fullStatisticsLimit == DEFAULT_FULL_STATISTICS_LIMIT) {
            "Лимит полной статистики не установлен"
        } else {
            "Лимит полной статистики: $fullStatisticsLimit"
        }

        messageSender.sendMessage(
            command.chatId,
            "Доступные команды:\n" +
                "/help — показать эту справку\n" +
                "/status — проверить, могу ли я работать в этом чате\n" +
                "/silent [дни] — показать участников, которые не писали указанное количество дней, по умолчанию 30\n" +
                "/top — показать топ-5 участников по количеству сообщений\n" +
                "/stats — показать полную статистику участников\n\n" +
                limitText,
        )
    }
}

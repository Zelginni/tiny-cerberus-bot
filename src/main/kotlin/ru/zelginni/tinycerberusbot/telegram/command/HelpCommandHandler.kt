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
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        val fullStatisticsLimit = chatService.getFullStatisticsLimit(command.chatId)
        val fullStatisticsLimitText = if (fullStatisticsLimit == DEFAULT_FULL_STATISTICS_LIMIT) {
            "Лимит полной статистики не установлен"
        } else {
            "Лимит полной статистики: $fullStatisticsLimit"
        }
        val warnLimitText = if ((chat?.warnLimit ?: -1) > 0) {
            "Лимит варнов: ${chat?.warnLimit}"
        } else {
            "Лимит варнов не установлен"
        }

        messageSender.sendMessage(
            command.chatId,
            "Доступные команды:\n" +
                "/help — показать эту справку\n" +
                "/status — проверить, могу ли я работать в этом чате\n" +
                "/warn — выдать предупреждение пользователю, используется ответом на сообщение\n" +
                "/unwarn — снять одно предупреждение с пользователя, используется ответом на сообщение\n" +
                "/statwarn — показать статистику варнов по чату или пользователю, если использовать ответом на сообщение\n" +
                "/digest [описание] — добавить сообщение в дневной дайджест, используется ответом на сообщение\n" +
                "/addrules [текст] — добавить или заменить правила чата\n" +
                "/rules — показать правила чата\n" +
                "/removerules — удалить правила чата\n" +
                "/silent [дни] — показать участников, которые не писали указанное количество дней, по умолчанию 30\n" +
                "/top — показать топ-5 участников по количеству сообщений\n" +
                "/stats — показать полную статистику участников\n\n" +
                "Настройки чата:\n" +
                "$warnLimitText\n" +
                fullStatisticsLimitText,
        )
    }
}

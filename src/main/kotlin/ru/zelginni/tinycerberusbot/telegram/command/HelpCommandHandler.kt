package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.Chat
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
        messageSender.sendMessage(command.chatId, buildHelpText(chat))
    }

    private fun buildHelpText(chat: Chat?): String {
        val sections = mutableListOf(
            commandSection(
                "Основные команды",
                "/help — показать эту справку",
                "/status — проверить, могу ли я работать в этом чате",
                "/feature — показать включенные и выключенные фичи чата",
            ),
            commandSection(
                "Варны",
                "/warn — выдать предупреждение пользователю, используется ответом на сообщение",
                "/unwarn — снять одно предупреждение с пользователя, используется ответом на сообщение",
                "/statwarn — показать статистику варнов по чату или пользователю, если использовать ответом на сообщение",
            ),
        )

        if (chat?.digestEnabled == true) {
            sections.add(
                commandSection(
                    "Дайджест",
                    "/digest [описание] — добавить сообщение в дневной дайджест, используется ответом на сообщение",
                )
            )
        }

        if (chat?.rulesEnabled == true) {
            sections.add(
                commandSection(
                    "Правила",
                    "/rules — показать правила чата",
                    "/addrules [текст] — добавить или заменить правила чата",
                    "/removerules — удалить правила чата",
                )
            )
        }

        if (chat?.statisticsEnabled == true) {
            sections.add(
                commandSection(
                    "Статистика",
                    "/silent [дни] — показать участников, которые не писали указанное количество дней, по умолчанию 30",
                    "/top — показать топ-5 участников по количеству сообщений",
                    "/stats — показать полную статистику участников",
                )
            )
        }

        sections.add(settingsSection(chat))
        return sections.joinToString(separator = "\n\n")
    }

    private fun commandSection(title: String, vararg commands: String): String =
        "$title:\n" + commands.joinToString(separator = "\n")

    private fun settingsSection(chat: Chat?): String {
        val settings = mutableListOf(
            if ((chat?.warnLimit ?: -1) > 0) {
                "Лимит варнов: ${chat?.warnLimit}"
            } else {
                "Лимит варнов не установлен"
            }
        )

        if (chat?.statisticsEnabled == true) {
            settings.add(fullStatisticsLimitText(chat.fullStatisticsLimit))
        }

        return "Настройки чата:\n" + settings.joinToString(separator = "\n")
    }

    private fun fullStatisticsLimitText(fullStatisticsLimit: Int): String =
        if (fullStatisticsLimit == DEFAULT_FULL_STATISTICS_LIMIT) {
            "Лимит полной статистики не установлен"
        } else {
            "Лимит полной статистики: $fullStatisticsLimit"
        }
}

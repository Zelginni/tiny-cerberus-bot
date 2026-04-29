package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage

@Component
class CommandParser {
    fun parse(message: IncomingChatMessage): ChatCommand? {
        val text = message.text?.trim().orEmpty()
        if (!text.startsWith("/") && !text.startsWith("!")) {
            return null
        }

        val parts = text.split(Regex("\\s+"), limit = 2)
        val command = parts.first().drop(1).substringBefore("@").lowercase()
        val arguments = parts.getOrNull(1)?.takeIf { it.isNotBlank() }

        return ChatCommand(
            chatId = message.chatId,
            userId = message.userId,
            command = command,
            arguments = arguments,
        )
    }
}

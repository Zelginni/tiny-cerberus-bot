package ru.zelginni.tinycerberusbot.telegram.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import java.time.Instant

class CommandParserTest {

    private val parser = CommandParser()

    @Test
    fun parseSlashCommandWithBotName() {
        val command = parser.parse(message("/warn@tinycerberus_bot reason"))

        assertEquals(expectedCommand("warn", "reason"), command)
    }

    @Test
    fun parseExclamationCommandWithoutBotName() {
        val command = parser.parse(message("!warn reason"))

        assertEquals(expectedCommand("warn", "reason"), command)
    }

    @Test
    fun ignoreRegularText() {
        assertNull(parser.parse(message("warn reason")))
    }

    private fun message(text: String): IncomingChatMessage =
        IncomingChatMessage(
            chatId = 1L,
            userId = 2L,
            messageId = 3,
            text = text,
            sentAt = Instant.EPOCH,
        )

    private fun expectedCommand(command: String, arguments: String?): ChatCommand =
        ChatCommand(
            chatId = 1L,
            userId = 2L,
            messageId = 3,
            command = command,
            arguments = arguments,
            sentAt = Instant.EPOCH,
            senderDisplayName = null,
            senderUsername = null,
            replyTo = null,
            hasOnlyText = true,
        )
}

package ru.zelginni.tinycerberusbot.bot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BotCommandTest {

    @Test
    fun selectCommandByMessageText() {
        val command = getCommand("/warn@tinycerberus_bot")
        assertEquals(BotCommand.Warn, command)
    }
}
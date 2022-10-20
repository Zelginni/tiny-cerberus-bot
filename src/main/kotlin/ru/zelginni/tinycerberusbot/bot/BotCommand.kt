package ru.zelginni.tinycerberusbot.bot

import org.telegram.telegrambots.meta.api.objects.Update

enum class BotCommand {
    Warn {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.warn(update)
        }
    },
    Status {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.status(update)
        }
    };

    abstract fun performCommand(commandService: CommandService, update: Update): CommandResult

}

fun getCommand(messageText: String): BotCommand? {
    return BotCommand.values().firstOrNull {
        it.name.lowercase() == messageText.substring(1, messageText.indexOf("@"))
    }
}
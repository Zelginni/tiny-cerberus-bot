package ru.zelginni.tinycerberusbot.bot

import org.telegram.telegrambots.meta.api.objects.Update

enum class BotCommand(val requireAdmin: Boolean = true) {
    Warn {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.warn(update)
        }
    },
    UnWarn {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.unWarn(update)
        }
    },
    StatWarn {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.statWarn(update)
        }
    },
    Status {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.status(update)
        }
    },
    Digest(requireAdmin = false) {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.digest(update)
        }
    },
    AddRules {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.addRules(update)
        }
    },
    RemoveRules {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.removeRules(update)
        }
    },
    Rules(requireAdmin = false) {
        override fun performCommand(commandService: CommandService, update: Update): CommandResult {
            return commandService.getRules(update)
        }
    };

    abstract fun performCommand(commandService: CommandService, update: Update): CommandResult

}

fun getCommand(messageText: String): BotCommand? {
    val endString = if(messageText.startsWith(ALT_COMMAND_START)) {
        val firstSpace = messageText.indexOf(" ")
        if (firstSpace > 0) {
            firstSpace
        } else {
            messageText.length
        }
    } else {
        messageText.indexOf("@")
    }
    return BotCommand.values().firstOrNull {
        it.name.lowercase() == messageText.substring(1, endString)
    }
}

const val ALT_COMMAND_START = "!"

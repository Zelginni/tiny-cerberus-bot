package ru.zelginni.tinycerberusbot.telegram.command

interface CommandHandler {
    val commandName: String

    val requiresAdmin: Boolean
        get() = true

    fun supports(command: ChatCommand): Boolean =
        command.command.equals(commandName, ignoreCase = true)

    fun handle(command: ChatCommand)
}

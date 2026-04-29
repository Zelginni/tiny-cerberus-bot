package ru.zelginni.tinycerberusbot.telegram.command

interface CommandHandler {
    fun supports(command: ChatCommand): Boolean

    fun handle(command: ChatCommand)
}

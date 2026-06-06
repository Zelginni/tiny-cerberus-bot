package ru.zelginni.tinycerberusbot.telegram

interface TelegramUpdateHandler {
    fun handleMessage(message: IncomingChatMessage)

    fun handleNewChatMembers(members: IncomingChatMembers)

    fun handleLeftChatMember(member: IncomingChatMemberLeft)
}

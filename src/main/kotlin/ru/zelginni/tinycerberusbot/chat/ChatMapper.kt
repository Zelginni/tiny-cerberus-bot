package ru.zelginni.tinycerberusbot.chat

fun ChatInsertDto.toDbModel(): Chat
    = Chat(telegramId = telegramId, name = name, warnLimit = warnLimit)

fun Chat.toViewModel(): ChatViewDto
    = ChatViewDto(
    name = name,
    telegramId = telegramId,
    enabled = enabled,
    warnLimit = warnLimit,
    bayanEnabled = bayanEnabled
)
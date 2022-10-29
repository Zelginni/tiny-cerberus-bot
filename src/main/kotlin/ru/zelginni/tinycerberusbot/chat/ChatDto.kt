package ru.zelginni.tinycerberusbot.chat

data class ChatInsertDto(
    var name: String? = null,
    var telegramId: String? = null,
    var warnLimit: Int? = 3
)

data class ChatViewDto(
    var name: String? = null,
    var telegramId: String? = null,
    var enabled: Boolean? = null,
    var warnLimit: Int? = null,
    var bayanEnabled: Boolean? = null
)

data class AllChatResponse(
    var chats: List<ChatViewDto>
)
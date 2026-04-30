package ru.zelginni.tinycerberusbot.chat

data class ChatInsertDto(
    var name: String? = null,
    var telegramId: String? = null,
    var warnLimit: Int? = 3,
    var fullStatisticsLimit: Int = DEFAULT_FULL_STATISTICS_LIMIT,
)

data class ChatViewDto(
    var id: Long? = null,
    var name: String? = null,
    var telegramId: String? = null,
    var enabled: Boolean? = null,
    var warnLimit: Int? = null,
    var bayanEnabled: Boolean? = null,
    var digestEnabled: Boolean? = null,
    var rulesEnabled: Boolean? = null,
    var statisticsEnabled: Boolean? = null,
    var fullStatisticsLimit: Int? = null,
)

data class AllChatResponse(
    var chats: List<ChatViewDto>
)

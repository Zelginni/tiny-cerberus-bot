package ru.zelginni.tinycerberusbot.chat

data class ChatInsertDto(
    var name: String? = null,
    var telegramId: String? = null,
    var warnLimit: Int? = 3,
    var fullStatisticsLimit: Int = DEFAULT_FULL_STATISTICS_LIMIT,
    var ignoredStatisticsMessageThreadIds: List<Int> = emptyList(),
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
    var ignoredStatisticsMessageThreadIds: List<Int> = emptyList(),
)

data class AllChatResponse(
    var chats: List<ChatViewDto>
)

data class IgnoredStatisticsTopicsRequest(
    var ignoredStatisticsMessageThreadIds: List<Int> = emptyList(),
)

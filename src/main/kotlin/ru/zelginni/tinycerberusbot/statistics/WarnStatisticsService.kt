package ru.zelginni.tinycerberusbot.statistics

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.user.UserService
import java.time.LocalDateTime

@Service
class WarnStatisticsService(
    private val chatService: ChatService,
    private val userService: UserService,
) {
    @Transactional(readOnly = true)
    fun getChatWarnStatistics(chatId: Long): ChatWarnStatisticsView? {
        val chat = chatService.getEnabledChatByTelegramId(chatId.toString()) ?: return null
        return getChatWarnStatistics(chat)
    }

    @Transactional(readOnly = true)
    fun getChatWarnStatistics(chat: Chat): ChatWarnStatisticsView {
        val users = userService.getWarnsByChat(chat)
            .groupBy { warn -> warn.user?.id }
            .values
            .mapNotNull { warns ->
                val user = warns.firstOrNull()?.user ?: return@mapNotNull null
                ChatUserWarnStatisticsView(
                    userId = user.id,
                    telegramId = user.telegramId,
                    username = user.username,
                    warnCount = warns.size,
                )
            }
            .sortedWith(compareByDescending<ChatUserWarnStatisticsView> { it.warnCount }.thenBy { it.username })

        return ChatWarnStatisticsView(
            chatId = chat.telegramId?.toLongOrNull(),
            warnLimit = chat.warnLimit,
            users = users,
        )
    }

    @Transactional
    fun getUserWarnStatistics(chat: Chat, message: IncomingChatMessage): ChatUserWarnDetailsView {
        val user = userService.createOrGetUser(message.userId.toString(), message.writableName(), chat)
        val warns = userService.getWarnsByUser(user)
            .sortedBy { warn -> warn.dateCreated }
            .map { warn ->
                WarnDetailsView(
                    dateCreated = warn.dateCreated?.toLocalDateTime(),
                    authorTelegramId = warn.authorTelegramId,
                    authorUsername = warn.authorUsername,
                )
            }

        return ChatUserWarnDetailsView(
            userId = user.id,
            telegramId = user.telegramId,
            username = message.writableName(),
            warnCount = warns.size,
            warnLimit = chat.warnLimit,
            warns = warns,
        )
    }

    private fun IncomingChatMessage.writableName(): String =
        senderUsername ?: senderDisplayName ?: userId.toString()
}

data class ChatWarnStatisticsView(
    val chatId: Long?,
    val warnLimit: Int?,
    val users: List<ChatUserWarnStatisticsView>,
)

data class ChatUserWarnStatisticsView(
    val userId: Long?,
    val telegramId: String?,
    val username: String?,
    val warnCount: Int,
)

data class ChatUserWarnDetailsView(
    val userId: Long?,
    val telegramId: String?,
    val username: String?,
    val warnCount: Int,
    val warnLimit: Int?,
    val warns: List<WarnDetailsView>,
)

data class WarnDetailsView(
    val dateCreated: LocalDateTime?,
    val authorTelegramId: String?,
    val authorUsername: String?,
)

package ru.zelginni.tinycerberusbot.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import ru.zelginni.tinycerberusbot.chat.ChatService
import kotlin.collections.HashMap


@Service
class StickerService(
        private val chatService: ChatService
) {

    @Value("\${bot.maxstickersinarow:1}")
    private val maxStickersInARow: Int = 1

    private val stickersInARow = HashMap<Long, List<User>>()

    fun purgeSticker(update: Update): CommandResult {
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
                ?: return CommandResult(
                        CommandStatus.Error,
                        "Аид запретил мне кусаться в этом чате."
                )
        stickersInARow.put(update.message.chatId, listOf(update.message.from))
        val stickersInARowCount = stickersInARow.get(update.message.chatId)
        if (stickersInARowCount != null && stickersInARowCount.size > maxStickersInARow) {
                stickersInARow.remove(update.message.chatId)
                return CommandResult(CommandStatus.Success, "", ResultAction.Delete)
            }
        return CommandResult(CommandStatus.Success, "", ResultAction.Nothing)
    }
}
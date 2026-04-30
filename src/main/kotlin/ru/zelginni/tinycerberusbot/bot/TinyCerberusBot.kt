package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberProfile
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMembers
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.TelegramUpdateHandler
import java.time.Instant

@Component
class TinyCerberusBot(
    private val telegramUpdateHandler: TelegramUpdateHandler
) {

    fun consume(update: Update?) {
        if (update == null) {
            return
        }
        if (!update.hasMessage()) {
            return
        }
        val message = update.message
        if (message.newChatMembers.isNotEmpty()) {
            telegramUpdateHandler.handleNewChatMembers(message.toIncomingChatMembers())
        }
        telegramUpdateHandler.handleMessage(message.toIncomingChatMessage())
    }

    private fun Message.toIncomingChatMessage(): IncomingChatMessage =
        IncomingChatMessage(
            chatId = chatId,
            userId = from.id,
            messageId = messageId,
            text = text,
            sentAt = Instant.ofEpochSecond(date.toLong()),
            senderDisplayName = from.writableName(),
            senderUsername = from.userName,
            senderIsBot = from.isBot,
            replyTo = replyToMessage?.toIncomingChatMessage(),
            hasText = hasText(),
            hasPhoto = hasPhoto(),
            hasDocument = hasDocument()
        )

    private fun User.writableName(): String =
        listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { userName ?: id.toString() }

    private fun Message.toIncomingChatMembers(): IncomingChatMembers =
        IncomingChatMembers(
            chatId = chatId,
            messageId = messageId,
            members = newChatMembers.map { member ->
                IncomingChatMemberProfile(
                    userId = member.id,
                    displayName = member.writableName(),
                    username = member.userName,
                    isBot = member.isBot,
                )
            }
        )
}

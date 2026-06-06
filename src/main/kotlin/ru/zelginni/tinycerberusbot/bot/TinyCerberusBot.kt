package ru.zelginni.tinycerberusbot.bot

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.zelginni.tinycerberusbot.telegram.*
import java.time.Instant

@Component
class TinyCerberusBot(
    private val administrationService: BotAdministrationService,
    private val telegramUpdateHandler: TelegramUpdateHandler
) {

    fun consume(update: Update?) {
        try {
            tryConsume(update)
        } catch (e: Exception) {
            logger.error("Something went wrong while consuming update", e)
        }
    }

    private fun tryConsume(update: Update?) {
        if (administrationService.currentState() != BotState.ENABLED) {
            return
        }
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
            messageThreadId = messageThreadId,
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
            messageThreadId = messageThreadId,
            members = newChatMembers.map { member ->
                IncomingChatMemberProfile(
                    userId = member.id,
                    displayName = member.writableName(),
                    username = member.userName,
                    isBot = member.isBot,
                )
            }
        )

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultTelegramUpdateHandler::class.java)
    }
}

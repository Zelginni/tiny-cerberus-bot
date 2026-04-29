package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.zelginni.tinycerberusbot.bayan.Bayan
import ru.zelginni.tinycerberusbot.bayan.BayanService
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.TelegramUpdateHandler
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import java.time.Instant

@Component
class TinyCerberusBot(
    private val telegramCommandSender: TelegramCommandSender,
    private val telegramUpdateHandler: TelegramUpdateHandler,
    private val bayanService: BayanService,
    private val chatService: ChatService,
    private val rulesService: RulesService
) {

    fun consume(update: Update?) {
        if (update == null) {
            return
        }
        processWelcomeMessage(update)
        if (!update.hasMessage()
            || !update.message.hasText()) {
            return
        }
        processBayan(update)
        processCommand(update)
    }

    private fun processBayan(update: Update) {
        if (!update.message.text.lowercase().contains("баян")
            || update.message.from.isBot
        ) {
            return
        }
        respondToBayan(update)
    }

    private fun respondToBayan(update: Update) {
        val bayan: Bayan? = bayanService.respondToBayan(update)
        if (bayan != null) {
            bayan.response?.let { sendSimpleReplyText(update, it) }
        }
    }

    private fun processCommand(update: Update) {
        telegramUpdateHandler.handleMessage(update.message.toIncomingChatMessage())
    }

    private fun sendSimpleReplyText(update: Update, messageText: String) {
        telegramCommandSender.sendReplyMessage(update.message.chatId, update.message.messageId, messageText)
    }

    private fun processWelcomeMessage(update: Update) {
        if (!update.hasMessage()) {
            return
        }
        val newChatMembers = update.message.newChatMembers
        if (newChatMembers.isEmpty()) {
            return
        }
        if (!doesNewMembersNeedGreeting(newChatMembers)) {
            return
        }
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat != null) {
            if (chat.rulesEnabled == false) {
                return
            }
        }
        val ruleSet = chat?.let { rulesService.getRules(it)?.ruleset }
        val rules = if (ruleSet == null)
                    "Правил у нас пока нет, располагайся."
                else
                    "Ознакомься с правилами чата:\n$ruleSet"
        val names = newChatMembers.joinToString(" ") { "@${it.userName}" }
        sendSimpleReplyText(
                update,
                "Привет, $names!\n\n$rules"
        )
    }

    private fun doesNewMembersNeedGreeting(newChatMembers: List<User>): Boolean {
        for (chatMember in newChatMembers) {
            if (chatMember.isBot) {
                return false
            }
        }
        return true
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
            hasDocument = hasDocument(),
            hasOnlyText = hasOnlyText(),
        )

    private fun Message.hasOnlyText(): Boolean =
        hasText()
            && !(hasDocument()
            || hasPhoto()
            || hasDice()
            || hasPoll()
            || hasAnimation()
            || hasAudio()
            || hasContact()
            || hasInvoice()
            || hasLocation()
            || hasPassportData()
            || hasReplyMarkup()
            || hasSticker()
            || hasSuccessfulPayment()
            || hasViaBot()
            || hasVideo()
            || hasVideoNote()
            || hasVoice())

    private fun User.writableName(): String =
        userName ?: "$firstName ${lastName ?: ""}".trim()
}

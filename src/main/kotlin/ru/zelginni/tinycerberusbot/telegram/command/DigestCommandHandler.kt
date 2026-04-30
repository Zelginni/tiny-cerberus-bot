package ru.zelginni.tinycerberusbot.telegram.command

import org.springframework.stereotype.Component
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.digest.DigestService
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramChatMemberService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender

@Component
class DigestCommandHandler(
    telegramCommandSender: TelegramCommandSender,
    chatMemberService: TelegramChatMemberService,
    private val chatService: ChatService,
    private val digestService: DigestService,
) : AbstractCommandHandler(telegramCommandSender, chatMemberService) {

    override val commandName = "digest"

    override val requiresAdmin = false

    override fun handleCommand(command: ChatCommand) {
        val chat = chatService.getEnabledChatByTelegramId(command.chatId.toString())
        if (chat == null || chat.digestEnabled == false) {
            reply(command, "Аид запретил мне собирать дайджест в этом чате.")
            return
        }

        val repliedMessage = command.replyTo
            ?: return reply(command, "Эта команда должна быть использована ответом на сообщение. Если оно есть, попробуйте сообщение посвежее.")

        if (repliedMessage.senderIsBot && repliedMessage.text?.contains("Дайджест") == true) {
            reply(command, "Наркоман штоле?")
            return
        }

        val linkToMessage = "https://t.me/c/${getChatIdForLink(chat.telegramId)}/${repliedMessage.messageId}"
        val repliedMessageDate = repliedMessage.sentAt.epochSecond.toInt()
        digestService.addDigest(chat, linkToMessage, getDescription(command, repliedMessage), repliedMessageDate)
            ?: return reply(command, "Это сообщение уже добавили в дайджест. Вы опоздали :(")

        reply(command, "Добавлено.")
    }

    private fun getDescription(command: ChatCommand, repliedMessage: IncomingChatMessage): String =
        command.arguments ?: createDescription(repliedMessage)

    private fun createDescription(repliedMessage: IncomingChatMessage): String {
        val author = repliedMessage.senderDisplayName.orEmpty()
        return when {
            repliedMessage.hasPhoto && !repliedMessage.hasText -> "Фото от $author"
            repliedMessage.hasDocument && !repliedMessage.hasText -> "Файл от $author"
            else -> repliedMessage.text.orEmpty().let { text ->
                text.substring(0, if (text.length < 101) text.length else 101)
            }
        }
    }

    private fun getChatIdForLink(telegramId: String?): String? =
        telegramId?.substring(4)
}

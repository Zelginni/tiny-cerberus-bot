package ru.zelginni.tinycerberusbot.bot

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.Serializable
import javax.annotation.PostConstruct

@Component
class TinyCerberusBot(
    private val commandService: CommandService
): TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(TinyCerberusBot::class.java)

    @Value("\${bot.name}")
    private val botUsername: String = ""

    @Value("\${bot.token}")
    private val botToken: String = ""

    @PostConstruct
    private fun init() {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(this)
        logger.info("Bot registered")
    }

    override fun getBotToken(): String = botToken

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update?) {
        if (update == null) {
            return
        }
        if (!update.hasMessage()
            || !update.message.hasText()
            || !update.message.isCommand
            || !update.message.text.contains(botUsername)
        ) {
            return
        }
        val command = getCommand(update.message.text)
        if (command == null) {
            sendSimpleText(update, "Я не понимаю :(")
            return
        }
        val commandResult =  try {
            command.performCommand(commandService, update)
        } catch (e: Exception) {
            logger.error("Command not performed: $command", e)
            CommandResult(CommandStatus.Error, "Не получилось :(", ResultAction.Print)
        }
        logger.info("Command $command, result $commandResult")
        if (commandResult.message != null) {
            sendSimpleText(update, commandResult.message)
        }
        when(commandResult.resultAction) {
            ResultAction.Ban -> banMember(update)
            ResultAction.Print -> {}
        }
    }

    private fun sendSimpleText(update: Update, messageText: String) {
        val message = SendMessage().apply {
            setChatId(update.message.chatId)
            text = messageText
            replyToMessageId = update.message.messageId
            enableHtml(true)
        }
        perform(message)
    }

    private fun banMember(update: Update) {
        val ban = BanChatMember().apply {
            setChatId(update.message.chatId)
            userId = update.message.replyToMessage.from.id
        }
        perform(ban)
    }

    private fun <T: Serializable> perform(action: BotApiMethod<T>) {
        try {
            execute(action)
        } catch (e: TelegramApiException) {
            logger.error("Problem to perform $action", e)
        }
    }
}
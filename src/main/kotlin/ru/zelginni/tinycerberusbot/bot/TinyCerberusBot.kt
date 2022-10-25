package ru.zelginni.tinycerberusbot.bot

import org.apache.commons.collections4.map.PassiveExpiringMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.chat.ChatViewDto
import ru.zelginni.tinycerberusbot.holiday.HolidayService
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.collections.HashMap

@Component
class TinyCerberusBot(
    private val commandService: CommandService,
    private val chatService: ChatService,
    private val holidayService: HolidayService
): TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(TinyCerberusBot::class.java)

    @Value("\${bot.name}")
    private val botUsername: String = ""

    @Value("\${bot.token}")
    private val botToken: String = ""

    @Value("\${bot.adminlistcache:600}")
    private val adminListCacheSeconds: Long = 600

    private val recentlyRequestedAdmins =
        Collections.synchronizedMap(PassiveExpiringMap(
            PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy(
                adminListCacheSeconds, TimeUnit.SECONDS),
            HashMap<Long, List<ChatMember>>()))

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
            || !isAdmin(update.message.from, update.message.chat)
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

    @Scheduled(cron = "0 0 10 * * *")
    fun dailyHoliday() {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val allChats: List<ChatViewDto> = chatService.getAllChats()
        val holiday = holidayService.getHoliday()
        for (chat in allChats) {
            val message = SendMessage().apply {
                chat.telegramId?.let { chatId = it }
                text = "Сегодня " + LocalDateTime.now().format(format) + "\n" +
                        "Отмечаем " + holiday.name + "\n" +
                        "Нет повода не выпить!"
            }
            perform(message)
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

    private fun isAdmin(user: User, chat: Chat): Boolean {
        return getAdminList(chat).any { it.user.id == user.id }
    }

    private fun getAdminList(chat: Chat): List<ChatMember> {
        return recentlyRequestedAdmins.computeIfAbsent(chat.id) {
            requestAdminList(it)
        }
    }

    private fun requestAdminList(chatId: Long): List<ChatMember> {
        return perform(GetChatAdministrators(chatId.toString())) ?: listOf()
    }

    private fun <T: Serializable> perform(action: BotApiMethod<T>): T? {
        return try {
            execute(action)
        } catch (e: TelegramApiException) {
            logger.error("Problem to perform $action", e)
            null
        }
    }
}
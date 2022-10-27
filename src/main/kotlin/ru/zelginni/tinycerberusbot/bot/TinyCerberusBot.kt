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
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.chat.ChatViewDto
import ru.zelginni.tinycerberusbot.digest.Digest
import ru.zelginni.tinycerberusbot.digest.DigestService
import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.collections.HashMap

@Component
class TinyCerberusBot(
    private val commandService: CommandService,
    private val chatService: ChatService,
    private val digestService: DigestService
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
        if (update.hasMessage()
                && update.message.hasText()
                && update.message.text.contains("Дайджест")
                && isBot(update.message.from)) {
            PinChatMessage(update.message.chatId.toString(), update.message.messageId, true)
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

    @Scheduled(cron = "0 0 20 * * *")
    fun dailyDigest() {
        val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val allchats: List<ChatViewDto> = chatService.getAllChats()
        for (chat in allchats) {
            val digest: List<Digest>? = digestService.fetchDigest(chat)
            if (digest.isNullOrEmpty()) {
                return
            } else {
                val digestList = StringBuilder()
                for (digestEntry in digest) {
                    digestList.append("\n")
                    digestList.append(digestEntry.linkToMessage)
                    digestList.append(" ")
                    digestList.append(digestEntry.createdOn?.format(format))
                    digestList.append("\n")
                    digestList.append(digestEntry.description)
                    digestList.append("\n")
                }
                val message = SendMessage().apply {
                    chat.telegramId
                    text = "Дайджест за сутки:" + "\n" +
                            digestList
                }
                perform(message)
            }
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

    private fun isBot(user: User): Boolean {
        return user.isBot
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
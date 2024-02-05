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
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.zelginni.tinycerberusbot.bayan.Bayan
import ru.zelginni.tinycerberusbot.bayan.BayanService
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.digest.DigestService
import ru.zelginni.tinycerberusbot.chat.ChatViewDto
import ru.zelginni.tinycerberusbot.rules.RulesService
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.collections.HashMap

@Component
class TinyCerberusBot(
    private val commandService: CommandService,
    private val bayanService: BayanService,
    private val chatService: ChatService,
    private val digestService: DigestService,
    private val rulesService: RulesService
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
            || !update.message.hasText()) {
            return
        }
        processBayan(update)
        processCommand(update)
        processWelcomeMessage(update)
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
        if ((!update.message.isCommand || !update.message.text.contains(botUsername))
            && !update.message.text.startsWith(ALT_COMMAND_START)
        ) {
            return
        }
        val command = getCommand(update.message.text)
        if (command == null) {
            sendSimpleReplyText(update, "Я не понимаю :(")
            return
        }
        if (command.requireAdmin && !isAdmin(update.message.from, update.message.chat)) {
            return
        }

        if (command == BotCommand.Warn
            && update.message.replyToMessage != null
            && isAdmin(update.message.replyToMessage.from, update.message.chat)) {
            sendSimpleReplyText(update, "Админов я кусать не буду.")
            return
        }

        val commandResult = try {
            command.performCommand(commandService, update)
        } catch (e: Exception) {
            logger.error("Command not performed: $command", e)
            CommandResult(CommandStatus.Error, "Не получилось :(", ResultAction.Print)
        }
        logger.info("Command $command, result $commandResult")
        if (commandResult.message != null) {
            sendSimpleReplyText(update, commandResult.message)
        }
        when(commandResult.resultAction) {
            ResultAction.Ban -> banMember(update)
            ResultAction.Print -> {}
        }
    }

    @Scheduled(cron = "\${bot.digest.cron}")
    fun dailyDigest() {
        val allChats: List<ChatViewDto> = chatService.getAllChats()
        allChats.filter {
                chat -> chat.enabled == true
                && chat.digestEnabled == true
                && chat.id != null
                && chat.telegramId != null
        }.forEach { chat -> sendDigest(chat)}
        cleanDigestPins()
    }

    private fun sendDigest(chat: ChatViewDto) {
        val chatId = chat.telegramId ?: return
        val digest = digestService.compileDigest(chat.id ?: -1) ?: return
        if (digest.isBlank()) {
            sendSimpleText(chatId.toLong(), "За прошедшие сутки в дайджест ничего не добавили.")
            return
        }
        val message = perform(SendMessage().apply {
            this.chatId = chatId
            text = digest
            disableNotification = true
        }) ?: return
        perform(PinChatMessage(message.chatId.toString(), message.messageId, true))
        digestService.addPinnedDigest(chatId, message.messageId)
        digestService.deleteDigest(chatId)
    }

    private fun cleanDigestPins() {
        digestService.fetchOutdatedDigests()?.filter { it.chat?.telegramId != null }?.forEach {
            perform(UnpinChatMessage(it.chat?.telegramId ?: "", it.pinnedMessageId))
        }
        digestService.deletePinnedDigests()
    }

    private fun sendSimpleText(chatId: Long, messageText: String) {
        val message = SendMessage().apply {
            setChatId(chatId)
            text = messageText
        }
        perform(message)
    }

    private fun sendSimpleReplyText(update: Update, messageText: String) {
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

    private fun processWelcomeMessage(update: Update) {
        val newChatMembers = update.message.newChatMembers
        if (newChatMembers.isEmpty()) {
            return
        }
        if (checkNewChatMembersForBots(newChatMembers)) {
            return
        }
        val chat = chatService.getEnabledChatByTelegramId(update.message.chatId.toString())
        if (chat != null) {
            if (chat.rulesEnabled == false) {
                return
            }
        }
        val names = newChatMembers.joinToString(" ") { "@${it.userName}" }
        sendSimpleReplyText(
                update,
                "Привет, $names!\n\nОзнакомься с правилами чата:\n${chat?.let { rulesService.getRules(it) }}"
        )
    }

    private fun checkNewChatMembersForBots(newChatMembers: List<User>): Boolean {
        for (chatMember in newChatMembers) {
            if (chatMember.isBot) {
                return true
            }
        }
        return false
    }
}
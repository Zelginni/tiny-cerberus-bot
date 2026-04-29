package ru.zelginni.tinycerberusbot.bot

import org.apache.commons.collections4.map.PassiveExpiringMap
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import ru.zelginni.tinycerberusbot.bayan.Bayan
import ru.zelginni.tinycerberusbot.bayan.BayanService
import ru.zelginni.tinycerberusbot.chat.ChatService
import ru.zelginni.tinycerberusbot.digest.DigestService
import ru.zelginni.tinycerberusbot.chat.ChatViewDto
import ru.zelginni.tinycerberusbot.rules.RulesService
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

@Component
class TinyCerberusBot(
    private val telegramCommandSender: TelegramCommandSender,
    private val botProperties: TelegramBotProperties,
    private val commandService: CommandService,
    private val bayanService: BayanService,
    private val chatService: ChatService,
    private val digestService: DigestService,
    private val rulesService: RulesService
): SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private val logger = LoggerFactory.getLogger(TinyCerberusBot::class.java)
    private val adminListCacheSeconds: Long = 600

    private val recentlyRequestedAdmins =
        Collections.synchronizedMap(PassiveExpiringMap(
            PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy(
                adminListCacheSeconds, TimeUnit.SECONDS),
            HashMap<Long, List<ChatMember>>()))

    override fun getBotToken(): String = botProperties.token

    override fun getUpdatesConsumer() = this

    override fun consume(update: Update?) {
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
        if ((!update.message.isCommand || !update.message.text.contains(botProperties.name))
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
            telegramCommandSender.sendMessage(chatId.toLong(), "За прошедшие сутки в дайджест ничего не добавили.")
            return
        }
        val message = telegramCommandSender.sendSilentMessage(chatId, digest) ?: return
        val messageId = message.messageId ?: return
        telegramCommandSender.pinMessage(message.chatId.toString(), messageId, true)
        digestService.addPinnedDigest(chatId, messageId)
        digestService.deleteDigest(chatId)
    }

    private fun cleanDigestPins() {
        digestService.fetchOutdatedDigests()
            ?.filter { it.chat?.telegramId != null && it.pinnedMessageId != null }
            ?.forEach {
                telegramCommandSender.unpinMessage(it.chat?.telegramId ?: "", it.pinnedMessageId ?: return@forEach)
        }
        digestService.deletePinnedDigests()
    }

    private fun sendSimpleReplyText(update: Update, messageText: String) {
        telegramCommandSender.sendReplyMessage(update.message.chatId, update.message.messageId, messageText)
    }

    private fun banMember(update: Update) {
        telegramCommandSender.banChatMember(update.message.chatId, update.message.replyToMessage.from.id)
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
        return telegramCommandSender.getChatAdministrators(chatId)
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
}

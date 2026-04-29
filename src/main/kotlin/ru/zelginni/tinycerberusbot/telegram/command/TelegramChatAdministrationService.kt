package ru.zelginni.tinycerberusbot.telegram.command

import org.apache.commons.collections4.map.PassiveExpiringMap
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import ru.zelginni.tinycerberusbot.telegram.gateway.TelegramCommandSender
import java.util.Collections
import java.util.concurrent.TimeUnit

@Service
class TelegramChatAdministrationService(
    private val telegramCommandSender: TelegramCommandSender,
) {
    private val recentlyRequestedAdmins =
        Collections.synchronizedMap(
            PassiveExpiringMap(
                PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy(ADMIN_LIST_CACHE_SECONDS, TimeUnit.SECONDS),
                HashMap<Long, List<ChatMember>>()
            )
        )

    fun isAdmin(chatId: Long, userId: Long): Boolean =
        getAdminList(chatId).any { it.user.id == userId }

    private fun getAdminList(chatId: Long): List<ChatMember> =
        recentlyRequestedAdmins.computeIfAbsent(chatId) {
            telegramCommandSender.getChatAdministrators(it)
        }

    private companion object {
        private const val ADMIN_LIST_CACHE_SECONDS: Long = 600
    }
}

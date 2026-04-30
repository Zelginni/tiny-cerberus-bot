package ru.zelginni.tinycerberusbot.telegram.gateway

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class TelegramApiChatMemberService(
    private val telegramClient: TelegramClient,
    private val registry: TelegramChatMemberRegistry,
) : TelegramChatMemberService {
    override fun isChatAdministrator(chatId: Long, userId: Long): Boolean {
        val member = telegramClient.execute(GetChatMember(chatId.toString(), userId))
        return member is ChatMemberOwner || member is ChatMemberAdministrator
    }

    override fun getChatMembers(chatId: Long): List<TelegramChatMember> =
        registry.getKnownMembers(chatId)
}

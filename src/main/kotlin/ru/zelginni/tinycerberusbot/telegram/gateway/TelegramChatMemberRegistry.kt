package ru.zelginni.tinycerberusbot.telegram.gateway

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberLeft
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMembers
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import java.time.Clock
import java.time.Instant

@Service
class TelegramChatMemberRegistry(
    private val repository: TelegramKnownChatMemberRepository,
    private val clock: Clock,
) {
    @Transactional
    fun rememberSender(message: IncomingChatMessage) {
        val displayName = message.senderDisplayName ?: return
        rememberMember(
            chatId = message.chatId,
            userId = message.userId,
            displayName = displayName,
            username = message.senderUsername,
        )
    }

    @Transactional
    fun rememberMembers(members: IncomingChatMembers) {
        members.members.forEach {
            rememberMember(chatId = members.chatId, userId = it.userId, displayName = it.displayName, username = it.username)
        }
    }

    @Transactional
    fun forgetMember(member: IncomingChatMemberLeft) {
        val id = TelegramKnownChatMemberId(chatId = member.chatId, userId = member.userId)
        if (repository.existsById(id)) {
            logger.debug("Forgetting known chat member chatId={} userId={}", member.chatId, member.userId)
            repository.deleteById(id)
        }
    }

    @Transactional
    fun forgetMembers(chatId: Long, userIds: Collection<Long>) {
        userIds.distinct().forEach { userId ->
            forgetMember(IncomingChatMemberLeft(chatId = chatId, userId = userId))
        }
    }

    private fun rememberMember(chatId: Long, userId: Long, displayName: String, username: String?) {
        val id = TelegramKnownChatMemberId(chatId = chatId, userId = userId)
        val existingMember = repository.findById(id)
        val knownMember = existingMember.orElseGet {
            logger.debug("Adding known chat member chatId={} userId={} displayName={} username={}", chatId, userId, displayName, username)
            TelegramKnownChatMember(
                id = id,
                displayName = displayName,
                lastSeenAt = Instant.now(clock),
                username = username,
            )
        }

        knownMember.displayName = displayName
        knownMember.username = username
        knownMember.lastSeenAt = Instant.now(clock)
        repository.save(knownMember)
    }

    @Transactional(readOnly = true)
    fun getKnownMembers(chatId: Long): List<TelegramChatMember> =
        repository.findKnownMembers(chatId)
            .map { TelegramChatMember(userId = it.id.userId, displayName = it.displayName, username = it.username) }

    private companion object {
        private val logger = LoggerFactory.getLogger(TelegramChatMemberRegistry::class.java)
    }
}

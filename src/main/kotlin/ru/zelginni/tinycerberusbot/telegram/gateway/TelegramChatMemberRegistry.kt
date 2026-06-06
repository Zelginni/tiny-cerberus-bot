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
            displayName = displayName
        )
    }

    @Transactional
    fun rememberMembers(members: IncomingChatMembers) {
        members.members.forEach { rememberMember(chatId = members.chatId, it.userId, it.displayName) }
    }

    @Transactional
    fun forgetMember(member: IncomingChatMemberLeft) {
        val id = TelegramKnownChatMemberId(chatId = member.chatId, userId = member.userId)
        if (repository.existsById(id)) {
            logger.debug("Forgetting known chat member chatId={} userId={}", member.chatId, member.userId)
            repository.deleteById(id)
        }
    }

    private fun rememberMember(chatId: Long, userId: Long, displayName: String) {
        val id = TelegramKnownChatMemberId(chatId = chatId, userId = userId)
        val existingMember = repository.findById(id)
        val knownMember = existingMember.orElseGet {
            logger.debug("Adding known chat member chatId={} userId={} displayName={}", chatId, userId, displayName)
            TelegramKnownChatMember(
                id = id,
                displayName = displayName,
                lastSeenAt = Instant.now(clock),
            )
        }

        knownMember.displayName = displayName
        knownMember.lastSeenAt = Instant.now(clock)
        repository.save(knownMember)
    }

    @Transactional(readOnly = true)
    fun getKnownMembers(chatId: Long): List<TelegramChatMember> =
        repository.findKnownMembers(chatId)
            .map { TelegramChatMember(userId = it.id.userId, displayName = it.displayName) }

    private companion object {
        private val logger = LoggerFactory.getLogger(TelegramChatMemberRegistry::class.java)
    }
}

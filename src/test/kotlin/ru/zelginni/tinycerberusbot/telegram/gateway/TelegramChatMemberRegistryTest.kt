package ru.zelginni.tinycerberusbot.telegram.gateway

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMessage
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberLeft
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberProfile
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMembers
import java.time.Instant

@SpringBootTest
class TelegramChatMemberRegistryTest {

    @Autowired
    private lateinit var registry: TelegramChatMemberRegistry

    @Autowired
    private lateinit var repository: TelegramKnownChatMemberRepository

    @Test
    fun forgetMemberWhenMemberLeftChat() {
        val chatId = -987654321L
        val userId = 123456789L

        registry.rememberMembers(
            IncomingChatMembers(
                chatId = chatId,
                messageId = 1,
                members = listOf(
                    IncomingChatMemberProfile(
                        userId = userId,
                        displayName = "Known Member",
                        username = "known_member",
                        isBot = false,
                    )
                ),
            )
        )

        registry.forgetMember(IncomingChatMemberLeft(chatId = chatId, userId = userId))

        assertTrue(repository.findById(TelegramKnownChatMemberId(chatId = chatId, userId = userId)).isEmpty)
    }

    @Test
    fun rememberNewMembersWithUsername() {
        val chatId = -987654322L
        val userId = 123456780L

        registry.rememberMembers(
            IncomingChatMembers(
                chatId = chatId,
                messageId = 1,
                members = listOf(
                    IncomingChatMemberProfile(
                        userId = userId,
                        displayName = "Known Member",
                        username = "known_member",
                        isBot = false,
                    )
                ),
            )
        )

        val member = repository.findById(TelegramKnownChatMemberId(chatId = chatId, userId = userId)).orElseThrow()
        assertEquals("known_member", member.username)
    }

    @Test
    fun updateKnownMemberUsernameFromMessage() {
        val chatId = -987654323L
        val userId = 123456781L
        repository.saveAndFlush(
            TelegramKnownChatMember(
                id = TelegramKnownChatMemberId(chatId = chatId, userId = userId),
                displayName = "Old Name",
                lastSeenAt = Instant.EPOCH,
                username = "old_tag",
            )
        )

        registry.rememberSender(
            IncomingChatMessage(
                chatId = chatId,
                userId = userId,
                messageId = 1,
                text = "hello",
                sentAt = Instant.EPOCH,
                senderDisplayName = "New Name",
                senderUsername = "new_tag",
            )
        )

        val member = repository.findById(TelegramKnownChatMemberId(chatId = chatId, userId = userId)).orElseThrow()
        assertEquals("New Name", member.displayName)
        assertEquals("new_tag", member.username)
    }
}

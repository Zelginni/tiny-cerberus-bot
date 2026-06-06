package ru.zelginni.tinycerberusbot.telegram.gateway

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberLeft
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMemberProfile
import ru.zelginni.tinycerberusbot.telegram.IncomingChatMembers

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
}

package ru.zelginni.tinycerberusbot.user

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.Chat
import ru.zelginni.tinycerberusbot.warn.Warn
import ru.zelginni.tinycerberusbot.warn.WarnRepository
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val warnRepository: WarnRepository
) {

    fun createOrGetUser(telegramId: String, userName: String, chat: Chat): User {
        val user = userRepository.findByTelegramIdAndChatId(telegramId, chat.id
            ?: throw IllegalStateException("Chat has no id"))
        if (user == null) {
            val newUser = User(telegramId = telegramId, username = userName, chat = chat)
            userRepository.saveAndFlush(newUser)
            return newUser
        } else if (user.username != userName) {
            user.username = userName
            userRepository.saveAndFlush(user)
        }
        return user
    }

    fun makeNewWarnAndReturnWarnCount(user: User, authorTelegramId: String, authorUsername: String): Int {
        val warn = Warn(
            dateCreated = Timestamp.valueOf(LocalDateTime.now()),
            authorTelegramId = authorTelegramId,
            authorUsername = authorUsername,
            user = user
        )
        warnRepository.saveAndFlush(warn)
        return warnRepository.findAllByUserId(user.id ?: throw IllegalStateException("User without id")).size
    }

    fun deleteOneWarnAndReturnWarnCount(user: User): Int {
        val warns = warnRepository.findAllByUserId(user.id ?: throw IllegalStateException("User without id"))
        val latest = warns.maxByOrNull { w -> w.dateCreated?:Timestamp.valueOf(LocalDateTime.now()) } ?: return -1
        warnRepository.delete(latest)
        return warns.size - 1
    }

    fun getWarnsByUser(user: User): List<Warn> {
        return warnRepository.findAllByUserId(user.id ?: throw IllegalStateException("User without id"))
    }

    fun getWarnsByChat(chat: Chat): List<Warn> {
        return warnRepository.findAllByChatId(chat.id ?: throw IllegalStateException("Chat without id"))
    }
}
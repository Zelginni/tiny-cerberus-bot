package ru.zelginni.tinycerberusbot.rules

import org.springframework.stereotype.Service
import ru.zelginni.tinycerberusbot.chat.Chat

@Service
class RulesService(
        private val rulesRepository: RulesRepository) {

    fun addRules(chat: Chat, ruleset: String) {
        val rules = rulesRepository.findByChatId(
                chat.id ?: throw IllegalStateException("How does chat not have its id?!"))
                ?: Rules(null, ruleset, chat)
        rules.ruleset = ruleset
        rulesRepository.saveAndFlush(rules)
    }

    fun removeRules(chat: Chat) {
        rulesRepository.deleteByChatId(
                chat.id ?: throw IllegalStateException("How does chat not have its id?!"))
    }

    fun getRules(chat: Chat): Rules? {
        return rulesRepository.findByChatId(
                chat.id ?: throw IllegalStateException("How does chat not have its id?!"))
    }
}
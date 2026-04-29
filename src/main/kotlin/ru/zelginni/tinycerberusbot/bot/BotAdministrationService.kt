package ru.zelginni.tinycerberusbot.bot

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BotAdministrationService(
    private val repository: BotSettingsRepository,
) {
    @Transactional(readOnly = true)
    fun currentState(): BotState =
        repository.findById(BotSettings.DEFAULT_ID)
            .map { it.state }
            .orElse(BotState.DISABLED)

    @Transactional
    fun setState(state: BotState): BotState {
        val settings = repository.findById(BotSettings.DEFAULT_ID)
            .orElseGet { BotSettings() }

        settings.state = state
        settings.updatedAt = Instant.now()

        return repository.save(settings).state
    }
}

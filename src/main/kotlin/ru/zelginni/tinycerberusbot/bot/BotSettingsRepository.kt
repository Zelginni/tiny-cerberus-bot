package ru.zelginni.tinycerberusbot.bot

import org.springframework.data.jpa.repository.JpaRepository
import ru.zelginni.tinycerberusbot.bot.BotSettings

interface BotSettingsRepository : JpaRepository<BotSettings, String>

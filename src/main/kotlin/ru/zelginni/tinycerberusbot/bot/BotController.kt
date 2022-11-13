package ru.zelginni.tinycerberusbot.bot

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/bot")
class BotController(
    private val tinyCerberusBot: TinyCerberusBot
) {
    private val logger = LoggerFactory.getLogger(BotController::class.java)

    @GetMapping("/daily-digest")
    @Operation(
        summary = "Launch digest",
        description = "Launch daily digest. Suggested for testing purposes only",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun dailyDigest(): ResponseEntity<String> {
        logger.info("Launch digest requested")
        tinyCerberusBot.dailyDigest()
        return ResponseEntity.ok("Daily digest performed")
    }
}
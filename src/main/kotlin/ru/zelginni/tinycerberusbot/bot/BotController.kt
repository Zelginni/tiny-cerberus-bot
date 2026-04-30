package ru.zelginni.tinycerberusbot.bot

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.zelginni.tinycerberusbot.digest.DigestPublisher

@RestController
@RequestMapping("/admin/bot")
class BotController(
    private val administrationService: BotAdministrationService,
    private val digestPublisher: DigestPublisher,
) {
    private val logger = LoggerFactory.getLogger(BotController::class.java)


    @GetMapping("/state")
    fun getState(): BotStateResponse {
        logger.info("Admin API call get bot state")
        return BotStateResponse(administrationService.currentState())
    }

    @PutMapping("/state")
    fun setState(@RequestBody request: SetBotStateRequest): BotStateResponse {
        logger.info("Admin API call set bot state state={}", request.state)
        return BotStateResponse(administrationService.setState(request.state))
    }

    @GetMapping("/daily-digest")
    @Operation(
        summary = "Launch digest",
        description = "Launch daily digest. Suggested for testing purposes only",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun dailyDigest(): ResponseEntity<String> {
        logger.info("Launch digest requested")
        digestPublisher.publishDailyDigest()
        return ResponseEntity.ok("Daily digest performed")
    }
}

data class SetBotStateRequest(
    var state: BotState,
)

data class BotStateResponse(
    var state: BotState,
)
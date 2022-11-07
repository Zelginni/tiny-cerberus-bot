package ru.zelginni.tinycerberusbot.chat

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/chat")
class ChatController(
    private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping
    @Operation(
        summary = "New chat",
        description = "Add new chat to available chats",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun createChat(@RequestBody newChat: ChatInsertDto): ResponseEntity<String> {
        logger.info("New Chat: $newChat")
        chatService.createNewChat(newChat)
        return ResponseEntity.ok("Chat created")
    }

    @GetMapping("/all")
    @Operation(
        summary = "List of all chats",
        description = "View all chats stored in configs",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun allChats(): ResponseEntity<AllChatResponse> {
        logger.info("All chats requested")
        return ResponseEntity.ok(AllChatResponse(chatService.getAllChats()))
    }

    @PutMapping("/disable")
    @Operation(
        summary = "Disable chat",
        description = "Disable chat for bot",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun disableChat(@RequestParam telegramId: String): ResponseEntity<String> {
        logger.info("Disable chat $telegramId")
        chatService.disableChat(telegramId)
        chatService.cleanCache()
        return ResponseEntity.ok("Chat $telegramId disabled")
    }

    @PutMapping("/enable")
    @Operation(
        summary = "Enable chat",
        description = "Enable chat for bot",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun enableChat(@RequestParam telegramId: String): ResponseEntity<String> {
        logger.info("Enable chat $telegramId")
        chatService.enableChat(telegramId)
        chatService.cleanCache()
        return ResponseEntity.ok("Chat $telegramId enabled")
    }

    @PutMapping("/enable/{feature}")
    @Operation(
        summary = "Enable feature",
        description = "Enable feature for chat",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun enableFeature(@RequestParam telegramId: String, @PathVariable feature: String): ResponseEntity<String> {
        logger.info("Enable feature $feature in chat $telegramId")
        val chatFeature = getFeature(feature) ?: return ResponseEntity.badRequest().body("No such feature as $feature")
        chatService.enableFeatureInChat(telegramId, chatFeature)
        chatService.cleanCache()
        return ResponseEntity.ok("$feature in chat $telegramId enabled")
    }

    @PutMapping("/disable/{feature}")
    @Operation(
        summary = "Disable feature",
        description = "Disable feature for chat",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun disableFeature(@RequestParam telegramId: String, @PathVariable feature: String): ResponseEntity<String> {
        logger.info("Disable feature $feature in chat $telegramId")
        val chatFeature = getFeature(feature) ?: return ResponseEntity.badRequest().body("No such feature as $feature")
        chatService.disableFeatureInChat(telegramId, chatFeature)
        chatService.cleanCache()
        return ResponseEntity.ok("$feature in chat $telegramId enabled")
    }

    @GetMapping("/cache/clean")
    @Operation(
        summary = "Clean chat cache",
        description = "Clean chat cache",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun cleanCache(): ResponseEntity<String> {
        logger.info("Clean cache")
        chatService.cleanCache()
        return ResponseEntity.ok("Chat cache cleaned")
    }

    @ExceptionHandler
    fun handleError(e: Exception): Map<String, String?> {
        logger.error("Something went wrong in admin chat controller", e)
        return mapOf("error" to e.message)
    }
}
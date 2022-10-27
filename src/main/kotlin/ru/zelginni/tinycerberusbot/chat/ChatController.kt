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
        return ResponseEntity.ok("Chat $telegramId enabled")
    }

    @PutMapping("/enable/bayan")
    @Operation(
        summary = "Enable bayan",
        description = "Enable bayan feature for chat",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun enableBayan(@RequestParam telegramId: String): ResponseEntity<String> {
        logger.info("Enable bayan in chat $telegramId")
        chatService.enableBayanInChat(telegramId)
        return ResponseEntity.ok("Bayan in chat $telegramId enabled")
    }

    @PutMapping("/disable/bayan")
    @Operation(
        summary = "Disable bayan",
        description = "Disable bayan feature for chat",
        security = [SecurityRequirement(name = "basicAuth")]
    )
    fun disableBayan(@RequestParam telegramId: String): ResponseEntity<String> {
        logger.info("Disable bayan in chat $telegramId")
        chatService.disableBayanInChat(telegramId)
        return ResponseEntity.ok("Bayan in chat $telegramId enabled")
    }

    @GetMapping("/cache/clean")
    @Operation(
        summary = "Enable chat",
        description = "Enable chat for bot",
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
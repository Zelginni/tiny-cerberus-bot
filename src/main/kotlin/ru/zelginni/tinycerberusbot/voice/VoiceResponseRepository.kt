package ru.zelginni.tinycerberusbot.voice

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VoiceResponseRepository: JpaRepository<VoiceResponse, Long> {
    @Query(value = "SELECT * FROM cerberus.voice_response ORDER BY random() LIMIT 1", nativeQuery = true)
    fun fetchVoiceResponse(): VoiceResponse?
}
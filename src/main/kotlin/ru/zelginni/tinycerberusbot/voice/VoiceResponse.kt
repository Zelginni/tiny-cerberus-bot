package ru.zelginni.tinycerberusbot.voice

import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "voice_response")
data class VoiceResponse(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        var id: Long? = null,
        @Column(name = "response")
        var response: String? = null,
)
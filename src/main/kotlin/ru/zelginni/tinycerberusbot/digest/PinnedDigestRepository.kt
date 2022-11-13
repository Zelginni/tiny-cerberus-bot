package ru.zelginni.tinycerberusbot.digest

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PinnedDigestRepository: JpaRepository<PinnedDigest, Long> {
    fun findByCreatedOnBefore(past: LocalDateTime): List<PinnedDigest>?

    fun deleteByCreatedOnBefore(past: LocalDateTime)
}
package ru.zelginni.tinycerberusbot.warn

import org.springframework.data.jpa.repository.JpaRepository

interface WarnRepository: JpaRepository<Warn, Long> {
    fun findAllByUserId(userId: Long): List<Warn>
}
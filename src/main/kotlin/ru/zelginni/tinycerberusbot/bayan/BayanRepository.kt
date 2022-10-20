package ru.zelginni.tinycerberusbot.bayan

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BayanRepository: JpaRepository<Bayan, Long> {
    @Query(value = "SELECT * FROM cerberus.bayan ORDER BY RAND() LIMIT 1", nativeQuery = true)
    fun fetchBayanResponse(): Bayan?
}
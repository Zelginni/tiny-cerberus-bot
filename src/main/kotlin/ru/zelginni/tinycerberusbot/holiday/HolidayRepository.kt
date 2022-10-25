package ru.zelginni.tinycerberusbot.holiday

import org.springframework.data.jpa.repository.JpaRepository

interface HolidayRepository: JpaRepository<Holiday, Long> {
    fun findByMonthAndDay(month: Int, day: Int): Holiday
}
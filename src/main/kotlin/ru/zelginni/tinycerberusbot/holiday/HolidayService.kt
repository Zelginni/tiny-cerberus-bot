package ru.zelginni.tinycerberusbot.holiday

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HolidayService(
        private val holidayRepository: HolidayRepository,
) {
    fun getHoliday(): Holiday {
        val time = LocalDateTime.now()
        return holidayRepository.findByMonthAndDay(time.monthValue, time.dayOfMonth)
    }
}
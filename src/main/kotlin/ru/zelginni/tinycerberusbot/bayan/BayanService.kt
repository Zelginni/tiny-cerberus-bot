package ru.zelginni.tinycerberusbot.bayan

import org.springframework.stereotype.Service

@Service
class BayanService(
        private val bayanRepository: BayanRepository
) {
    fun respondToBayan(): Bayan? {
        return bayanRepository.fetchBayanResponse()
    }
}
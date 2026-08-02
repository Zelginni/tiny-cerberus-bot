package ru.zelginni.tinycerberusbot.pythia

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/bot/predictions")
class PythiaPredictionController(
    private val service: PythiaPredictionService,
) {
    @GetMapping
    fun getPredictions(): List<PythiaPredictionView> {
        logger.info("Admin API call get pythia predictions")
        return service.getAllPredictions()
    }

    @PostMapping
    fun addPrediction(@RequestBody request: AddPythiaPredictionRequest): PythiaPredictionView {
        logger.info("Admin API call add pythia prediction")
        return service.addPrediction(request.text)
    }

    @DeleteMapping("/{id}")
    fun deletePrediction(@PathVariable id: Long): ResponseEntity<String> {
        logger.info("Admin API call delete pythia prediction id={}", id)
        service.deletePrediction(id)
        return ResponseEntity.ok("Prediction $id deleted")
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(PythiaPredictionController::class.java)
    }
}

package ru.zelginni.tinycerberusbot.pythia

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import kotlin.random.Random

@Service
class PythiaPredictionService(
    private val repository: PythiaPredictionRepository,
) {
    @Transactional(readOnly = true)
    fun getAllPredictions(): List<PythiaPredictionView> =
        repository.findAll()
            .sortedBy { it.id }
            .map { it.toView() }

    @Transactional
    fun addPrediction(text: String): PythiaPredictionView {
        val preparedText = text.trim()
        if (preparedText.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Prediction text must not be blank")
        }

        return repository.saveAndFlush(PythiaPrediction(text = preparedText)).toView()
    }

    @Transactional
    fun deletePrediction(id: Long) {
        if (!repository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Prediction $id not found")
        }
        repository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getRandomPrediction(): PythiaPredictionView? {
        val predictions = repository.findAll()
        if (predictions.isEmpty()) {
            return null
        }
        return predictions[Random.nextInt(predictions.size)].toView()
    }

    private fun PythiaPrediction.toView(): PythiaPredictionView =
        PythiaPredictionView(id = id ?: 0, text = text)
}

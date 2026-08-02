package ru.zelginni.tinycerberusbot.pythia

data class PythiaPredictionView(
    val id: Long,
    val text: String,
)

data class AddPythiaPredictionRequest(
    val text: String = "",
)

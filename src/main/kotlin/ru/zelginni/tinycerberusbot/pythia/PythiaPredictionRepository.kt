package ru.zelginni.tinycerberusbot.pythia

import org.springframework.data.jpa.repository.JpaRepository

interface PythiaPredictionRepository : JpaRepository<PythiaPrediction, Long>

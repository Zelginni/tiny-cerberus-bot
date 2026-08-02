package ru.zelginni.tinycerberusbot.pythia

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "cerberus", name = "pythia_predictions")
data class PythiaPrediction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long? = null,
    @Column(name = "prediction_text", nullable = false)
    var text: String = "",
)

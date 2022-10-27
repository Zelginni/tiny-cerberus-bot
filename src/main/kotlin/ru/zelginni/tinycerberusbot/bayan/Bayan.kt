package ru.zelginni.tinycerberusbot.bayan

import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "bayan")
data class Bayan(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        var id: Long? = null,
        @Column(name = "response")
        var response: String? = null,
)

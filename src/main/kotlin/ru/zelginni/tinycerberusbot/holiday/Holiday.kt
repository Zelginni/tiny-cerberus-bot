package ru.zelginni.tinycerberusbot.holiday

import javax.persistence.*

@Entity
@Table(schema = "cerberus", name = "holiday")
class Holiday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long? = null
    @Column
    var name: String? = null
    @Column
    var day: Int? = null
    @Column
    var month: Int? = null
}
package org.megamind.rdc_etat_civil.territoire.commune

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.territoire.entite.Entite

@Entity
@Table(name = "communes")

data class Commune(

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long?=null,
    val designation: String,

    @ManyToOne
    @JoinColumn(name = "entiteId")
    val entite: Entite
)

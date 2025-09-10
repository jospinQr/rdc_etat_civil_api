package org.megamind.rdc_etat_civil.territoire.quarier

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.territoire.commune.Commune

@Entity
@Table(name = "quartiers")
data class Quartier(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val designation: String,

    @ManyToOne
    @JoinColumn(name = "communeId")
    val commune: Commune
)
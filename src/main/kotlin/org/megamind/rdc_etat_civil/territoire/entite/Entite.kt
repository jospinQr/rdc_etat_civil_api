package org.megamind.rdc_etat_civil.territoire.entite

import org.megamind.rdc_etat_civil.territoire.province.Province
import jakarta.persistence.*

@Entity
@Table(name = "entites")

data class Entite(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val designation: String,
    val estVille: Boolean,

    @ManyToOne
    @JoinColumn(name = "ProvinceId")
    val province: Province,

    )
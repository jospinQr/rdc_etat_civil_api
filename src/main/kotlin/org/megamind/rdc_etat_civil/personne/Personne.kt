package org.megamind.rdc_etat_civil.personne

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate


@Entity
@Table(name = "personnes")
data class Personne(


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String,
    val sexe: Sexe,
    val lieuNaiss: String,
    val dateNaissance: LocalDate,
    val profession: String,
    val nationalite: String,
    val communeChefferie: String,
    val quartierGroup: String,
    val avenueVillage: String,
    val celluleLocalite: String,
    val telephone: String?,
    val email: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    val statut: StatutPersonne,

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_matrimoniale")
    val situationMatrimoniale: SituationMatrimoniale = SituationMatrimoniale.CELIBATAIRE,

    @Column(name = "date_deces")
    val dateDeces: LocalDate? = null,

    @Column(name = "lieu_deces")
    val lieuDeces: String? = null,

    @Column(name = "cause_deces")
    val causeDeces: String? = null,
)


enum class Sexe {
    MASCULIN, FEMININ
}


enum class StatutPersonne {
    VIVANT, DECEDE, INCONNU
}

enum class SituationMatrimoniale {
    CELIBATAIRE, MARIE, VEUF, DIVORCE, SEPARE
}
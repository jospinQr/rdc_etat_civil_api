package org.megamind.rdc_etat_civil.personne

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime


@Entity
@Table(
    name = "personnes",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["nom", "postnom", "prenom", "dateNaissance"])
    ]
)


data class Personne(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "nom", nullable = false, length = 50)
    val nom: String,

    @Column(name = "postnom", nullable = true, length = 50)
    val postnom: String,

    @Column(name = "prenom", nullable = true, length = 50)
    val prenom: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe", nullable = false, length = 10)
    val sexe: Sexe,

    @Column(name = "lieu_naissance", nullable = true, length = 100)
    val lieuNaiss: String ?= null,

    @Column(name = "date_naissance", nullable = true)
    val dateNaissance: LocalDate? =null,

    @Column(name = "heure_deces", nullable = true)
    val heureNaissance: LocalTime? = null,

    @Column(name = "profession", length = 100)
    val profession: String? = null,

    @Column(name = "nationalite", length = 50)
    val nationalite: String? = null,

    @Column(name = "commune_chefferie", length = 50)
    val communeChefferie: String? = null,

    @Column(name = "quartier_group", length = 50)
    val quartierGroup: String? = null,

    @Column(name = "avenue_village", length = 50)
    val avenueVillage: String? = null,

    @Column(name = "cellule_localite", length = 50)
    val celluleLocalite: String? = null,

    @Column(name = "telephone", length = 20)
    val telephone: String? = null,

    @Column(name = "email", length = 50)
    val email: String? = null,

    // Relation vers le père
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pere_id", foreignKey = ForeignKey(name = "fk_personne_pere"))
    val pere: Personne? = null,

    // Relation vers la mère
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mere_id", foreignKey = ForeignKey(name = "fk_personne_mere"))
    val mere: Personne? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 10)
    val statut: StatutPersonne = StatutPersonne.VIVANT,

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_matrimoniale", nullable = false, length = 10)
    val situationMatrimoniale: SituationMatrimoniale = SituationMatrimoniale.CELIBATAIRE
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

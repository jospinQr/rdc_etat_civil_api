package org.megamind.rdc_etat_civil.naissance

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import java.time.LocalDate

@Entity
@Table(
    name = "actes_naissance",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["numero_acte"])
    ]
)
data class ActeNaissance(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // Numéro unique de l'acte
    @Column(name = "numero_acte", nullable = false, unique = true, length = 30)
    val numeroActe: String,

    // Enfant concerné par l'acte
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "enfant_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_acte_naissance_enfant")
    )
    val enfant: Personne,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id", nullable = false)
    val commune: Commune,

    // Personne qui a déclaré la naissance - nom du déclarant
    @Column(name = "declarant", length = 100)
    val declarant: String? = null,

    // Officier d'état civil qui enregistre l'acte
    @Column(name = "officier", nullable = false, length = 100)
    val officier: String,

    // Date d’enregistrement de l’acte
    @Column(name = "date_enregistrement", nullable = false)
    val dateEnregistrement: LocalDate = LocalDate.now()
)

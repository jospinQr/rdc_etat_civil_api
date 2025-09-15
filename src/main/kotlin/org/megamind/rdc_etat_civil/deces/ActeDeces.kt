package org.megamind.rdc_etat_civil.deces

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.personne.Personne
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(
    name = "actes_deces",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["numero_acte"])
    ]
)
data class ActeDeces(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // Numéro unique de l'acte de décès
    @Column(name = "numero_acte", nullable = false, unique = true, length = 30)
    val numeroActe: String,

    // Personne concernée (décédée)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "defunt_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_acte_deces_defunt")
    )
    val defunt: Personne,

    // Date du décès
    @Column(name = "date_deces", nullable = false)
    val dateDeces: LocalDate,
    @Column(name = "heure_deces")


    val heureDeces: LocalTime? = null,
    // Lieu du décès
    @Column(name = "lieu_deces", length = 150, nullable = false)
    val lieuDeces: String,

    // Déclarant (facultatif) - nom de la personne qui déclare le décès
    @Column(name = "declarant", length = 100)
    val declarant: String? = null,

    // Officier d'état civil
    @Column(name = "officier", length = 100, nullable = false)
    val officier: String,

    // Date d'enregistrement dans l'état civil
    @Column(name = "date_enregistrement", nullable = false)
    val dateEnregistrement: LocalDate = LocalDate.now()
)

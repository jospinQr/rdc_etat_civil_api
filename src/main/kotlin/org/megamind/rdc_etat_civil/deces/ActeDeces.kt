package org.megamind.rdc_etat_civil.deces

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.territoire.commune.Commune
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

    // Commune où le décès a été enregistré
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id", nullable = false)
    val commune: Commune,

    // Date du décès
    @Column(name = "date_deces", nullable = false)
    val dateDeces: LocalDate,

    // Heure du décès (optionnelle)
    @Column(name = "heure_deces")
    val heureDeces: LocalTime? = null,

    // Lieu du décès
    @Column(name = "lieu_deces", length = 150, nullable = false)
    val lieuDeces: String,

    // Cause du décès (optionnelle)
    @Column(name = "cause_deces", length = 200)
    val causeDeces: String? = null,

    // Déclarant (facultatif) - nom de la personne qui déclare le décès
    @Column(name = "declarant", length = 100)
    val declarant: String? = null,

    // Officier d'état civil
    @Column(name = "officier", length = 100, nullable = false)
    val officier: String,

    // Date d'enregistrement dans l'état civil
    @Column(name = "date_enregistrement", nullable = false)
    val dateEnregistrement: LocalDate = LocalDate.now(),

    // Témoins (optionnels)
    @Column(name = "temoin1", length = 100)
    val temoin1: String? = null,

    @Column(name = "temoin2", length = 100)
    val temoin2: String? = null,

    // Médecin qui a constaté le décès (optionnel)
    @Column(name = "medecin", length = 100)
    val medecin: String? = null,

    // Observations supplémentaires
    @Column(name = "observations", length = 500)
    val observations: String? = null
)

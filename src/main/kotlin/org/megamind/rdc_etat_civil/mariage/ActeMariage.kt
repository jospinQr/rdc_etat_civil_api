package org.megamind.rdc_etat_civil.mariage

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.personne.Personne
import java.time.LocalDate

@Entity
@Table(
    name = "actes_mariage",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["numero_acte"])
    ]
)
data class ActeDeMariage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // Numéro unique de l'acte de mariage
    @Column(name = "numero_acte", nullable = false, unique = true, length = 50)
    val numeroActe: String,

    // Époux
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "epoux_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_acte_mariage_epoux")
    )
    val epoux: Personne,

    // Épouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "epouse_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_acte_mariage_epouse")
    )
    val epouse: Personne,

    // Date du mariage
    @Column(name = "date_mariage", nullable = false)
    val dateMariage: LocalDate,

    // Lieu du mariage (peut être une commune, paroisse, etc.)
    @Column(name = "lieu_mariage", length = 255, nullable = false)
    val lieuMariage: String,

    // Officier d'état civil
    @Column(name = "officier", length = 255, nullable = false)
    val officier: String,

    @Column(name = "temoin1", length = 255, nullable = false)
    val temoin1: String? = null,

    @Column(name = "temoin2", length = 255, nullable = true)
    val temoin2: String? = null,
)

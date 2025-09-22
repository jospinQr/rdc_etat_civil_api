package org.megamind.rdc_etat_civil.deces.dto

import org.megamind.rdc_etat_civil.deces.ActeDeces
import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate
import java.time.LocalTime

/**
 * DTO simplifié pour l'affichage des actes de décès dans les listes
 */
data class ActeDecesSimple(
    val id: Long,
    val numeroActe: String,
    val nomCompletDefunt: String,
    val sexeDefunt: Sexe,
    val dateDeces: LocalDate,
    val heureDeces: LocalTime?,
    val lieuDeces: String,
    val dateEnregistrement: LocalDate,
    val commune: String,
    val entite: String,
    val province: String,
    val officier: String,
    val ageAuDeces: Int?
) {
    companion object {
        fun fromEntity(acte: ActeDeces): ActeDecesSimple {
            val ageAuDeces = acte.defunt.dateNaissance?.let { dateNaissance ->
                java.time.temporal.ChronoUnit.YEARS.between(dateNaissance, acte.dateDeces).toInt()
            }
            
            return ActeDecesSimple(
                id = acte.id,
                numeroActe = acte.numeroActe,
                nomCompletDefunt = buildString {
                    append(acte.defunt.nom)
                    append(" ")
                    append(acte.defunt.postnom)
                    acte.defunt.prenom?.let { append(" $it") }
                }.trim(),
                sexeDefunt = acte.defunt.sexe,
                dateDeces = acte.dateDeces,
                heureDeces = acte.heureDeces,
                lieuDeces = acte.lieuDeces,
                dateEnregistrement = acte.dateEnregistrement,
                commune = acte.commune.designation,
                entite = acte.commune.entite.designation,
                province = acte.commune.entite.province.designation,
                officier = acte.officier,
                ageAuDeces = ageAuDeces
            )
        }
    }
}


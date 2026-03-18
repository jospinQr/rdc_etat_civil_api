package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.ActeMariage
import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import java.time.LocalDate

/**
 * DTO simplifié pour l'affichage des actes de mariage dans les listes
 */
data class ActeMariageSimple(
    val id: Long,
    val numeroActe: String,
    val nomEpoux: String,
    val nomEpouse: String,
    val dateMariage: LocalDate,
    val lieuMariage: String,
    val regimeMatrimonial: RegimeMatrimonial,
    val commune: String,
    val officier: String
) {
    companion object {
        fun fromEntity(acte: ActeMariage): ActeMariageSimple {
            return ActeMariageSimple(
                id = acte.id,
                numeroActe = acte.numeroActe,
                nomEpoux = "${acte.epoux.nom} ${acte.epoux.postnom} ${acte.epoux.prenom ?: ""}".trim(),
                nomEpouse = "${acte.epouse.nom} ${acte.epouse.postnom} ${acte.epouse.prenom ?: ""}".trim(),
                dateMariage = acte.dateMariage,
                lieuMariage = acte.lieuMariage,
                regimeMatrimonial = acte.regimeMatrimonial,
                commune = acte.commune.designation,
                officier = acte.officier
            )
        }
    }
}

package org.megamind.rdc_etat_civil.deces.dto

import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate

/**
 * DTO pour les critères de recherche d'actes de décès
 */
data class ActeDecesSearchCriteria(
    val numeroActe: String? = null,
    val nomDefunt: String? = null,
    val postnomDefunt: String? = null,
    val prenomDefunt: String? = null,
    val sexeDefunt: Sexe? = null,
    val communeId: Long? = null,
    val communeNom: String? = null,
    val entiteId: Long? = null,
    val entiteNom: String? = null,
    val provinceId: Long? = null,
    val provinceNom: String? = null,
    val officier: String? = null,
    val declarant: String? = null,
    val medecin: String? = null,
    val lieuDeces: String? = null,
    val causeDeces: String? = null,
    val dateDecesDebut: LocalDate? = null,
    val dateDecesFin: LocalDate? = null,
    val dateEnregistrementDebut: LocalDate? = null,
    val dateEnregistrementFin: LocalDate? = null,
    val ageMinAuDeces: Int? = null,
    val ageMaxAuDeces: Int? = null,
    val avecTemoins: Boolean? = null,
    val enregistrementTardif: Boolean? = null,
    val avecCauseDeces: Boolean? = null,
    val avecMedecin: Boolean? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "dateEnregistrement",
    val sortDirection: String = "DESC"
) {
    /**
     * Calculer la date de début finale en tenant compte de l'âge minimum
     */
    fun getFinalDateDebut(): LocalDate? {
        return when {
            dateDecesDebut != null && ageMaxAuDeces != null -> {
                val dateMax = LocalDate.now().minusYears(ageMaxAuDeces.toLong())
                if (dateDecesDebut.isAfter(dateMax)) dateDecesDebut else dateMax
            }
            dateDecesDebut != null -> dateDecesDebut
            ageMaxAuDeces != null -> LocalDate.now().minusYears(ageMaxAuDeces.toLong())
            else -> null
        }
    }

    /**
     * Calculer la date de fin finale en tenant compte de l'âge minimum
     */
    fun getFinalDateFin(): LocalDate? {
        return when {
            dateDecesFin != null && ageMinAuDeces != null -> {
                val dateMin = LocalDate.now().minusYears(ageMinAuDeces.toLong())
                if (dateDecesFin.isBefore(dateMin)) dateDecesFin else dateMin
            }
            dateDecesFin != null -> dateDecesFin
            ageMinAuDeces != null -> LocalDate.now().minusYears(ageMinAuDeces.toLong())
            else -> null
        }
    }
}


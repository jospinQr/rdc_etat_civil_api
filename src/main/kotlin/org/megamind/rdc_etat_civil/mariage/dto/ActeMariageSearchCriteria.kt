package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate

/**
 * DTO pour les critères de recherche d'actes de mariage
 */
data class ActeMariageSearchCriteria(
    val numeroActe: String? = null,
    val nomEpoux: String? = null,
    val postnomEpoux: String? = null,
    val prenomEpoux: String? = null,
    val nomEpouse: String? = null,
    val postnomEpouse: String? = null,
    val prenomEpouse: String? = null,
    val sexeEpoux: Sexe? = null,
    val sexeEpouse: Sexe? = null,
    val regimeMatrimonial: RegimeMatrimonial? = null,
    val officier: String? = null,
    val communeId: Long? = null,
    val entiteId: Long? = null,
    val provinceId: Long? = null,
    val dateMariageDebut: LocalDate? = null,
    val dateMariageFin: LocalDate? = null,
    val lieuMariage: String? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "dateMariage",
    val sortDirection: String = "DESC"
)

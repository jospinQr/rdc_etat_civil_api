package org.megamind.rdc_etat_civil.personne.dto

import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.personne.SituationMatrimoniale
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import java.time.LocalDate

data class PersonneSearchCriteria(
    val nom: String? = null,
    val postnom: String? = null,
    val prenom: String? = null,
    val sexe: Sexe? = null,
    val statut: StatutPersonne? = null,
    val situationMatrimoniale: SituationMatrimoniale? = null,
    val commune: String? = null,
    val lieuNaissance: String? = null,
    val dateNaissanceDebut: LocalDate? = null,
    val dateNaissanceFin: LocalDate? = null,
    val ageMin: Int? = null,
    val ageMax: Int? = null,
    val nationalite: String? = null,
    val profession: String? = null,
    
    // Pagination
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "nom",
    val sortDirection: String = "ASC"
) {
    /**
     * Convertit les âges en dates pour la recherche
     */
    fun getDateNaissanceDebutFromAge(): LocalDate? {
        return ageMax?.let { LocalDate.now().minusYears(it.toLong()).minusDays(1) }
    }
    
    fun getDateNaissanceFinFromAge(): LocalDate? {
        return ageMin?.let { LocalDate.now().minusYears(it.toLong()) }
    }
    
    /**
     * Détermine les dates finales en combinant les critères de date et d'âge
     */
    fun getFinalDateDebut(): LocalDate? {
        val dateFromAge = getDateNaissanceDebutFromAge()
        return when {
            dateNaissanceDebut != null && dateFromAge != null -> 
                maxOf(dateNaissanceDebut, dateFromAge)
            dateNaissanceDebut != null -> dateNaissanceDebut
            dateFromAge != null -> dateFromAge
            else -> null
        }
    }
    
    fun getFinalDateFin(): LocalDate? {
        val dateFromAge = getDateNaissanceFinFromAge()
        return when {
            dateNaissanceFin != null && dateFromAge != null -> 
                minOf(dateNaissanceFin, dateFromAge)
            dateNaissanceFin != null -> dateNaissanceFin
            dateFromAge != null -> dateFromAge
            else -> null
        }
    }
}

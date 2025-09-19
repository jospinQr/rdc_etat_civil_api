package org.megamind.rdc_etat_civil.naissance.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import org.megamind.rdc_etat_civil.naissance.ActeNaissance
import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate
import java.time.LocalTime

/**
 * DTO simplifié pour les réponses rapides des actes de naissance
 */
data class ActeNaissanceSimple(
    val id: Long,
    val numeroActe: String,
    val nomCompletEnfant: String,
    val dateNaissance: LocalDate?,
    val dateEnregistrement: LocalDate,
    val commune: String,
    val officier: String
) {
    companion object {
        fun fromEntity(acte: ActeNaissance): ActeNaissanceSimple {
            return ActeNaissanceSimple(
                id = acte.id,
                numeroActe = acte.numeroActe,
                nomCompletEnfant = buildString {
                    append(acte.enfant.nom)
                    append(" ")
                    append(acte.enfant.postnom)
                    acte.enfant.prenom?.let { append(" $it") }
                }.trim(),
                dateNaissance = acte.enfant.dateNaissance,
                dateEnregistrement = acte.dateEnregistrement,
                commune = acte.commune.designation,
                officier = acte.officier
            )
        }
    }
}

/**
 * DTO pour les critères de recherche
 */
data class ActeNaissanceSearchCriteria(
    val numeroActe: String? = null,
    val nomEnfant: String? = null,
    val postnomEnfant: String? = null,
    val prenomEnfant: String? = null,
    val communeId: Long? = null,
    val entiteId: Long? = null,
    val provinceId: Long? = null,
    val officier: String? = null,
    val declarant: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrementDebut: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrementFin: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateNaissanceDebut: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateNaissanceFin: LocalDate? = null,
    
    val avecTemoins: Boolean? = null,
    val enregistrementTardif: Boolean? = null,
    
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "dateEnregistrement",
    val sortDirection: String = "DESC"
)

/**
 * DTO pour les statistiques
 */
data class ActeNaissanceStatistiques(
    val totalActes: Long,
    val actesAujourdhui: Long,
    val actesCeMois: Long,
    val actesEnregistrementTardif: Long,
    val repartitionParCommune: List<Map<String, Any>>,
    val repartitionParOfficier: List<Map<String, Any>>,
    val repartitionParMois: List<Map<String, Any>>
)
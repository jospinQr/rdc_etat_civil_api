package org.megamind.rdc_etat_civil.naissance.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDate

/**
 * DTO pour les requêtes d'enregistrement d'actes de naissance
 */
data class ActeNaissanceRequest(
    
    @field:NotBlank(message = "Le numéro d'acte est obligatoire")
    @field:Size(max = 30, message = "Le numéro d'acte ne peut pas dépasser 30 caractères")
    val numeroActe: String,
    
    @field:NotNull(message = "L'ID de l'enfant est obligatoire")
    @field:Positive(message = "L'ID de l'enfant doit être positif")
    val enfantId: Long,
    
    @field:NotNull(message = "L'ID de la commune est obligatoire")
    @field:Positive(message = "L'ID de la commune doit être positif")
    val communeId: Long,
    
    @field:NotBlank(message = "L'officier d'état civil est obligatoire")
    @field:Size(max = 100, message = "Le nom de l'officier ne peut pas dépasser 100 caractères")
    val officier: String,
    
    @field:Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    val declarant: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrement: LocalDate = LocalDate.now(),
    
    val temoin1: String? = null,
    val temoin2: String? = null
)

/**
 * DTO pour les requêtes de recherche d'actes de naissance
 */
data class ActeNaissanceSearchRequest(
    val numeroActe: String? = null,
    val nomEnfant: String? = null,
    val postnomEnfant: String? = null,
    val prenomEnfant: String? = null,
    val communeId: Long? = null,
    val communeNom: String? = null,
    val entiteId: Long? = null,
    val entiteNom: String? = null,
    val provinceId: Long? = null,
    val provinceNom: String? = null,
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
    val typeEntiteVille: Boolean? = null
)

/**
 * DTO pour les requêtes de mise à jour d'actes de naissance
 */
data class ActeNaissanceUpdateRequest(
    @field:Size(max = 30, message = "Le numéro d'acte ne peut pas dépasser 30 caractères")
    val numeroActe: String? = null,
    
    @field:Positive(message = "L'ID de la commune doit être positif")
    val communeId: Long? = null,
    
    @field:Size(max = 100, message = "Le nom de l'officier ne peut pas dépasser 100 caractères")
    val officier: String? = null,
    
    @field:Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    val declarant: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrement: LocalDate? = null,
    
    val temoin1: String? = null,
    val temoin2: String? = null
)

/**
 * DTO simplifié pour l'affichage rapide des actes
 */
data class ActeNaissanceResumeResponse(
    val id: Long,
    val numeroActe: String,
    val nomCompletEnfant: String,
    val dateNaissance: LocalDate?,
    val dateEnregistrement: LocalDate,
    val commune: String,
    val entite: String,
    val province: String,
    val officier: String
) {
    companion object {
        fun fromEntity(acte: org.megamind.rdc_etat_civil.naissance.ActeNaissance): ActeNaissanceResumeResponse {
            return ActeNaissanceResumeResponse(
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
                entite = acte.commune.entite.designation,
                province = acte.commune.entite.province.designation,
                officier = acte.officier
            )
        }
    }
}

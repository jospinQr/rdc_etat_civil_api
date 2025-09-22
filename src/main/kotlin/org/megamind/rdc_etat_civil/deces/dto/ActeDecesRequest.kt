package org.megamind.rdc_etat_civil.deces.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalTime

/**
 * DTO pour les requêtes d'enregistrement d'actes de décès
 */
data class ActeDecesRequest(
    
    @field:NotBlank(message = "Le numéro d'acte est obligatoire")
    @field:Size(max = 30, message = "Le numéro d'acte ne peut pas dépasser 30 caractères")
    val numeroActe: String,
    
    @field:NotNull(message = "L'ID du défunt est obligatoire")
    @field:Positive(message = "L'ID du défunt doit être positif")
    val defuntId: Long,
    
    @field:NotNull(message = "L'ID de la commune est obligatoire")
    @field:Positive(message = "L'ID de la commune doit être positif")
    val communeId: Long,
    
    @field:NotNull(message = "La date de décès est obligatoire")
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateDeces: LocalDate,
    
    @field:JsonFormat(pattern = "HH:mm")
    val heureDeces: LocalTime? = null,
    
    @field:NotBlank(message = "Le lieu de décès est obligatoire")
    @field:Size(max = 150, message = "Le lieu de décès ne peut pas dépasser 150 caractères")
    val lieuDeces: String,
    
    @field:Size(max = 200, message = "La cause de décès ne peut pas dépasser 200 caractères")
    val causeDeces: String? = null,
    
    @field:NotBlank(message = "L'officier d'état civil est obligatoire")
    @field:Size(max = 100, message = "Le nom de l'officier ne peut pas dépasser 100 caractères")
    val officier: String,
    
    @field:Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    val declarant: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrement: LocalDate = LocalDate.now(),
    
    val temoin1: String? = null,
    val temoin2: String? = null,
    
    @field:Size(max = 100, message = "Le nom du médecin ne peut pas dépasser 100 caractères")
    val medecin: String? = null,
    
    @field:Size(max = 500, message = "Les observations ne peuvent pas dépasser 500 caractères")
    val observations: String? = null
)

/**
 * DTO pour les requêtes de recherche d'actes de décès
 */
data class ActeDecesSearchRequest(
    val numeroActe: String? = null,
    val nomDefunt: String? = null,
    val postnomDefunt: String? = null,
    val prenomDefunt: String? = null,
    val communeId: Long? = null,
    val communeNom: String? = null,
    val entiteId: Long? = null,
    val entiteNom: String? = null,
    val provinceId: Long? = null,
    val provinceNom: String? = null,
    val officier: String? = null,
    val declarant: String? = null,
    val medecin: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateDecesDebut: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateDecesFin: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrementDebut: LocalDate? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrementFin: LocalDate? = null,
    
    val lieuDeces: String? = null,
    val causeDeces: String? = null,
    val avecTemoins: Boolean? = null,
    val enregistrementTardif: Boolean? = null
)

/**
 * DTO pour les requêtes de mise à jour d'actes de décès
 */
data class ActeDecesUpdateRequest(
    @field:Size(max = 30, message = "Le numéro d'acte ne peut pas dépasser 30 caractères")
    val numeroActe: String? = null,
    
    @field:Positive(message = "L'ID de la commune doit être positif")
    val communeId: Long? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateDeces: LocalDate? = null,
    
    @field:JsonFormat(pattern = "HH:mm")
    val heureDeces: LocalTime? = null,
    
    @field:Size(max = 150, message = "Le lieu de décès ne peut pas dépasser 150 caractères")
    val lieuDeces: String? = null,
    
    @field:Size(max = 200, message = "La cause de décès ne peut pas dépasser 200 caractères")
    val causeDeces: String? = null,
    
    @field:Size(max = 100, message = "Le nom de l'officier ne peut pas dépasser 100 caractères")
    val officier: String? = null,
    
    @field:Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    val declarant: String? = null,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateEnregistrement: LocalDate? = null,
    
    val temoin1: String? = null,
    val temoin2: String? = null,
    
    @field:Size(max = 100, message = "Le nom du médecin ne peut pas dépasser 100 caractères")
    val medecin: String? = null,
    
    @field:Size(max = 500, message = "Les observations ne peuvent pas dépasser 500 caractères")
    val observations: String? = null
)

/**
 * DTO simplifié pour l'affichage rapide des actes
 */
data class ActeDecesResumeResponse(
    val id: Long,
    val numeroActe: String,
    val nomCompletDefunt: String,
    val dateDeces: LocalDate,
    val dateEnregistrement: LocalDate,
    val lieuDeces: String,
    val commune: String,
    val entite: String,
    val province: String,
    val officier: String
) {
    companion object {
        fun fromEntity(acte: org.megamind.rdc_etat_civil.deces.ActeDeces): ActeDecesResumeResponse {
            return ActeDecesResumeResponse(
                id = acte.id,
                numeroActe = acte.numeroActe,
                nomCompletDefunt = buildString {
                    append(acte.defunt.nom)
                    append(" ")
                    append(acte.defunt.postnom)
                    acte.defunt.prenom?.let { append(" $it") }
                }.trim(),
                dateDeces = acte.dateDeces,
                dateEnregistrement = acte.dateEnregistrement,
                lieuDeces = acte.lieuDeces,
                commune = acte.commune.designation,
                entite = acte.commune.entite.designation,
                province = acte.commune.entite.province.designation,
                officier = acte.officier
            )
        }
    }
}


package org.megamind.rdc_etat_civil.naissance.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate

/**
 * DTO pour l'enregistrement en lot d'actes de naissance
 */
data class ActeNaissanceBatchRequest(
    
    @field:NotEmpty(message = "Le lot ne peut pas être vide")
    @field:Size(max = 100, message = "Un lot ne peut pas contenir plus de 100 actes")
    @field:Valid
    val actes: List<ActeNaissanceItemRequest>,
    
    @field:Size(max = 200, message = "La description du lot ne peut pas dépasser 200 caractères")
    val descriptionLot: String? = null,
    
    @field:NotBlank(message = "Le responsable du lot est obligatoire")
    @field:Size(max = 100, message = "Le nom du responsable ne peut pas dépasser 100 caractères")
    val responsableLot: String,
    
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateTraitement: LocalDate = LocalDate.now(),
    
    val validationStricte: Boolean = true
)

/**
 * DTO pour un acte individuel dans un lot
 */
data class ActeNaissanceItemRequest(
    
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
    val temoin2: String? = null,
    
    // Métadonnées pour le traitement en lot
    val numeroOrdre: Int? = null,
    val reference: String? = null
)

/**
 * DTO pour la réponse d'un lot d'actes
 */
data class ActeNaissanceBatchResponse(
    val success: Boolean,
    val message: String,
    val totalActes: Int,
    val actesTraites: Int,
    val actesReussis: Int,
    val actesEchecs: Int,
    val tempsTraitement: Long, // en millisecondes
    val resultats: List<ActeNaissanceBatchItemResponse>,
    val statistiques: BatchStatistiques? = null
)

/**
 * DTO pour le résultat d'un acte individuel dans un lot
 */
data class ActeNaissanceBatchItemResponse(
    val numeroActe: String,
    val enfantId: Long,
    val success: Boolean,
    val acteId: Long? = null,
    val erreur: String? = null,
    val numeroOrdre: Int? = null,
    val reference: String? = null
)

/**
 * DTO pour les statistiques d'un lot
 */
data class BatchStatistiques(
    val repartitionParCommune: Map<String, Int>,
    val repartitionParOfficier: Map<String, Int>,
    val repartitionParDate: Map<LocalDate, Int>,
    val actesAvecTemoins: Int,
    val actesSansTemoins: Int,
    val enregistrementsTardifs: Int
)

/**
 * DTO pour valider un lot avant traitement
 */
data class BatchValidationRequest(
    @field:Valid
    val actes: List<ActeNaissanceItemRequest>,
    val validationComplete: Boolean = true
)

/**
 * DTO pour le résultat de validation d'un lot
 */
data class BatchValidationResponse(
    val valide: Boolean,
    val nombreActes: Int,
    val erreursValidation: List<BatchValidationError>,
    val alertes: List<BatchValidationAlert>,
    val statistiquesPreliminaires: BatchStatistiques? = null
)

/**
 * DTO pour les erreurs de validation
 */
data class BatchValidationError(
    val numeroActe: String?,
    val enfantId: Long?,
    val numeroOrdre: Int?,
    val typeErreur: String,
    val message: String,
    val champ: String? = null
)

/**
 * DTO pour les alertes de validation
 */
data class BatchValidationAlert(
    val numeroActe: String?,
    val enfantId: Long?,
    val numeroOrdre: Int?,
    val typeAlerte: String,
    val message: String,
    val severite: AlerteSeverite = AlerteSeverite.INFO
)

/**
 * Énumération pour la sévérité des alertes
 */
enum class AlerteSeverite {
    INFO, WARNING, CRITICAL
}

/**
 * DTO pour le suivi de progression d'un lot
 */
data class BatchProgressResponse(
    val batchId: String,
    val statut: BatchStatut,
    val progression: Int, // pourcentage 0-100
    val actesTraites: Int,
    val totalActes: Int,
    val tempsEcoule: Long, // en millisecondes
    val tempsEstimeRestant: Long? = null, // en millisecondes
    val derniereActivite: LocalDate = LocalDate.now()
)

/**
 * Énumération pour le statut d'un lot
 */
enum class BatchStatut {
    EN_ATTENTE,
    EN_COURS,
    TERMINE_SUCCES,
    TERMINE_ECHEC,
    ANNULE,
    ERREUR
}

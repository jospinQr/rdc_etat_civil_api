package org.megamind.rdc_etat_civil.mariage.dto

/**
 * DTO de réponse pour le traitement par lot d'actes de mariage
 */
data class ActeMariageBatchResponse(
    val success: Boolean,
    val message: String,
    val totalActes: Int,
    val actesTraites: Int,
    val actesReussis: Int,
    val actesEchecs: Int,
    val tempsTraitement: Long,
    val resultats: List<ActeMariageBatchItemResponse>,
    val statistiques: BatchStatistiques
)

/**
 * DTO pour le résultat d'un acte de mariage dans un lot
 */
data class ActeMariageBatchItemResponse(
    val numeroActe: String,
    val epouxId: Long,
    val epouseId: Long,
    val success: Boolean,
    val acteId: Long? = null,
    val erreur: String? = null,
    val numeroOrdre: Int,
    val reference: String? = null
)

/**
 * DTO pour les statistiques d'un lot traité
 */
data class BatchStatistiques(
    val repartitionParCommune: Map<String, Int>,
    val repartitionParOfficier: Map<String, Int>,
    val repartitionParDate: Map<String, Int>,
    val mariagesAvecTemoins: Int,
    val mariagesSansTemoins: Int
)

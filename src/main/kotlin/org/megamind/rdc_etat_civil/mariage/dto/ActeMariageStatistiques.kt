package org.megamind.rdc_etat_civil.mariage.dto

/**
 * DTO pour les statistiques des actes de mariage
 */
data class ActeMariageStatistiques(
    val totalActes: Long,
    val actesAujourdhui: Long,
    val actesCeMois: Long,
    val repartitionParCommune: List<Map<String, Any>>,
    val repartitionParOfficier: List<Map<String, Any>>,
    val repartitionParMois: List<Map<String, Any>>,
    val repartitionParRegime: List<Map<String, Any>>,
    val repartitionParSexeEpoux: Map<String, Long>,
    val repartitionParSexeEpouse: Map<String, Long>,
    val moyenneAgeEpouxAuMariage: Double?,
    val moyenneAgeEpouseAuMariage: Double?,
    val mariagesAvecTemoins: Long,
    val mariagesSansTemoins: Long
)

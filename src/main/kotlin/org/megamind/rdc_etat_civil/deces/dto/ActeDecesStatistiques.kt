package org.megamind.rdc_etat_civil.deces.dto

import java.time.LocalDate

/**
 * DTO pour les statistiques des actes de décès
 */
data class ActeDecesStatistiques(
    val totalActes: Long,
    val actesAujourdhui: Long,
    val actesCeMois: Long,
    val actesEnregistrementTardif: Long,
    val repartitionParCommune: List<Map<String, Any>>,
    val repartitionParOfficier: List<Map<String, Any>>,
    val repartitionParMois: List<Map<String, Any>>,
    val repartitionParCause: List<Map<String, Any>>,
    val repartitionParAge: Map<String, Long>,
    val repartitionParSexe: Map<String, Long>,
    val moyenneAgeAuDeces: Double?,
    val actesAvecCause: Long,
    val actesSansCause: Long,
    val actesAvecMedecin: Long,
    val actesSansMedecin: Long
)

/**
 * DTO pour les statistiques par tranche d'âge
 */
data class StatistiqueTrancheAge(
    val tranche: String,
    val nombre: Long,
    val pourcentage: Double
)

/**
 * DTO pour les statistiques par cause de décès
 */
data class StatistiqueCauseDeces(
    val cause: String,
    val nombre: Long,
    val pourcentage: Double
)

/**
 * DTO pour les statistiques par commune
 */
data class StatistiqueCommune(
    val commune: String,
    val entite: String,
    val province: String,
    val nombre: Long,
    val pourcentage: Double
)

/**
 * DTO pour les statistiques par officier
 */
data class StatistiqueOfficier(
    val officier: String,
    val commune: String,
    val nombre: Long,
    val pourcentage: Double
)

/**
 * DTO pour les statistiques mensuelles
 */
data class StatistiqueMensuelle(
    val annee: Int,
    val mois: Int,
    val nombre: Long,
    val pourcentage: Double
)


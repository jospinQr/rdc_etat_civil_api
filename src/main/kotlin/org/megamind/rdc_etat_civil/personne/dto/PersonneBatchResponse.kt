package org.megamind.rdc_etat_civil.personne.dto

/**
 * DTO pour la réponse de l'enregistrement de plusieurs personnes
 */
data class PersonneBatchResponse(
    val totalDemandees: Int,
    val totalCreees: Int,
    val totalEchecs: Int,
    val personnesCreees: List<PersonneResponse>,
    val echecs: List<PersonneEchecInfo>
)

/**
 * Information sur les échecs lors de l'enregistrement en lot
 */
data class PersonneEchecInfo(
    val index: Int,
    val personne: PersonneRequest,
    val erreur: String
)


package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import java.time.LocalDate

/**
 * DTO pour la création d'actes de mariage par lot
 */
data class ActeMariageBatchRequest(
    val actes: List<ActeMariageItemRequest>
)

/**
 * DTO pour un acte de mariage dans un lot
 */
data class ActeMariageItemRequest(
    val numeroActe: String,
    val epouxId: Long,
    val epouseId: Long,
    val communeId: Long,
    val dateMariage: LocalDate,
    val lieuMariage: String,
    val regimeMatrimonial: RegimeMatrimonial,
    val officier: String,
    val temoin1: String? = null,
    val temoin2: String? = null,
    val numeroOrdre: Int? = null,
    val reference: String? = null
)

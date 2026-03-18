package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import java.time.LocalDate

/**
 * DTO pour la création d'un acte de mariage
 */
data class ActeMariageRequest(
    val numeroActe: String,
    val epouxId: Long,
    val epouseId: Long,
    val communeId: Long,
    val dateMariage: LocalDate,
    val lieuMariage: String,
    val regimeMatrimonial: RegimeMatrimonial,
    val officier: String,
    val temoin1: String? = null,
    val temoin2: String? = null
)

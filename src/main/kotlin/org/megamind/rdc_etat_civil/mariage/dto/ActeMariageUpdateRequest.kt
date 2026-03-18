package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import java.time.LocalDate

/**
 * DTO pour la modification d'un acte de mariage
 */
data class ActeMariageUpdateRequest(
    val numeroActe: String? = null,
    val communeId: Long? = null,
    val dateMariage: LocalDate? = null,
    val lieuMariage: String? = null,
    val regimeMatrimonial: RegimeMatrimonial? = null,
    val officier: String? = null,
    val temoin1: String? = null,
    val temoin2: String? = null
)

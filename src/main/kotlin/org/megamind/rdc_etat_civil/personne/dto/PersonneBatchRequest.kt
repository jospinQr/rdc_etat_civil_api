package org.megamind.rdc_etat_civil.personne.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * DTO pour l'enregistrement de plusieurs personnes en une seule fois
 */
data class PersonneBatchRequest(
    @field:NotEmpty(message = "La liste des personnes ne peut pas être vide")
    @field:Size(max = 500, message = "Maximum 100 personnes peuvent être enregistrées en une fois")
    @field:Valid
    val personnes: List<PersonneRequest>
)


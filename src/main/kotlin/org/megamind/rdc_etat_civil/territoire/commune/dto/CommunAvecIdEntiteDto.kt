package org.megamind.rdc_etat_civil.territoire.commune.dto

import org.megamind.rdc_etat_civil.territoire.commune.Commune

data class CommunAvecIdEntiteDto(

    val id: Long,
    val designation: String,
    val entiteId: Long

)


fun Commune.toCommunAvecIdEntiteDto(): CommunAvecIdEntiteDto {

    val entite = this.entite

    return CommunAvecIdEntiteDto(
        id = this.id,
        designation = this.designation,
        entiteId = entite.id
    )

}
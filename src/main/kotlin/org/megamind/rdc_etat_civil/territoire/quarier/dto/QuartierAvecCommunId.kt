package org.megamind.rdc_etat_civil.territoire.quarier.dto

import org.megamind.rdc_etat_civil.territoire.quarier.Quartier

data class QuartierAvecCommunId(val id: Long, val designation: String, val communeId: Long) {
}


fun Quartier.toQuartierAvecCommunId(): QuartierAvecCommunId {


    return QuartierAvecCommunId(

        id = this.id,
        designation = this.designation,
        communeId = this.commune.id
    )


}
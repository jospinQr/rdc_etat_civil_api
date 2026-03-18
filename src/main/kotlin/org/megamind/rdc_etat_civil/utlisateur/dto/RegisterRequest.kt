package org.megamind.rdc_etat_civil.utlisateur.dto

import org.megamind.rdc_etat_civil.utlisat.Role

data class RegisterRequest(
    val username: String,
    val password: String,
    val provinceId: Long?,
    val entiteId: Long?,
    val communeId: Long?,
    val role: Role,
)
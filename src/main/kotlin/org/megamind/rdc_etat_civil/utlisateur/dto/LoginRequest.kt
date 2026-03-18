package org.megamind.rdc_etat_civil.utlisateur.dto

data class LoginRequest(
    val username: String,
    val password: String,
)
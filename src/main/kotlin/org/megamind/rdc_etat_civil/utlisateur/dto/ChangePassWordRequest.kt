package org.megamind.rdc_etat_civil.utlisateur.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class ChangePasswordRequest(
    @field:NotBlank(message = "Le mot de passe actuel est obligatoire")
    val currentPassword: String,

    @field:Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    val newPassword: String? = null,

    @field:Size(min = 3, message = "Le nom d'utilisateur doit contenir au moins 3 caractères")
    val newUsername: String? = null
)
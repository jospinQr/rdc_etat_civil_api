package org.megamind.rdc_etat_civil.personne.dto

import jakarta.validation.constraints.*
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.personne.SituationMatrimoniale
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import java.time.LocalDate
import java.time.LocalTime

data class PersonneRequest(
    @field:NotBlank(message = "Le nom est obligatoire")
    @field:Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    val nom: String,

    @field:NotBlank(message = "Le postnom est obligatoire")
    @field:Size(max = 50, message = "Le postnom ne peut pas dépasser 50 caractères")
    val postnom: String,

    @field:Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    val prenom: String? = null,

    @field:NotNull(message = "Le sexe est obligatoire")
    val sexe: Sexe,

    @field:Size(max = 100, message = "Le lieu de naissance ne peut pas dépasser 100 caractères")
    val lieuNaiss: String? = null,

    @field:Past(message = "La date de naissance doit être dans le passé")
    val dateNaissance: LocalDate? = null,

    val heureNaissance: LocalTime? = null,

    @field:Size(max = 100, message = "La profession ne peut pas dépasser 100 caractères")
    val profession: String? = null,

    @field:Size(max = 50, message = "La nationalité ne peut pas dépasser 50 caractères")
    val nationalite: String? = "Congolaise",

    @field:Size(max = 50, message = "La commune/chefferie ne peut pas dépasser 50 caractères")
    val communeChefferie: String? = null,

    @field:Size(max = 50, message = "Le quartier/groupement ne peut pas dépasser 50 caractères")
    val quartierGroup: String? = null,

    @field:Size(max = 50, message = "L'avenue/village ne peut pas dépasser 50 caractères")
    val avenueVillage: String? = null,

    @field:Size(max = 50, message = "La cellule/localité ne peut pas dépasser 50 caractères")
    val celluleLocalite: String? = null,

    @field:Pattern(
        regexp = "^(\\+243|0)?[0-9]{9}$",
        message = "Format de téléphone invalide (ex: +243123456789 ou 0123456789)"
    )
    val telephone: String? = null,

    @field:Email(message = "Format d'email invalide")
    @field:Size(max = 50, message = "L'email ne peut pas dépasser 50 caractères")
    val email: String? = null,

    val pereId: Long? = null,
    val mereId: Long? = null,

    val statut: StatutPersonne = StatutPersonne.VIVANT,
    val situationMatrimoniale: SituationMatrimoniale = SituationMatrimoniale.CELIBATAIRE
)

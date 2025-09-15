package org.megamind.rdc_etat_civil.personne.dto

import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.personne.SituationMatrimoniale
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import java.time.LocalDate
import java.time.LocalTime

data class PersonneResponse(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val sexe: Sexe,
    val lieuNaiss: String?,
    val dateNaissance: LocalDate?,
    val heureNaissance: LocalTime?,
    val profession: String?,
    val nationalite: String?,
    val communeChefferie: String?,
    val quartierGroup: String?,
    val avenueVillage: String?,
    val celluleLocalite: String?,
    val telephone: String?,
    val email: String?,
    val pere: PersonneSimple?,
    val mere: PersonneSimple?,
    val statut: StatutPersonne,
    val situationMatrimoniale: SituationMatrimoniale,
    val age: Int? = null // Calculé automatiquement
) {
    companion object {
        fun fromEntity(personne: Personne): PersonneResponse {
            return PersonneResponse(
                id = personne.id,
                nom = personne.nom,
                postnom = personne.postnom,
                prenom = personne.prenom,
                sexe = personne.sexe,
                lieuNaiss = personne.lieuNaiss,
                dateNaissance = personne.dateNaissance,
                heureNaissance = personne.heureNaissance,
                profession = personne.profession,
                nationalite = personne.nationalite,
                communeChefferie = personne.communeChefferie,
                quartierGroup = personne.quartierGroup,
                avenueVillage = personne.avenueVillage,
                celluleLocalite = personne.celluleLocalite,
                telephone = personne.telephone,
                email = personne.email,
                pere = personne.pere?.let { PersonneSimple.fromEntity(it) },
                mere = personne.mere?.let { PersonneSimple.fromEntity(it) },
                statut = personne.statut,
                situationMatrimoniale = personne.situationMatrimoniale,
                age = personne.dateNaissance?.let { 
                    java.time.Period.between(it, LocalDate.now()).years 
                }
            )
        }
    }
}

/**
 * Version simplifiée pour éviter la récursion infinie dans les relations
 */
data class PersonneSimple(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val sexe: Sexe,
    val dateNaissance: LocalDate?,
    val statut: StatutPersonne
) {
    companion object {
        fun fromEntity(personne: Personne): PersonneSimple {
            return PersonneSimple(
                id = personne.id,
                nom = personne.nom,
                postnom = personne.postnom,
                prenom = personne.prenom,
                sexe = personne.sexe,
                dateNaissance = personne.dateNaissance,
                statut = personne.statut
            )
        }
    }
}

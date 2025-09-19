package org.megamind.rdc_etat_civil.naissance.dto

import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate
import java.time.LocalTime

/**
 * DTO complet pour la génération de PDF d'acte de naissance
 * Contient toutes les informations nécessaires pour créer un PDF officiel
 */
data class ActeNaissanceCompletDto(
    val id: Long,
    val numeroActe: String,
    val dateEnregistrement: LocalDate,
    val officier: String,
    val declarant: String?,
    val temoin1: String?,
    val temoin2: String?,
    
    // Informations de l'enfant
    val enfant: EnfantCompletDto,
    
    // Informations géographiques
    val commune: CommuneInfo,
    val entite: EntiteInfo,
    val province: ProvinceInfo
) {
    companion object {
        fun fromActeNaissanceResponse(response: ActeNaissanceResponse): ActeNaissanceCompletDto {
            return ActeNaissanceCompletDto(
                id = response.id,
                numeroActe = response.numeroActe,
                dateEnregistrement = response.dateEnregistrement,
                officier = response.officier,
                declarant = response.declarant,
                temoin1 = response.temoin1,
                temoin2 = response.temoin2,
                enfant = EnfantCompletDto.fromEnfantInfo(response.enfant),
                commune = response.commune,
                entite = response.entite,
                province = response.province
            )
        }
    }
}

data class EnfantCompletDto(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val sexe: Sexe,
    val dateNaissance: LocalDate?,
    val heureNaissance: LocalTime?,
    val lieuNaissance: String?,
    val pere: ParentCompletDto?,
    val mere: ParentCompletDto?
) {
    companion object {
        fun fromEnfantInfo(enfantInfo: EnfantInfo): EnfantCompletDto {
            return EnfantCompletDto(
                id = enfantInfo.id,
                nom = enfantInfo.nom,
                postnom = enfantInfo.postnom,
                prenom = enfantInfo.prenom,
                sexe = enfantInfo.sexe,
                dateNaissance = enfantInfo.dateNaissance,
                heureNaissance = enfantInfo.heureNaissance,
                lieuNaissance = enfantInfo.lieuNaissance,
                pere = enfantInfo.pere?.let { ParentCompletDto.fromParentInfo(it) },
                mere = enfantInfo.mere?.let { ParentCompletDto.fromParentInfo(it) }
            )
        }
    }
}

data class ParentCompletDto(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val profession: String?,
    val nationalite: String?,
    val dateNaissance: LocalDate?,
    val lieuNaissance: String?
) {
    companion object {
        fun fromParentInfo(parentInfo: ParentInfo): ParentCompletDto {
            return ParentCompletDto(
                id = parentInfo.id,
                nom = parentInfo.nom,
                postnom = parentInfo.postnom,
                prenom = parentInfo.prenom,
                profession = parentInfo.profession,
                nationalite = parentInfo.nationalite,
                dateNaissance = null, // À récupérer depuis la base de données si nécessaire
                lieuNaissance = null // À récupérer depuis la base de données si nécessaire
            )
        }
    }
}

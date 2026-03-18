package org.megamind.rdc_etat_civil.mariage.dto

import org.megamind.rdc_etat_civil.mariage.ActeMariage
import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province
import java.time.LocalDate

/**
 * DTO de réponse pour un acte de mariage complet
 */
data class ActeMariageResponse(
    val id: Long,
    val numeroActe: String,
    val epoux: PersonneMariageDto,
    val epouse: PersonneMariageDto,
    val commune: CommuneMariageDto,
    val dateMariage: LocalDate,
    val lieuMariage: String,
    val regimeMatrimonial: RegimeMatrimonial,
    val officier: String,
    val temoin1: String?,
    val temoin2: String?
) {
    companion object {
        fun fromEntity(acte: ActeMariage): ActeMariageResponse {
            return ActeMariageResponse(
                id = acte.id,
                numeroActe = acte.numeroActe,
                epoux = PersonneMariageDto.fromEntity(acte.epoux),
                epouse = PersonneMariageDto.fromEntity(acte.epouse),
                commune = CommuneMariageDto.fromEntity(acte.commune),
                dateMariage = acte.dateMariage,
                lieuMariage = acte.lieuMariage,
                regimeMatrimonial = acte.regimeMatrimonial,
                officier = acte.officier,
                temoin1 = acte.temoin1,
                temoin2 = acte.temoin2
            )
        }
    }
}

/**
 * DTO pour les informations d'une personne dans un acte de mariage
 */
data class PersonneMariageDto(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val sexe: Sexe,
    val dateNaissance: LocalDate?,
    val lieuNaissance: String?,
    val profession: String?,
    val nationalite: String?
) {
    companion object {
        fun fromEntity(personne: org.megamind.rdc_etat_civil.personne.Personne): PersonneMariageDto {
            return PersonneMariageDto(
                id = personne.id,
                nom = personne.nom,
                postnom = personne.postnom,
                prenom = personne.prenom,
                sexe = personne.sexe,
                dateNaissance = personne.dateNaissance,
                lieuNaissance = personne.lieuNaiss,
                profession = personne.profession,
                nationalite = personne.nationalite
            )
        }
    }
}

/**
 * DTO pour les informations d'une commune dans un acte de mariage
 */
data class CommuneMariageDto(
    val id: Long,
    val designation: String,
    val entite: EntiteMariageDto
) {
    companion object {
        fun fromEntity(commune: Commune): CommuneMariageDto {
            return CommuneMariageDto(
                id = commune.id!!,
                designation = commune.designation,
                entite = EntiteMariageDto.fromEntity(commune.entite)
            )
        }
    }
}

/**
 * DTO pour les informations d'une entité dans un acte de mariage
 */
data class EntiteMariageDto(
    val id: Long,
    val designation: String,
    val province: ProvinceMariageDto
) {
    companion object {
        fun fromEntity(entite: Entite): EntiteMariageDto {
            return EntiteMariageDto(
                id = entite.id!!,
                designation = entite.designation,
                province = ProvinceMariageDto.fromEntity(entite.province)
            )
        }
    }
}

/**
 * DTO pour les informations d'une province dans un acte de mariage
 */
data class ProvinceMariageDto(
    val id: Long,
    val designation: String
) {
    companion object {
        fun fromEntity(province: Province): ProvinceMariageDto {
            return ProvinceMariageDto(
                id = province.id!!,
                designation = province.designation
            )
        }
    }
}

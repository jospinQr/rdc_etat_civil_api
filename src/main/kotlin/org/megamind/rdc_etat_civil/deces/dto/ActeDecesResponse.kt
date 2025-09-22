package org.megamind.rdc_etat_civil.deces.dto

import org.megamind.rdc_etat_civil.deces.ActeDeces
import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate
import java.time.LocalTime

/**
 * DTO pour la réponse des actes de décès avec informations essentielles
 */
data class ActeDecesResponse(
    val id: Long,
    val numeroActe: String,
    val dateDeces: LocalDate,
    val heureDeces: LocalTime?,
    val lieuDeces: String,
    val causeDeces: String?,
    val dateEnregistrement: LocalDate,
    val officier: String,
    val declarant: String?,
    val temoin1: String?,
    val temoin2: String?,
    val medecin: String?,
    val observations: String?,
    
    // Informations du défunt
    val defunt: DefuntInfo,
    
    // Informations géographiques
    val commune: CommuneInfo,
    val entite: EntiteInfo,
    val province: ProvinceInfo
) {
    companion object {
        fun fromEntity(acte: ActeDeces): ActeDecesResponse {
            return ActeDecesResponse(
                id = acte.id,
                numeroActe = acte.numeroActe,
                dateDeces = acte.dateDeces,
                heureDeces = acte.heureDeces,
                lieuDeces = acte.lieuDeces,
                causeDeces = acte.causeDeces,
                dateEnregistrement = acte.dateEnregistrement,
                officier = acte.officier,
                declarant = acte.declarant,
                temoin1 = acte.temoin1,
                temoin2 = acte.temoin2,
                medecin = acte.medecin,
                observations = acte.observations,
                defunt = DefuntInfo.fromPersonne(acte.defunt),
                commune = CommuneInfo(
                    id = acte.commune.id ?: 0L,
                    nom = acte.commune.designation
                ),
                entite = EntiteInfo(
                    id = acte.commune.entite.id ?: 0L,
                    nom = acte.commune.entite.designation,
                    estVille = acte.commune.entite.estVille
                ),
                province = ProvinceInfo(
                    id = acte.commune.entite.province.id ?: 0L,
                    nom = acte.commune.entite.province.designation
                )
            )
        }
    }
}

data class DefuntInfo(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val sexe: Sexe,
    val dateNaissance: LocalDate?,
    val lieuNaissance: String?,
    val profession: String?,
    val nationalite: String?,
    val pere: ParentInfo?,
    val mere: ParentInfo?
) {
    companion object {
        fun fromPersonne(personne: org.megamind.rdc_etat_civil.personne.Personne): DefuntInfo {
            return DefuntInfo(
                id = personne.id,
                nom = personne.nom,
                postnom = personne.postnom,
                prenom = personne.prenom,
                sexe = personne.sexe,
                dateNaissance = personne.dateNaissance,
                lieuNaissance = personne.lieuNaiss,
                profession = personne.profession,
                nationalite = personne.nationalite,
                pere = personne.pere?.let { ParentInfo.fromPersonne(it) },
                mere = personne.mere?.let { ParentInfo.fromPersonne(it) }
            )
        }
    }
}

data class ParentInfo(
    val id: Long,
    val nom: String,
    val postnom: String,
    val prenom: String?,
    val profession: String?,
    val nationalite: String?
) {
    companion object {
        fun fromPersonne(personne: org.megamind.rdc_etat_civil.personne.Personne): ParentInfo {
            return ParentInfo(
                id = personne.id,
                nom = personne.nom,
                postnom = personne.postnom,
                prenom = personne.prenom,
                profession = personne.profession,
                nationalite = personne.nationalite
            )
        }
    }
}

data class CommuneInfo(
    val id: Long,
    val nom: String
)

data class EntiteInfo(
    val id: Long,
    val nom: String,
    val estVille: Boolean
)

data class ProvinceInfo(
    val id: Long,
    val nom: String
)

/**
 * DTO pour les statistiques par province
 */
data class StatistiqueProvinceResponse(
    val province: ProvinceInfo,
    val nombreActes: Long,
    val pourcentage: Double = 0.0
)

/**
 * DTO pour le résumé global des provinces
 */
data class ResumeProvincesResponse(
    val totalActes: Long,
    val nombreProvinces: Int,
    val provinces: List<StatistiqueProvinceResponse>
)


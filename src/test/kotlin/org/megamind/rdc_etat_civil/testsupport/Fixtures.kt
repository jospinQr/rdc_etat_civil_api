package org.megamind.rdc_etat_civil.testsupport

import org.megamind.rdc_etat_civil.deces.ActeDeces
import org.megamind.rdc_etat_civil.mariage.ActeMariage
import org.megamind.rdc_etat_civil.mariage.RegimeMatrimonial
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.personne.SituationMatrimoniale
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province
import java.time.LocalDate
import java.time.LocalTime

object Fixtures {
    fun province(id: Long = 1L, designation: String = "KINSHASA") =
        Province(id = id, designation = designation)

    fun entite(id: Long = 1L, designation: String = "GOMBE", estVille: Boolean = true, province: Province = province()) =
        Entite(id = id, designation = designation, estVille = estVille, province = province)

    fun commune(id: Long = 1L, designation: String = "GOMBE", entite: Entite = entite()) =
        Commune(id = id, designation = designation, entite = entite)

    fun personne(
        id: Long = 1L,
        nom: String = "KABAMBA",
        postnom: String = "MULUMBA",
        prenom: String? = "JEAN",
        sexe: Sexe = Sexe.MASCULIN,
        dateNaissance: LocalDate? = LocalDate.of(1990, 5, 12),
        lieuNaiss: String? = "KINSHASA",
        profession: String? = "INGENIEUR",
        nationalite: String? = "Congolaise",
        statut: StatutPersonne = StatutPersonne.VIVANT,
        situation: SituationMatrimoniale = SituationMatrimoniale.CELIBATAIRE,
        pere: Personne? = null,
        mere: Personne? = null
    ) = Personne(
        id = id,
        nom = nom,
        postnom = postnom,
        prenom = prenom,
        sexe = sexe,
        lieuNaiss = lieuNaiss,
        dateNaissance = dateNaissance,
        heureNaissance = LocalTime.of(6, 15),
        profession = profession,
        nationalite = nationalite,
        communeChefferie = "GOMBE",
        quartierGroup = "CENTRE",
        avenueVillage = "30 JUIN",
        celluleLocalite = "C1",
        telephone = "+243812345678",
        email = "jean.kabamba@example.com",
        pere = pere,
        mere = mere,
        statut = statut,
        situationMatrimoniale = situation
    )

    fun personneRequest(
        nom: String = " Kabamba ",
        postnom: String = " Mulumba ",
        prenom: String? = " Jean ",
        sexe: Sexe = Sexe.MASCULIN,
        pereId: Long? = null,
        mereId: Long? = null,
        dateNaissance: LocalDate? = LocalDate.of(1990, 5, 12),
    ) = PersonneRequest(
        nom = nom,
        postnom = postnom,
        prenom = prenom,
        sexe = sexe,
        lieuNaiss = "Kinshasa",
        dateNaissance = dateNaissance,
        heureNaissance = LocalTime.of(6, 15),
        profession = "Ingénieur",
        nationalite = "Congolaise",
        communeChefferie = "Gombe",
        quartierGroup = "Centre",
        avenueVillage = "30 Juin",
        celluleLocalite = "Cellule 1",
        telephone = "+243812345678",
        email = "Jean.Kabamba@Example.com",
        pereId = pereId,
        mereId = mereId,
        statut = StatutPersonne.VIVANT,
        situationMatrimoniale = SituationMatrimoniale.CELIBATAIRE
    )

    fun acteDeces(
        id: Long = 1L,
        numeroActe: String = "AD-2026-0001",
        defunt: Personne = personne(),
        commune: Commune = commune(),
        dateDeces: LocalDate = LocalDate.now().minusDays(5),
        dateEnregistrement: LocalDate = LocalDate.now()
    ) = ActeDeces(
        id = id,
        numeroActe = numeroActe,
        defunt = defunt,
        commune = commune,
        dateDeces = dateDeces,
        heureDeces = LocalTime.of(14, 30),
        lieuDeces = "Hôpital Général",
        causeDeces = "Maladie",
        declarant = "DECLARANT",
        officier = "OEC MUKENDI",
        dateEnregistrement = dateEnregistrement,
        temoin1 = "TEMOIN 1",
        temoin2 = "TEMOIN 2",
        medecin = "Dr. LUMUMBA",
        observations = "RAS"
    )

    fun acteMariage(
        id: Long = 1L,
        numeroActe: String = "AM-2026-0001",
        epoux: Personne = personne(id = 10L, sexe = Sexe.MASCULIN),
        epouse: Personne = personne(id = 11L, sexe = Sexe.FEMININ, prenom = "ALINE"),
        commune: Commune = commune(),
        dateMariage: LocalDate = LocalDate.now().minusDays(10)
    ) = ActeMariage(
        id = id,
        numeroActe = numeroActe,
        epoux = epoux,
        epouse = epouse,
        commune = commune,
        dateMariage = dateMariage,
        lieuMariage = "Commune de Gombe",
        regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
        officier = "OEC MUKENDI",
        temoin1 = "TEMOIN 1",
        temoin2 = "TEMOIN 2"
    )
}


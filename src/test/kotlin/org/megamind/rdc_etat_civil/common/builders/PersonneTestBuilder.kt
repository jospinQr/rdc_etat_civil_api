package org.megamind.rdc_etat_civil.common.builders

import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.personne.SituationMatrimoniale
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import java.time.LocalDate
import java.time.LocalTime

/**
 * Builder pour créer des objets Personne de test
 */
class PersonneTestBuilder {
    private var id: Long = 0L
    private var nom: String = "Dupont"
    private var postnom: String = "Jean"
    private var prenom: String? = "Pierre"
    private var dateNaissance: LocalDate? = LocalDate.of(1990, 1, 1)
    private var lieuNaiss: String? = "Kinshasa"
    private var sexe: Sexe = Sexe.MASCULIN
    private var profession: String? = "Ingénieur"
    private var nationalite: String? = "Congolaise"
    private var communeChefferie: String? = "Lemba"
    private var quartierGroup: String? = "Quartier 1"
    private var avenueVillage: String? = "Avenue de la Paix"
    private var celluleLocalite: String? = "Cellule A"
    private var telephone: String? = "+243123456789"
    private var email: String? = "test@example.com"
    private var heureNaissance: LocalTime? = null
    private var pere: Personne? = null
    private var mere: Personne? = null
    private var statut: StatutPersonne = StatutPersonne.VIVANT
    private var situationMatrimoniale: SituationMatrimoniale = SituationMatrimoniale.CELIBATAIRE

    fun withId(id: Long) = apply { this.id = id }
    fun withNom(nom: String) = apply { this.nom = nom }
    fun withPostnom(postnom: String) = apply { this.postnom = postnom }
    fun withPrenom(prenom: String?) = apply { this.prenom = prenom }
    fun withDateNaissance(dateNaissance: LocalDate?) = apply { this.dateNaissance = dateNaissance }
    fun withLieuNaiss(lieuNaiss: String?) = apply { this.lieuNaiss = lieuNaiss }
    fun withSexe(sexe: Sexe) = apply { this.sexe = sexe }
    fun withProfession(profession: String?) = apply { this.profession = profession }
    fun withNationalite(nationalite: String?) = apply { this.nationalite = nationalite }
    fun withCommuneChefferie(communeChefferie: String?) = apply { this.communeChefferie = communeChefferie }
    fun withQuartierGroup(quartierGroup: String?) = apply { this.quartierGroup = quartierGroup }
    fun withAvenueVillage(avenueVillage: String?) = apply { this.avenueVillage = avenueVillage }
    fun withCelluleLocalite(celluleLocalite: String?) = apply { this.celluleLocalite = celluleLocalite }
    fun withTelephone(telephone: String?) = apply { this.telephone = telephone }
    fun withEmail(email: String?) = apply { this.email = email }
    fun withHeureNaissance(heureNaissance: LocalTime?) = apply { this.heureNaissance = heureNaissance }
    fun withPere(pere: Personne?) = apply { this.pere = pere }
    fun withMere(mere: Personne?) = apply { this.mere = mere }
    fun withStatut(statut: StatutPersonne) = apply { this.statut = statut }
    fun withSituationMatrimoniale(situationMatrimoniale: SituationMatrimoniale) = apply { this.situationMatrimoniale = situationMatrimoniale }

    fun build(): Personne {
        return Personne(
            id = this@PersonneTestBuilder.id,
            nom = this@PersonneTestBuilder.nom,
            postnom = this@PersonneTestBuilder.postnom,
            prenom = this@PersonneTestBuilder.prenom,
            sexe = this@PersonneTestBuilder.sexe,
            lieuNaiss = this@PersonneTestBuilder.lieuNaiss,
            dateNaissance = this@PersonneTestBuilder.dateNaissance,
            heureNaissance = this@PersonneTestBuilder.heureNaissance,
            profession = this@PersonneTestBuilder.profession,
            nationalite = this@PersonneTestBuilder.nationalite,
            communeChefferie = this@PersonneTestBuilder.communeChefferie,
            quartierGroup = this@PersonneTestBuilder.quartierGroup,
            avenueVillage = this@PersonneTestBuilder.avenueVillage,
            celluleLocalite = this@PersonneTestBuilder.celluleLocalite,
            telephone = this@PersonneTestBuilder.telephone,
            email = this@PersonneTestBuilder.email,
            pere = this@PersonneTestBuilder.pere,
            mere = this@PersonneTestBuilder.mere,
            statut = this@PersonneTestBuilder.statut,
            situationMatrimoniale = this@PersonneTestBuilder.situationMatrimoniale
        )
    }

    fun buildRequest(): PersonneRequest {
        return PersonneRequest(
            nom = nom,
            postnom = postnom,
            prenom = prenom,
            sexe = sexe,
            lieuNaiss = lieuNaiss,
            dateNaissance = dateNaissance,
            heureNaissance = heureNaissance,
            profession = profession,
            nationalite = nationalite,
            communeChefferie = communeChefferie,
            quartierGroup = quartierGroup,
            avenueVillage = avenueVillage,
            celluleLocalite = celluleLocalite,
            telephone = telephone,
            email = email,
            pereId = pere?.id,
            mereId = mere?.id,
            statut = statut,
            situationMatrimoniale = situationMatrimoniale
        )
    }

    companion object {
        fun create() = PersonneTestBuilder()
        fun createDefault() = create().build()
        fun createDefaultRequest() = create().buildRequest()
    }
}

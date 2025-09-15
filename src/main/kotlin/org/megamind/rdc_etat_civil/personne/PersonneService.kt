package org.megamind.rdc_etat_civil.personne

import org.megamind.rdc_etat_civil.personne.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class PersonneService(
    private val personneRepository: PersonneRepository
) {

    // ====== OPÉRATIONS CRUD ======

    /**
     * Créer une nouvelle personne
     */
    fun creerPersonne(request: PersonneRequest): PersonneResponse {
        // Vérification des doublons
        if (request.dateNaissance != null) {
            val existeDeja = personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(
                request.nom,
                request.postnom,
                request.prenom ?: "",
                request.dateNaissance
            )
            if (existeDeja) {
                throw IllegalArgumentException(
                    "Une personne avec ce nom complet et cette date de naissance existe déjà"
                )
            }
        }

        // Vérification des parents si spécifiés
        val pere = request.pereId?.let { 
            personneRepository.findById(it).orElseThrow { 
                IllegalArgumentException("Père introuvable avec l'ID: $it") 
            }
        }
        val mere = request.mereId?.let { 
            personneRepository.findById(it).orElseThrow { 
                IllegalArgumentException("Mère introuvable avec l'ID: $it") 
            }
        }

        // Validation logique des parents
        pere?.let { 
            if (it.sexe != Sexe.MASCULIN) {
                throw IllegalArgumentException("Le père doit être de sexe masculin")
            }
        }
        mere?.let { 
            if (it.sexe != Sexe.FEMININ) {
                throw IllegalArgumentException("La mère doit être de sexe féminin")
            }
        }

        val personne = Personne(
            nom = request.nom.trim().uppercase(),
            postnom = request.postnom.trim().uppercase(),
            prenom = request.prenom?.trim()?.uppercase(),
            sexe = request.sexe,
            lieuNaiss = request.lieuNaiss?.trim(),
            dateNaissance = request.dateNaissance,
            heureNaissance = request.heureNaissance,
            profession = request.profession?.trim(),
            nationalite = request.nationalite?.trim() ?: "Congolaise",
            communeChefferie = request.communeChefferie?.trim(),
            quartierGroup = request.quartierGroup?.trim(),
            avenueVillage = request.avenueVillage?.trim(),
            celluleLocalite = request.celluleLocalite?.trim(),
            telephone = request.telephone?.trim(),
            email = request.email?.trim()?.lowercase(),
            pere = pere,
            mere = mere,
            statut = request.statut,
            situationMatrimoniale = request.situationMatrimoniale
        )

        val personneSauvee = personneRepository.save(personne)
        return PersonneResponse.fromEntity(personneSauvee)
    }

    /**
     * Créer plusieurs personnes en une seule fois
     */
    fun creerPersonnesEnLot(request: PersonneBatchRequest): PersonneBatchResponse {
        val personnesCreees = mutableListOf<PersonneResponse>()
        val echecs = mutableListOf<PersonneEchecInfo>()

        request.personnes.forEachIndexed { index, personneRequest ->
            try {
                val personneCreee = creerPersonne(personneRequest)
                personnesCreees.add(personneCreee)
            } catch (e: Exception) {
                echecs.add(
                    PersonneEchecInfo(
                        index = index,
                        personne = personneRequest,
                        erreur = e.message ?: "Erreur inconnue"
                    )
                )
            }
        }

        return PersonneBatchResponse(
            totalDemandees = request.personnes.size,
            totalCreees = personnesCreees.size,
            totalEchecs = echecs.size,
            personnesCreees = personnesCreees,
            echecs = echecs
        )
    }

    /**
     * Modifier une personne existante
     */
    fun modifierPersonne(id: Long, request: PersonneRequest): PersonneResponse {
        val personneExistante = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }

        // Vérification des doublons (exclure la personne actuelle)
        if (request.dateNaissance != null) {
            val doublonExiste = personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(
                request.nom,
                request.postnom,
                request.prenom ?: "",
                request.dateNaissance
            )
            if (doublonExiste != null && doublonExiste.id != id) {
                throw IllegalArgumentException(
                    "Une autre personne avec ce nom complet et cette date de naissance existe déjà"
                )
            }
        }

        // Vérification des parents
        val pere = request.pereId?.let { pereId ->
            if (pereId == id) throw IllegalArgumentException("Une personne ne peut pas être son propre père")
            personneRepository.findById(pereId).orElseThrow { 
                IllegalArgumentException("Père introuvable avec l'ID: $pereId") 
            }
        }
        val mere = request.mereId?.let { mereId ->
            if (mereId == id) throw IllegalArgumentException("Une personne ne peut pas être sa propre mère")
            personneRepository.findById(mereId).orElseThrow { 
                IllegalArgumentException("Mère introuvable avec l'ID: $mereId") 
            }
        }

        val personneModifiee = personneExistante.copy(
            nom = request.nom.trim().uppercase(),
            postnom = request.postnom.trim().uppercase(),
            prenom = request.prenom?.trim()?.uppercase(),
            sexe = request.sexe,
            lieuNaiss = request.lieuNaiss?.trim(),
            dateNaissance = request.dateNaissance,
            heureNaissance = request.heureNaissance,
            profession = request.profession?.trim(),
            nationalite = request.nationalite?.trim() ?: "Congolaise",
            communeChefferie = request.communeChefferie?.trim(),
            quartierGroup = request.quartierGroup?.trim(),
            avenueVillage = request.avenueVillage?.trim(),
            celluleLocalite = request.celluleLocalite?.trim(),
            telephone = request.telephone?.trim(),
            email = request.email?.trim()?.lowercase(),
            pere = pere,
            mere = mere,
            statut = request.statut,
            situationMatrimoniale = request.situationMatrimoniale
        )

        val personneSauvee = personneRepository.save(personneModifiee)
        return PersonneResponse.fromEntity(personneSauvee)
    }
  
    /**
     * Supprimer une personne
     */
    fun supprimerPersonne(id: Long) {
        val personne = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }

        // Vérifier si la personne a des enfants
        val nombreEnfants = personneRepository.findByPere(personne, PageRequest.of(0, 1)).totalElements +
                           personneRepository.findByMere(personne, PageRequest.of(0, 1)).totalElements

        if (nombreEnfants > 0) {
            throw IllegalStateException(
                "Impossible de supprimer cette personne car elle a des enfants enregistrés"
            )
        }

        personneRepository.deleteById(id)
    }

    /**
     * Obtenir une personne par ID
     */
    @Transactional(readOnly = true)
    fun obtenirPersonne(id: Long): PersonneResponse {
        val personne = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }
        return PersonneResponse.fromEntity(personne)
    }

    // ====== RECHERCHES ======

    /**
     * Lister toutes les personnes avec pagination
     */
    @Transactional(readOnly = true)
    fun listerPersonnes(page: Int = 0, size: Int = 20): Page<PersonneResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("nom", "postnom", "prenom"))
        return personneRepository.findAll(pageable).map { PersonneResponse.fromEntity(it) }
    }

    /**
     * Recherche globale par nom
     */
    @Transactional(readOnly = true)
    fun rechercherParNom(terme: String, page: Int = 0, size: Int = 20): Page<PersonneResponse> {
        val pageable = PageRequest.of(page, size)
        return personneRepository.rechercherParNom(terme.trim(), pageable)
            .map { PersonneResponse.fromEntity(it) }
    }

    /**
     * Recherche multicritères avancée
     */
    @Transactional(readOnly = true)
    fun rechercherPersonnes(criteria: PersonneSearchCriteria): Page<PersonneResponse> {
        val sort = if (criteria.sortDirection.uppercase() == "DESC") {
            Sort.by(criteria.sortBy).descending()
        } else {
            Sort.by(criteria.sortBy).ascending()
        }
        
        val pageable = PageRequest.of(criteria.page, criteria.size, sort)
        
        // Combiner les critères de date et d'âge
        val dateDebut = criteria.getFinalDateDebut()
        val dateFin = criteria.getFinalDateFin()

        return personneRepository.rechercheMulticriteres(
            nom = criteria.nom?.trim(),
            postnom = criteria.postnom?.trim(),
            prenom = criteria.prenom?.trim(),
            sexe = criteria.sexe,
            statut = criteria.statut,
            commune = criteria.commune?.trim(),
            dateDebut = dateDebut,
            dateFin = dateFin,
            pageable = pageable
        ).map { PersonneResponse.fromEntity(it) }
    }

    /**
     * Rechercher les enfants d'une personne
     */
    @Transactional(readOnly = true)
    fun obtenirEnfants(parentId: Long, page: Int = 0, size: Int = 20): Page<PersonneResponse> {
        val parent = personneRepository.findById(parentId).orElseThrow {
            IllegalArgumentException("Parent introuvable avec l'ID: $parentId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateNaissance").descending())
        
        return when (parent.sexe) {
            Sexe.MASCULIN -> personneRepository.findByPere(parent, pageable)
            Sexe.FEMININ -> personneRepository.findByMere(parent, pageable)
        }.map { PersonneResponse.fromEntity(it) }
    }

    // ====== STATISTIQUES ======

    /**
     * Statistiques générales
     */
    @Transactional(readOnly = true)
    fun obtenirStatistiquesGenerales(): Map<String, Any> {
        val totalPersonnes = personneRepository.count()
        val hommes = personneRepository.countBySexe(Sexe.MASCULIN)
        val femmes = personneRepository.countBySexe(Sexe.FEMININ)
        val vivants = personneRepository.countByStatut(StatutPersonne.VIVANT)
        val decedes = personneRepository.countByStatut(StatutPersonne.DECEDE)
        
        val dateLimiteMineur = LocalDate.now().minusYears(18)
        val mineurs = personneRepository.countMineurs(dateLimiteMineur)
        val majeurs = personneRepository.countMajeurs(dateLimiteMineur)

        return mapOf(
            "totalPersonnes" to totalPersonnes,
            "repartitionSexe" to mapOf(
                "hommes" to hommes,
                "femmes" to femmes
            ),
            "repartitionStatut" to mapOf(
                "vivants" to vivants,
                "decedes" to decedes
            ),
            "repartitionAge" to mapOf(
                "mineurs" to mineurs,
                "majeurs" to majeurs
            )
        )
    }

    /**
     * Statistiques par commune
     */
    @Transactional(readOnly = true)
    fun obtenirStatistiquesParCommune(): List<Map<String, Any>> {
        return personneRepository.statistiquesParCommune().map { row ->
            mapOf(
                "commune" to (row[0] as String),
                "nombre" to (row[1] as Long)
            )
        }
    }

    /**
     * Statistiques par sexe
     */
    @Transactional(readOnly = true)
    fun obtenirStatistiquesParSexe(): List<Map<String, Any>> {
        return personneRepository.statistiquesParSexe().map { row ->
            mapOf(
                "sexe" to (row[0] as Sexe),
                "nombre" to (row[1] as Long)
            )
        }
    }

    // ====== MÉTHODES UTILITAIRES ======

    /**
     * Vérifier si une personne existe
     */
    @Transactional(readOnly = true)
    fun personneExiste(id: Long): Boolean {
        return personneRepository.existsById(id)
    }

    /**
     * Obtenir une version simplifiée d'une personne
     */
    @Transactional(readOnly = true)
    fun obtenirPersonneSimple(id: Long): PersonneSimple {
        val personne = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }
        return PersonneSimple.fromEntity(personne)
    }

    /**
     * Changer le statut d'une personne (vivant -> décédé)
     */
    fun changerStatut(id: Long, nouveauStatut: StatutPersonne): PersonneResponse {
        val personne = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }

        val personneModifiee = personne.copy(statut = nouveauStatut)
        val personneSauvee = personneRepository.save(personneModifiee)
        return PersonneResponse.fromEntity(personneSauvee)
    }

    /**
     * Mettre à jour la situation matrimoniale
     */
    fun changerSituationMatrimoniale(id: Long, nouvelleSituation: SituationMatrimoniale): PersonneResponse {
        val personne = personneRepository.findById(id).orElseThrow {
            IllegalArgumentException("Personne introuvable avec l'ID: $id")
        }

        val personneModifiee = personne.copy(situationMatrimoniale = nouvelleSituation)
        val personneSauvee = personneRepository.save(personneModifiee)
        return PersonneResponse.fromEntity(personneSauvee)
    }
}

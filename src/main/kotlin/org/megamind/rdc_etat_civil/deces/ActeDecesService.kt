package org.megamind.rdc_etat_civil.deces

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.deces.dto.*
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Service pour la gestion des actes de décès
 * 
 * Ce service gère toutes les opérations liées aux actes de décès :
 * - Création, modification, suppression
 * - Recherches multicritères avancées
 * - Validations métier spécifiques au contexte RDC
 * - Traitement par lot pour l'administration
 * - Statistiques et rapports
 */
@Service
@Transactional
class ActeDecesService(
    private val acteDecesRepository: ActeDecesRepository,
    private val personneRepository: PersonneRepository,
    private val communeRepository: CommuneRepository,
    private val entiteRepository: EntiteRepository,
    private val provinceRepository: ProvinceRepository,
    private val personneService: org.megamind.rdc_etat_civil.personne.PersonneService
) {

    // ====== OPÉRATIONS CRUD ======

    /**
     * Créer un nouvel acte de décès avec toutes les validations
     */
    fun creerActeDeces(request: ActeDecesRequest): ActeDecesResponse {
        // 1. Validation du numéro d'acte (unicité)
        if (acteDecesRepository.existsByNumeroActe(request.numeroActe)) {
            throw BadRequestException("Le numéro d'acte '${request.numeroActe}' existe déjà")
        }

        // 2. Vérification du défunt
        val defunt = personneRepository.findById(request.defuntId).orElseThrow {
            EntityNotFoundException("Défunt introuvable avec l'ID: ${request.defuntId}")
        }

        // 3. Vérification que le défunt n'a pas déjà un acte de décès
        if (acteDecesRepository.existsByDefunt(defunt)) {
            throw BadRequestException("Ce défunt a déjà un acte de décès enregistré")
        }

        // 4. Vérification de la commune
        val commune = communeRepository.findById(request.communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: ${request.communeId}")
        }

        // 5. Validations métier spécifiques
        validerDonneesActe(request, defunt)

        // 6. Création de l'acte
        val acte = ActeDeces(
            numeroActe = request.numeroActe.trim().uppercase(),
            defunt = defunt,
            commune = commune,
            dateDeces = request.dateDeces,
            heureDeces = request.heureDeces,
            lieuDeces = request.lieuDeces.trim(),
            causeDeces = request.causeDeces?.trim(),
            officier = request.officier.trim(),
            declarant = request.declarant?.trim(),
            dateEnregistrement = request.dateEnregistrement,
            temoin1 = request.temoin1?.trim(),
            temoin2 = request.temoin2?.trim(),
            medecin = request.medecin?.trim(),
            observations = request.observations?.trim()
        )

        val acteSauve = acteDecesRepository.save(acte)
        
        // Mise à jour automatique du statut de la personne en DÉCÉDÉ
        try {
            personneService.changerStatut(request.defuntId, org.megamind.rdc_etat_civil.personne.StatutPersonne.DECEDE)
        } catch (e: Exception) {
            // Log l'erreur mais ne fait pas échouer la création de l'acte
            // car l'acte de décès est plus important que le statut
            println("Attention: Impossible de mettre à jour le statut de la personne ${request.defuntId}: ${e.message}")
        }
        
        return ActeDecesResponse.fromEntity(acteSauve)
    }

    /**
     * Obtenir un acte de décès par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActeDeces(id: Long): ActeDecesResponse {
        val acte = acteDecesRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de décès introuvable avec l'ID: $id")
        }
        return ActeDecesResponse.fromEntity(acte)
    }

    /**
     * Obtenir une version simplifiée d'un acte
     */
    @Transactional(readOnly = true)
    fun obtenirActeSimple(id: Long): ActeDecesSimple {
        val acte = acteDecesRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de décès introuvable avec l'ID: $id")
        }
        return ActeDecesSimple.fromEntity(acte)
    }

    /**
     * Modifier un acte de décès existant
     */
    fun modifierActeDeces(id: Long, request: ActeDecesUpdateRequest): ActeDecesResponse {
        val acteExistant = acteDecesRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de décès introuvable avec l'ID: $id")
        }

        // Validation du nouveau numéro d'acte si modifié
        request.numeroActe?.let { nouveauNumero ->
            if (nouveauNumero != acteExistant.numeroActe && 
                acteDecesRepository.existsByNumeroActe(nouveauNumero)) {
                throw BadRequestException("Le numéro d'acte '$nouveauNumero' existe déjà")
            }
        }

        // Validation de la nouvelle commune si modifiée
        val nouvelleCommune = request.communeId?.let { communeId ->
            communeRepository.findById(communeId).orElseThrow {
                EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
            }
        }

        // Création de l'acte modifié
        val acteModifie = acteExistant.copy(
            numeroActe = request.numeroActe?.trim()?.uppercase() ?: acteExistant.numeroActe,
            commune = nouvelleCommune ?: acteExistant.commune,
            dateDeces = request.dateDeces ?: acteExistant.dateDeces,
            heureDeces = request.heureDeces,
            lieuDeces = request.lieuDeces?.trim() ?: acteExistant.lieuDeces,
            causeDeces = request.causeDeces?.trim(),
            officier = request.officier?.trim() ?: acteExistant.officier,
            declarant = request.declarant?.trim(),
            dateEnregistrement = request.dateEnregistrement ?: acteExistant.dateEnregistrement,
            temoin1 = request.temoin1?.trim(),
            temoin2 = request.temoin2?.trim(),
            medecin = request.medecin?.trim(),
            observations = request.observations?.trim()
        )

        val acteSauve = acteDecesRepository.save(acteModifie)
        return ActeDecesResponse.fromEntity(acteSauve)
    }

    /**
     * Supprimer un acte de décès
     */
    fun supprimerActeDeces(id: Long) {
        val acte = acteDecesRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de décès introuvable avec l'ID: $id")
        }
        
        // Récupérer l'ID du défunt avant suppression
        val defuntId = acte.defunt.id
        
        // Supprimer l'acte
        acteDecesRepository.deleteById(id)
        
        // Restaurer le statut de la personne en VIVANT
        try {
            personneService.changerStatut(defuntId, org.megamind.rdc_etat_civil.personne.StatutPersonne.VIVANT)
        } catch (e: Exception) {
            // Log l'erreur mais ne fait pas échouer la suppression de l'acte
            println("Attention: Impossible de restaurer le statut de la personne $defuntId: ${e.message}")
        }
    }

    // ====== RECHERCHES ======

    /**
     * Lister tous les actes avec pagination
     */
    @Transactional(readOnly = true)
    fun listerActesDeces(page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findAll(pageable).map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Rechercher par numéro d'acte
     */
    @Transactional(readOnly = true)
    fun rechercherParNumeroActe(numeroActe: String): ActeDecesResponse? {
        val acte = acteDecesRepository.findByNumeroActe(numeroActe.trim().uppercase())
        return acte?.let { ActeDecesResponse.fromEntity(it) }
    }

    /**
     * Rechercher par nom du défunt
     */
    @Transactional(readOnly = true)
    fun rechercherParNomDefunt(terme: String, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        val pageable = PageRequest.of(page, size)
        return acteDecesRepository.rechercherParNomDefunt(terme.trim(), pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir l'acte d'un défunt spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActeParDefunt(defuntId: Long): ActeDecesResponse? {
        val defunt = personneRepository.findById(defuntId).orElseThrow {
            EntityNotFoundException("Défunt introuvable avec l'ID: $defuntId")
        }
        val acte = acteDecesRepository.findByDefunt(defunt)
        return acte?.let { ActeDecesResponse.fromEntity(it) }
    }

    /**
     * Obtenir les actes d'une commune
     */
    @Transactional(readOnly = true)
    fun obtenirActesParCommune(communeId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        val commune = communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findByCommune(commune, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir le nombre total d'actes d'une commune
     */
    @Transactional(readOnly = true)
    fun compterActesParCommune(communeId: Long): Long {
        val commune = communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        
        return acteDecesRepository.countByCommune(commune)
    }

    /**
     * Obtenir tous les actes d'une province par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActesParProvince(provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findByProvinceId(provinceId, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir le nombre total d'actes d'une province
     */
    @Transactional(readOnly = true)
    fun compterActesParProvince(provinceId: Long): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteDecesRepository.countByProvinceId(provinceId)
    }

    /**
     * Obtenir tous les actes d'une entité par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActesParEntite(entiteId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        // Vérifier que l'entité existe
        entiteRepository.findById(entiteId).orElseThrow {
            EntityNotFoundException("Entité introuvable avec l'ID: $entiteId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findByEntiteId(entiteId, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir le nombre total d'actes d'une entité
     */
    @Transactional(readOnly = true)
    fun compterActesParEntite(entiteId: Long): Long {
        // Vérifier que l'entité existe
        entiteRepository.findById(entiteId).orElseThrow {
            EntityNotFoundException("Entité introuvable avec l'ID: $entiteId")
        }
        
        return acteDecesRepository.countByEntiteId(entiteId)
    }

    /**
     * Recherche multicritères avancée
     */
    @Transactional(readOnly = true)
    fun rechercherActes(criteria: ActeDecesSearchCriteria): Page<ActeDecesSimple> {
        val pageable = PageRequest.of(
            criteria.page, 
            criteria.size, 
            Sort.by(
                if (criteria.sortDirection.uppercase() == "ASC") 
                    Sort.Direction.ASC 
                else 
                    Sort.Direction.DESC,
                criteria.sortBy
            )
        )

        return acteDecesRepository.rechercheMulticriteres(
            numeroActe = criteria.numeroActe,
            nomDefunt = criteria.nomDefunt,
            postnomDefunt = criteria.postnomDefunt,
            prenomDefunt = criteria.prenomDefunt,
            officier = criteria.officier,
            declarant = criteria.declarant,
            medecin = criteria.medecin,
            dateDebutDeces = criteria.dateDecesDebut,
            dateFinDeces = criteria.dateDecesFin,
            dateDebutEnreg = criteria.dateEnregistrementDebut,
            dateFinEnreg = criteria.dateEnregistrementFin,
            pageable = pageable
        ).map { ActeDecesSimple.fromEntity(it) }
    }

    // ====== RECHERCHES PAR SEXE ======

    /**
     * Obtenir tous les actes de défunts d'un sexe donné
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexe(sexe: Sexe, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findBySexeDefunt(sexe, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes de défunts d'un sexe donné dans une province spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEtProvince(sexe: Sexe, provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findBySexeDefuntAndProvinceId(sexe, provinceId, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes de défunts d'un sexe donné dans une entité spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEtEntite(sexe: Sexe, entiteId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        // Vérifier que l'entité existe
        entiteRepository.findById(entiteId).orElseThrow {
            EntityNotFoundException("Entité introuvable avec l'ID: $entiteId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findBySexeDefuntAndEntiteId(sexe, entiteId, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes de défunts d'un sexe donné dans une commune spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEtCommune(sexe: Sexe, communeId: Long, page: Int = 0, size: Int = 20): Page<ActeDecesSimple> {
        // Vérifier que la commune existe
        communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending())
        return acteDecesRepository.findBySexeDefuntAndCommuneId(sexe, communeId, pageable)
            .map { ActeDecesSimple.fromEntity(it) }
    }

    /**
     * Compter les actes par sexe
     */
    @Transactional(readOnly = true)
    fun compterActesParSexe(sexe: Sexe): Long {
        return acteDecesRepository.countBySexeDefunt(sexe)
    }

    /**
     * Compter les actes par sexe dans une province
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEtProvince(sexe: Sexe, provinceId: Long): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteDecesRepository.countBySexeDefuntAndProvinceId(sexe, provinceId)
    }

    /**
     * Compter les actes par sexe dans une entité
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEtEntite(sexe: Sexe, entiteId: Long): Long {
        // Vérifier que l'entité existe
        entiteRepository.findById(entiteId).orElseThrow {
            EntityNotFoundException("Entité introuvable avec l'ID: $entiteId")
        }
        
        return acteDecesRepository.countBySexeDefuntAndEntiteId(sexe, entiteId)
    }

    /**
     * Compter les actes par sexe dans une commune
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEtCommune(sexe: Sexe, communeId: Long): Long {
        // Vérifier que la commune existe
        communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        
        return acteDecesRepository.countBySexeDefuntAndCommuneId(sexe, communeId)
    }

    // ====== VALIDATIONS ======

    /**
     * Vérifier si un numéro d'acte existe
     */
    @Transactional(readOnly = true)
    fun verifierNumeroActe(numeroActe: String): Boolean {
        return acteDecesRepository.existsByNumeroActe(numeroActe.trim().uppercase())
    }

    /**
     * Vérifier si un défunt a déjà un acte
     */
    @Transactional(readOnly = true)
    fun verifierDefuntAActe(defuntId: Long): Boolean {
        val defunt = personneRepository.findById(defuntId).orElseThrow {
            EntityNotFoundException("Défunt introuvable avec l'ID: $defuntId")
        }
        return acteDecesRepository.existsByDefunt(defunt)
    }

    // ====== TRAITEMENT PAR LOT ======

    /**
     * Créer plusieurs actes de décès en une seule fois
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun creerActesEnLot(request: ActeDecesBatchRequest): ActeDecesBatchResponse {
        val tempsDebut = System.currentTimeMillis()
        val resultats = mutableListOf<ActeDecesBatchItemResponse>()
        var actesReussis = 0
        var actesEchecs = 0

        request.actes.forEachIndexed { index, acteRequest ->
            try {
                // Transformation vers ActeDecesRequest standard
                val requestStandard = ActeDecesRequest(
                    numeroActe = acteRequest.numeroActe,
                    defuntId = acteRequest.defuntId,
                    communeId = acteRequest.communeId,
                    dateDeces = acteRequest.dateDeces,
                    heureDeces = acteRequest.heureDeces,
                    lieuDeces = acteRequest.lieuDeces,
                    causeDeces = acteRequest.causeDeces,
                    officier = acteRequest.officier,
                    declarant = acteRequest.declarant,
                    dateEnregistrement = acteRequest.dateEnregistrement,
                    temoin1 = acteRequest.temoin1,
                    temoin2 = acteRequest.temoin2,
                    medecin = acteRequest.medecin,
                    observations = acteRequest.observations
                )

                val acteCreee = creerActeDeces(requestStandard)
                
                // La mise à jour du statut est déjà gérée dans creerActeDeces()
                // donc pas besoin de la refaire ici
                
                resultats.add(
                    ActeDecesBatchItemResponse(
                        numeroActe = acteRequest.numeroActe,
                        defuntId = acteRequest.defuntId,
                        success = true,
                        acteId = acteCreee.id,
                        numeroOrdre = acteRequest.numeroOrdre ?: (index + 1),
                        reference = acteRequest.reference
                    )
                )
                actesReussis++
            } catch (e: Exception) {
                resultats.add(
                    ActeDecesBatchItemResponse(
                        numeroActe = acteRequest.numeroActe,
                        defuntId = acteRequest.defuntId,
                        success = false,
                        erreur = e.message ?: "Erreur inconnue",
                        numeroOrdre = acteRequest.numeroOrdre ?: (index + 1),
                        reference = acteRequest.reference
                    )
                )
                actesEchecs++
            }
        }

        val tempsTraitement = System.currentTimeMillis() - tempsDebut
        val statistiques = genererStatistiquesBatch(resultats, request.actes)

        return ActeDecesBatchResponse(
            success = actesEchecs == 0,
            message = if (actesEchecs == 0) {
                "Tous les actes ont été traités avec succès"
            } else {
                "$actesReussis actes créés, $actesEchecs échecs"
            },
            totalActes = request.actes.size,
            actesTraites = request.actes.size,
            actesReussis = actesReussis,
            actesEchecs = actesEchecs,
            tempsTraitement = tempsTraitement,
            resultats = resultats,
            statistiques = statistiques
        )
    }

    // ====== STATISTIQUES ======

    /**
     * Obtenir les statistiques générales
     */
    @Transactional(readOnly = true)
    fun obtenirStatistiques(): ActeDecesStatistiques {
        val total = acteDecesRepository.count()
        val aujourd = acteDecesRepository.countByDateEnregistrement(LocalDate.now())
        
        val debutMois = LocalDate.now().withDayOfMonth(1)
        val finMois = LocalDate.now()
        val ceMois = acteDecesRepository.countByDateEnregistrementBetween(debutMois, finMois)
        
        val enregistrementTardif = acteDecesRepository.countActesEnregistrementTardif()
        
        val parCommune = acteDecesRepository.statistiquesParCommune().map { array ->
            mapOf("commune" to array[0], "nombre" to array[1])
        }
        
        val parOfficier = acteDecesRepository.statistiquesParOfficier().map { array ->
            mapOf("officier" to array[0], "nombre" to array[1])
        }
        
        val parMois = acteDecesRepository.statistiquesParMoisEnregistrement(
            LocalDate.now().minusMonths(12)
        ).map { array ->
            mapOf(
                "annee" to array[0], 
                "mois" to array[1], 
                "nombre" to array[2]
            )
        }

        val parCause = acteDecesRepository.statistiquesParCauseDeces().map { array ->
            mapOf("cause" to array[0], "nombre" to array[1])
        }

        val parSexe = acteDecesRepository.statistiquesParSexe().map { array ->
            mapOf("sexe" to array[0], "nombre" to array[1])
        }

        val moyenneAge = acteDecesRepository.moyenneAgeAuDeces()
        val actesAvecCause = acteDecesRepository.countActesAvecCause()
        val actesSansCause = total - actesAvecCause
        val actesAvecMedecin = acteDecesRepository.countActesAvecMedecin()
        val actesSansMedecin = total - actesAvecMedecin

        return ActeDecesStatistiques(
            totalActes = total,
            actesAujourdhui = aujourd,
            actesCeMois = ceMois,
            actesEnregistrementTardif = enregistrementTardif,
            repartitionParCommune = parCommune,
            repartitionParOfficier = parOfficier,
            repartitionParMois = parMois,
            repartitionParCause = parCause,
            repartitionParAge = emptyMap(), // À implémenter si nécessaire
            repartitionParSexe = parSexe.associate { it["sexe"] as String to (it["nombre"] as Long) },
            moyenneAgeAuDeces = moyenneAge,
            actesAvecCause = actesAvecCause,
            actesSansCause = actesSansCause,
            actesAvecMedecin = actesAvecMedecin,
            actesSansMedecin = actesSansMedecin
        )
    }

    // ====== MÉTHODES PRIVÉES DE VALIDATION ======

    /**
     * Valider les données d'un acte de décès
     */
    private fun validerDonneesActe(request: ActeDecesRequest, defunt: org.megamind.rdc_etat_civil.personne.Personne) {
        // 1. Validation de la date de décès
        if (request.dateDeces.isAfter(LocalDate.now())) {
            throw BadRequestException("La date de décès ne peut pas être dans le futur")
        }

        // 2. Validation de la date d'enregistrement
        if (request.dateEnregistrement.isAfter(LocalDate.now())) {
            throw BadRequestException("La date d'enregistrement ne peut pas être dans le futur")
        }

        // 3. Validation de cohérence des dates
        if (request.dateEnregistrement.isBefore(request.dateDeces)) {
            throw BadRequestException("La date d'enregistrement ne peut pas être antérieure à la date de décès")
        }

        // 4. Validation du format du numéro d'acte
        if (!request.numeroActe.matches(Regex("^[A-Z0-9/-]+$"))) {
            throw BadRequestException("Le format du numéro d'acte n'est pas valide. Utilisez uniquement des lettres majuscules, chiffres, tirets et barres obliques")
        }

        // 5. Validation de la longueur des champs
        if (request.numeroActe.length < 5) {
            throw BadRequestException("Le numéro d'acte doit contenir au moins 5 caractères")
        }

        // 6. Validation de l'âge du défunt
        defunt.dateNaissance?.let { dateNaissance ->
            val ageAuDeces = ChronoUnit.YEARS.between(dateNaissance, request.dateDeces)
            if (ageAuDeces < 0) {
                throw BadRequestException("La date de décès ne peut pas être antérieure à la date de naissance")
            }
            if (ageAuDeces > 120) {
                throw BadRequestException("L'âge au décès semble irréaliste (plus de 120 ans)")
            }
        }

        // 7. Validation des témoins (recommandation, pas obligatoire)
        if (request.temoin1.isNullOrBlank() && request.temoin2.isNullOrBlank()) {
            // Pas d'erreur mais pourrait générer une alerte/log pour l'administration
        }
    }

    /**
     * Générer les statistiques d'un lot traité
     */
    private fun genererStatistiquesBatch(
        resultats: List<ActeDecesBatchItemResponse>,
        actes: List<ActeDecesItemRequest>
    ): BatchStatistiques {
        val repartitionParCommune = actes.groupBy { 
            it.communeId.toString() 
        }.mapValues { it.value.size }

        val repartitionParOfficier = actes.groupBy { it.officier }
            .mapValues { it.value.size }

        val repartitionParDate = actes.groupBy { it.dateEnregistrement }
            .mapValues { it.value.size }

        val actesAvecTemoins = actes.count { 
            !it.temoin1.isNullOrBlank() || !it.temoin2.isNullOrBlank() 
        }
        val actesSansTemoins = actes.size - actesAvecTemoins

        val actesAvecCause = actes.count { !it.causeDeces.isNullOrBlank() }
        val actesSansCause = actes.size - actesAvecCause

        // Calcul des enregistrements tardifs
        val enregistrementsTardifs = actes.count { 
            it.dateEnregistrement.isAfter(it.dateDeces.plusDays(30))
        }

        return BatchStatistiques(
            repartitionParCommune = repartitionParCommune,
            repartitionParOfficier = repartitionParOfficier,
            repartitionParDate = repartitionParDate,
            actesAvecTemoins = actesAvecTemoins,
            actesSansTemoins = actesSansTemoins,
            enregistrementsTardifs = enregistrementsTardifs,
            actesAvecCause = actesAvecCause,
            actesSansCause = actesSansCause
        )
    }

    /**
     * Valider un lot d'actes avant traitement
     */
    fun validerLot(request: BatchValidationRequest): BatchValidationResponse {
        val erreursValidation = mutableListOf<BatchValidationError>()
        val alertes = mutableListOf<BatchValidationAlert>()

        // Validation de chaque acte
        request.actes.forEachIndexed { index, acte ->
            val numeroOrdre = acte.numeroOrdre ?: (index + 1)
            
            // Vérification de l'existence du défunt
            if (!personneRepository.existsById(acte.defuntId)) {
                erreursValidation.add(
                    BatchValidationError(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeErreur = "DEFUNT_INTROUVABLE",
                        message = "Le défunt avec l'ID ${acte.defuntId} n'existe pas",
                        champ = "defuntId"
                    )
                )
            }

            // Vérification de l'existence de la commune
            if (!communeRepository.existsById(acte.communeId)) {
                erreursValidation.add(
                    BatchValidationError(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeErreur = "COMMUNE_INTROUVABLE",
                        message = "La commune avec l'ID ${acte.communeId} n'existe pas",
                        champ = "communeId"
                    )
                )
            }

            // Vérification de l'unicité du numéro d'acte
            if (acteDecesRepository.existsByNumeroActe(acte.numeroActe)) {
                erreursValidation.add(
                    BatchValidationError(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeErreur = "NUMERO_ACTE_EXISTANT",
                        message = "Le numéro d'acte ${acte.numeroActe} existe déjà",
                        champ = "numeroActe"
                    )
                )
            }

            // Vérification si le défunt a déjà un acte
            if (personneRepository.existsById(acte.defuntId)) {
                val defunt = personneRepository.findById(acte.defuntId).orElse(null)
                if (defunt != null && acteDecesRepository.existsByDefunt(defunt)) {
                    erreursValidation.add(
                        BatchValidationError(
                            numeroActe = acte.numeroActe,
                            defuntId = acte.defuntId,
                            numeroOrdre = numeroOrdre,
                            typeErreur = "DEFUNT_DEJA_ACTE",
                            message = "Le défunt avec l'ID ${acte.defuntId} a déjà un acte de décès",
                            champ = "defuntId"
                        )
                    )
                }
            }

            // Alertes pour les champs optionnels manquants
            if (acte.temoin1.isNullOrBlank() || acte.temoin2.isNullOrBlank()) {
                alertes.add(
                    BatchValidationAlert(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeAlerte = "TEMOINS_MANQUANTS",
                        message = "Un ou plusieurs témoins sont manquants",
                        severite = AlerteSeverite.WARNING
                    )
                )
            }

            if (acte.declarant.isNullOrBlank()) {
                alertes.add(
                    BatchValidationAlert(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeAlerte = "DECLARANT_MANQUANT",
                        message = "Le déclarant n'est pas spécifié",
                        severite = AlerteSeverite.INFO
                    )
                )
            }

            if (acte.causeDeces.isNullOrBlank()) {
                alertes.add(
                    BatchValidationAlert(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeAlerte = "CAUSE_MANQUANTE",
                        message = "La cause du décès n'est pas spécifiée",
                        severite = AlerteSeverite.WARNING
                    )
                )
            }

            // Vérification de la date d'enregistrement future
            if (acte.dateEnregistrement.isAfter(LocalDate.now())) {
                alertes.add(
                    BatchValidationAlert(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeAlerte = "DATE_FUTURE",
                        message = "La date d'enregistrement est dans le futur",
                        severite = AlerteSeverite.WARNING
                    )
                )
            }

            // Vérification de la cohérence des dates
            if (acte.dateEnregistrement.isBefore(acte.dateDeces)) {
                erreursValidation.add(
                    BatchValidationError(
                        numeroActe = acte.numeroActe,
                        defuntId = acte.defuntId,
                        numeroOrdre = numeroOrdre,
                        typeErreur = "DATES_INCOHERENTES",
                        message = "La date d'enregistrement ne peut pas être antérieure à la date de décès",
                        champ = "dateEnregistrement"
                    )
                )
            }
        }

        // Génération des statistiques préliminaires si pas d'erreurs critiques
        val statistiquesPreliminaires = if (erreursValidation.isEmpty()) {
            genererStatistiquesBatch(
                request.actes.map { 
                    ActeDecesBatchItemResponse(
                        numeroActe = it.numeroActe,
                        defuntId = it.defuntId,
                        success = true,
                        numeroOrdre = it.numeroOrdre ?: 1
                    )
                },
                request.actes
            )
        } else null

        val valide = erreursValidation.isEmpty()

        return BatchValidationResponse(
            valide = valide,
            nombreActes = request.actes.size,
            erreursValidation = erreursValidation,
            alertes = alertes,
            statistiquesPreliminaires = statistiquesPreliminaires
        )
    }
}
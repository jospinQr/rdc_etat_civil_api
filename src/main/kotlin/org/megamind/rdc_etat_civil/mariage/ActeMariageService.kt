package org.megamind.rdc_etat_civil.mariage

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.mariage.dto.*
import org.megamind.rdc_etat_civil.personne.Personne
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
 * Service pour la gestion des actes de mariage
 * 
 * Ce service gère toutes les opérations liées aux actes de mariage :
 * - Création, modification, suppression
 * - Recherches multicritères avancées
 * - Validations métier spécifiques au contexte RDC
 * - Traitement par lot pour l'administration
 * - Statistiques et rapports
 */
@Service
@Transactional
class ActeMariageService(
    private val acteMariageRepository: ActeMariageRepository,
    private val personneRepository: PersonneRepository,
    private val communeRepository: CommuneRepository,
    private val entiteRepository: EntiteRepository,
    private val provinceRepository: ProvinceRepository
) {

    // ====== OPÉRATIONS CRUD ======

    /**
     * Créer un nouvel acte de mariage avec toutes les validations
     */
    fun creerActeMariage(request: ActeMariageRequest): ActeMariageResponse {
        // 1. Validation du numéro d'acte (unicité)
        if (acteMariageRepository.existsByNumeroActe(request.numeroActe)) {
            throw BadRequestException("Le numéro d'acte '${request.numeroActe}' existe déjà")
        }

        // 2. Vérification de l'époux
        val epoux = personneRepository.findById(request.epouxId).orElseThrow {
            EntityNotFoundException("Époux introuvable avec l'ID: ${request.epouxId}")
        }

        // 3. Vérification de l'épouse
        val epouse = personneRepository.findById(request.epouseId).orElseThrow {
            EntityNotFoundException("Épouse introuvable avec l'ID: ${request.epouseId}")
        }

        // 4. Vérification que les personnes ne sont pas déjà mariées
        if (acteMariageRepository.estPersonneMariee(epoux.id)) {
            throw BadRequestException("L'époux est déjà marié")
        }
        if (acteMariageRepository.estPersonneMariee(epouse.id)) {
            throw BadRequestException("L'épouse est déjà mariée")
        }

        // 5. Vérification de la commune
        val commune = communeRepository.findById(request.communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: ${request.communeId}")
        }

        // 6. Validations métier spécifiques
        validerDonneesActe(request, epoux, epouse)

        // 7. Création de l'acte
        val acte = ActeMariage(
            numeroActe = request.numeroActe.trim().uppercase(),
            epoux = epoux,
            epouse = epouse,
            commune = commune,
            dateMariage = request.dateMariage,
            lieuMariage = request.lieuMariage.trim(),
            regimeMatrimonial = request.regimeMatrimonial,
            officier = request.officier.trim(),
            temoin1 = request.temoin1?.trim(),
            temoin2 = request.temoin2?.trim()
        )

        val acteSauve = acteMariageRepository.save(acte)
        return ActeMariageResponse.fromEntity(acteSauve)
    }

    /**
     * Obtenir un acte de mariage par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActeMariage(id: Long): ActeMariageResponse {
        val acte = acteMariageRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de mariage introuvable avec l'ID: $id")
        }
        return ActeMariageResponse.fromEntity(acte)
    }

    /**
     * Obtenir une version simplifiée d'un acte
     */
    @Transactional(readOnly = true)
    fun obtenirActeSimple(id: Long): ActeMariageSimple {
        val acte = acteMariageRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de mariage introuvable avec l'ID: $id")
        }
        return ActeMariageSimple.fromEntity(acte)
    }

    /**
     * Modifier un acte de mariage existant
     */
    fun modifierActeMariage(id: Long, request: ActeMariageUpdateRequest): ActeMariageResponse {
        val acteExistant = acteMariageRepository.findById(id).orElseThrow {
            EntityNotFoundException("Acte de mariage introuvable avec l'ID: $id")
        }

        // Validation du nouveau numéro d'acte si modifié
        request.numeroActe?.let { nouveauNumero ->
            if (nouveauNumero != acteExistant.numeroActe && 
                acteMariageRepository.existsByNumeroActe(nouveauNumero)) {
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
            dateMariage = request.dateMariage ?: acteExistant.dateMariage,
            lieuMariage = request.lieuMariage?.trim() ?: acteExistant.lieuMariage,
            regimeMatrimonial = request.regimeMatrimonial ?: acteExistant.regimeMatrimonial,
            officier = request.officier?.trim() ?: acteExistant.officier,
            temoin1 = request.temoin1?.trim(),
            temoin2 = request.temoin2?.trim()
        )

        val acteSauve = acteMariageRepository.save(acteModifie)
        return ActeMariageResponse.fromEntity(acteSauve)
    }

    /**
     * Supprimer un acte de mariage
     */
    fun supprimerActeMariage(id: Long) {
        if (!acteMariageRepository.existsById(id)) {
            throw EntityNotFoundException("Acte de mariage introuvable avec l'ID: $id")
        }
        acteMariageRepository.deleteById(id)
    }

    // ====== RECHERCHES ======

    /**
     * Lister tous les actes avec pagination
     */
    @Transactional(readOnly = true)
    fun listerActesMariage(page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findAll(pageable).map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Rechercher par numéro d'acte
     */
    @Transactional(readOnly = true)
    fun rechercherParNumeroActe(numeroActe: String): ActeMariageResponse? {
        val acte = acteMariageRepository.findByNumeroActe(numeroActe.trim().uppercase())
        return acte?.let { ActeMariageResponse.fromEntity(it) }
    }

    /**
     * Rechercher par nom des époux
     */
    @Transactional(readOnly = true)
    fun rechercherParNomEpouxOuEpouse(terme: String, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size)
        return acteMariageRepository.rechercherParNomEpouxOuEpouse(terme.trim(), pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Rechercher par nom de l'époux
     */
    @Transactional(readOnly = true)
    fun rechercherParNomEpoux(terme: String, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size)
        return acteMariageRepository.rechercherParNomEpoux(terme.trim(), pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Rechercher par nom de l'épouse
     */
    @Transactional(readOnly = true)
    fun rechercherParNomEpouse(terme: String, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size)
        return acteMariageRepository.rechercherParNomEpouse(terme.trim(), pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir les mariages d'une personne
     */
    @Transactional(readOnly = true)
    fun obtenirMariagesParPersonne(personneId: Long): List<ActeMariageResponse> {
        val personne = personneRepository.findById(personneId).orElseThrow {
            EntityNotFoundException("Personne introuvable avec l'ID: $personneId")
        }
        return acteMariageRepository.trouverMariagesParPersonne(personneId)
            .map { ActeMariageResponse.fromEntity(it) }
    }

    /**
     * Obtenir le dernier mariage d'une personne
     */
    @Transactional(readOnly = true)
    fun obtenirDernierMariageParPersonne(personneId: Long): ActeMariageResponse? {
        val personne = personneRepository.findById(personneId).orElseThrow {
            EntityNotFoundException("Personne introuvable avec l'ID: $personneId")
        }
        val acte = acteMariageRepository.trouverDernierMariageParPersonne(personneId)
        return acte?.let { ActeMariageResponse.fromEntity(it) }
    }

    /**
     * Obtenir les actes d'une commune
     */
    @Transactional(readOnly = true)
    fun obtenirActesParCommune(communeId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val commune = communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByCommune(commune, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir le nombre total d'actes d'une commune
     */
    @Transactional(readOnly = true)
    fun compterActesParCommune(communeId: Long): Long {
        val commune = communeRepository.findById(communeId).orElseThrow {
            EntityNotFoundException("Commune introuvable avec l'ID: $communeId")
        }
        
        return acteMariageRepository.countByCommune(commune)
    }

    /**
     * Obtenir tous les actes d'une province par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActesParProvince(provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByProvinceId(provinceId, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
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
        
        return acteMariageRepository.countByProvinceId(provinceId)
    }

    /**
     * Obtenir tous les actes d'une entité par son ID
     */
    @Transactional(readOnly = true)
    fun obtenirActesParEntite(entiteId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que l'entité existe
        entiteRepository.findById(entiteId).orElseThrow {
            EntityNotFoundException("Entité introuvable avec l'ID: $entiteId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByEntiteId(entiteId, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
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
        
        return acteMariageRepository.countByEntiteId(entiteId)
    }

    /**
     * Recherche multicritères avancée
     */
    @Transactional(readOnly = true)
    fun rechercherActes(criteria: ActeMariageSearchCriteria): Page<ActeMariageSimple> {
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

        return acteMariageRepository.rechercheAvanceeMariageRDC(
            provinceId = criteria.provinceId,
            entiteId = criteria.entiteId,
            communeId = criteria.communeId,
            sexeEpoux = criteria.sexeEpoux,
            sexeEpouse = criteria.sexeEpouse,
            regimeMatrimonial = criteria.regimeMatrimonial,
            terme = criteria.nomEpoux ?: criteria.nomEpouse,
            dateDebut = criteria.dateMariageDebut,
            dateFin = criteria.dateMariageFin,
            pageable = pageable
        ).map { ActeMariageSimple.fromEntity(it) }
    }

    // ====== RECHERCHES PAR SEXE ======

    /**
     * Obtenir tous les actes d'époux d'un sexe donné
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEpoux(sexe: Sexe, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findBySexeEpoux(sexe, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir tous les actes d'épouses d'un sexe donné
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEpouse(sexe: Sexe, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findBySexeEpouse(sexe, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes d'époux d'un sexe donné dans une province spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEpouxEtProvince(sexe: Sexe, provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findBySexeEpouxAndProvinceId(sexe, provinceId, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes d'épouses d'un sexe donné dans une province spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParSexeEpouseEtProvince(sexe: Sexe, provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findBySexeEpouseAndProvinceId(sexe, provinceId, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Compter les actes par sexe des époux
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEpoux(sexe: Sexe): Long {
        return acteMariageRepository.countBySexeEpoux(sexe)
    }

    /**
     * Compter les actes par sexe des épouses
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEpouse(sexe: Sexe): Long {
        return acteMariageRepository.countBySexeEpouse(sexe)
    }

    /**
     * Compter les actes par sexe des époux dans une province
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEpouxEtProvince(sexe: Sexe, provinceId: Long): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteMariageRepository.countBySexeEpouxAndProvinceId(sexe, provinceId)
    }

    /**
     * Compter les actes par sexe des épouses dans une province
     */
    @Transactional(readOnly = true)
    fun compterActesParSexeEpouseEtProvince(sexe: Sexe, provinceId: Long): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteMariageRepository.countBySexeEpouseAndProvinceId(sexe, provinceId)
    }

    // ====== RECHERCHES PAR RÉGIME MATRIMONIAL ======

    /**
     * Obtenir tous les actes d'un régime matrimonial donné
     */
    @Transactional(readOnly = true)
    fun obtenirActesParRegimeMatrimonial(regimeMatrimonial: RegimeMatrimonial, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByRegimeMatrimonial(regimeMatrimonial, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes d'un régime matrimonial donné dans une province spécifique
     */
    @Transactional(readOnly = true)
    fun obtenirActesParRegimeMatrimonialEtProvince(regimeMatrimonial: RegimeMatrimonial, provinceId: Long, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByRegimeMatrimonialAndProvinceId(regimeMatrimonial, provinceId, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Compter les actes par régime matrimonial
     */
    @Transactional(readOnly = true)
    fun compterActesParRegimeMatrimonial(regimeMatrimonial: RegimeMatrimonial): Long {
        return acteMariageRepository.countByRegimeMatrimonial(regimeMatrimonial)
    }

    /**
     * Compter les actes par régime matrimonial dans une province
     */
    @Transactional(readOnly = true)
    fun compterActesParRegimeMatrimonialEtProvince(regimeMatrimonial: RegimeMatrimonial, provinceId: Long): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteMariageRepository.countByRegimeMatrimonialAndProvinceId(regimeMatrimonial, provinceId)
    }

    // ====== RECHERCHES PAR PÉRIODE ======

    /**
     * Obtenir les actes par période
     */
    @Transactional(readOnly = true)
    fun obtenirActesParPeriode(dateDebut: LocalDate, dateFin: LocalDate, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByDateMariageBetween(dateDebut, dateFin, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Obtenir les actes par période dans une province
     */
    @Transactional(readOnly = true)
    fun obtenirActesParPeriodeEtProvince(provinceId: Long, dateDebut: LocalDate, dateFin: LocalDate, page: Int = 0, size: Int = 20): Page<ActeMariageSimple> {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by("dateMariage").descending())
        return acteMariageRepository.findByProvinceIdAndDateMariageBetween(provinceId, dateDebut, dateFin, pageable)
            .map { ActeMariageSimple.fromEntity(it) }
    }

    /**
     * Compter les actes par période
     */
    @Transactional(readOnly = true)
    fun compterActesParPeriode(dateDebut: LocalDate, dateFin: LocalDate): Long {
        return acteMariageRepository.countByDateMariageBetween(dateDebut, dateFin)
    }

    /**
     * Compter les actes par période dans une province
     */
    @Transactional(readOnly = true)
    fun compterActesParPeriodeEtProvince(provinceId: Long, dateDebut: LocalDate, dateFin: LocalDate): Long {
        // Vérifier que la province existe
        provinceRepository.findById(provinceId).orElseThrow {
            EntityNotFoundException("Province introuvable avec l'ID: $provinceId")
        }
        
        return acteMariageRepository.countByProvinceIdAndDateMariageBetween(provinceId, dateDebut, dateFin)
    }

    // ====== VALIDATIONS ======

    /**
     * Vérifier si un numéro d'acte existe
     */
    @Transactional(readOnly = true)
    fun verifierNumeroActe(numeroActe: String): Boolean {
        return acteMariageRepository.existsByNumeroActe(numeroActe.trim().uppercase())
    }

    /**
     * Vérifier si une personne est mariée
     */
    @Transactional(readOnly = true)
    fun verifierPersonneMariee(personneId: Long): Boolean {
        return acteMariageRepository.estPersonneMariee(personneId)
    }

    // ====== TRAITEMENT PAR LOT ======

    /**
     * Créer plusieurs actes de mariage en une seule fois
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun creerActesEnLot(request: ActeMariageBatchRequest): ActeMariageBatchResponse {
        val tempsDebut = System.currentTimeMillis()
        val resultats = mutableListOf<ActeMariageBatchItemResponse>()
        var actesReussis = 0
        var actesEchecs = 0

        request.actes.forEachIndexed { index, acteRequest ->
            try {
                // Transformation vers ActeMariageRequest standard
                val requestStandard = ActeMariageRequest(
                    numeroActe = acteRequest.numeroActe,
                    epouxId = acteRequest.epouxId,
                    epouseId = acteRequest.epouseId,
                    communeId = acteRequest.communeId,
                    dateMariage = acteRequest.dateMariage,
                    lieuMariage = acteRequest.lieuMariage,
                    regimeMatrimonial = acteRequest.regimeMatrimonial,
                    officier = acteRequest.officier,
                    temoin1 = acteRequest.temoin1,
                    temoin2 = acteRequest.temoin2
                )

                val acteCreee = creerActeMariage(requestStandard)
                resultats.add(
                    ActeMariageBatchItemResponse(
                        numeroActe = acteRequest.numeroActe,
                        epouxId = acteRequest.epouxId,
                        epouseId = acteRequest.epouseId,
                        success = true,
                        acteId = acteCreee.id,
                        numeroOrdre = acteRequest.numeroOrdre ?: (index + 1),
                        reference = acteRequest.reference
                    )
                )
                actesReussis++
            } catch (e: Exception) {
                resultats.add(
                    ActeMariageBatchItemResponse(
                        numeroActe = acteRequest.numeroActe,
                        epouxId = acteRequest.epouxId,
                        epouseId = acteRequest.epouseId,
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

        return ActeMariageBatchResponse(
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
    fun obtenirStatistiques(): ActeMariageStatistiques {
        val total = acteMariageRepository.count()
        val aujourd = acteMariageRepository.countByDateMariageBetween(LocalDate.now(), LocalDate.now())
        
        val debutMois = LocalDate.now().withDayOfMonth(1)
        val finMois = LocalDate.now()
        val ceMois = acteMariageRepository.countByDateMariageBetween(debutMois, finMois)
        
        val parCommune = acteMariageRepository.statistiquesMariagePaysParCommune().map { array ->
            mapOf("commune" to array[0], "nombre" to array[1])
        }
        
        val parOfficier = acteMariageRepository.statistiquesMariageCommuneParOfficier(1L).map { array ->
            mapOf("officier" to array[0], "nombre" to array[1])
        }
        
        val parMois = acteMariageRepository.statistiquesMariageCommuneParMois(1L, LocalDate.now().minusMonths(12)).map { array ->
            mapOf(
                "annee" to array[0], 
                "mois" to array[1], 
                "nombre" to array[2]
            )
        }

        val parRegime = acteMariageRepository.statistiquesMariagePaysParRegime().map { array ->
            mapOf("regime" to array[0], "nombre" to array[1])
        }

        val parSexeEpoux = acteMariageRepository.statistiquesMariageProvinceParSexeEpoux(1L).associate { array ->
            array[0] as Sexe to (array[1] as Long)
        }.mapKeys { it.key.name }

        val parSexeEpouse = acteMariageRepository.statistiquesMariageProvinceParSexeEpouse(1L).associate { array ->
            array[0] as Sexe to (array[1] as Long)
        }.mapKeys { it.key.name }

        val moyenneAgeEpoux = acteMariageRepository.moyenneAgeEpouxAuMariage()
        val moyenneAgeEpouse = acteMariageRepository.moyenneAgeEpouseAuMariage()
        val mariagesAvecTemoins = acteMariageRepository.countMariagesAvecTemoins()
        val mariagesSansTemoins = acteMariageRepository.countMariagesSansTemoins()

        return ActeMariageStatistiques(
            totalActes = total,
            actesAujourdhui = aujourd,
            actesCeMois = ceMois,
            repartitionParCommune = parCommune,
            repartitionParOfficier = parOfficier,
            repartitionParMois = parMois,
            repartitionParRegime = parRegime,
            repartitionParSexeEpoux = parSexeEpoux,
            repartitionParSexeEpouse = parSexeEpouse,
            moyenneAgeEpouxAuMariage = moyenneAgeEpoux,
            moyenneAgeEpouseAuMariage = moyenneAgeEpouse,
            mariagesAvecTemoins = mariagesAvecTemoins,
            mariagesSansTemoins = mariagesSansTemoins
        )
    }

    // ====== MÉTHODES PRIVÉES DE VALIDATION ======

    /**
     * Valider les données d'un acte de mariage
     */
    private fun validerDonneesActe(request: ActeMariageRequest, epoux: Personne, epouse: Personne) {
        // 1. Validation de l'âge minimum pour le mariage (18 ans)
        epoux.dateNaissance?.let { dateNaissance ->
            val age = ChronoUnit.YEARS.between(dateNaissance, request.dateMariage)
            if (age < 18) {
                throw BadRequestException("L'époux doit avoir au moins 18 ans au moment du mariage")
            }
        }

        epouse.dateNaissance?.let { dateNaissance ->
            val age = ChronoUnit.YEARS.between(dateNaissance, request.dateMariage)
            if (age < 18) {
                throw BadRequestException("L'épouse doit avoir au moins 18 ans au moment du mariage")
            }
        }

        // 2. Validation de la date de mariage
        if (request.dateMariage.isAfter(LocalDate.now())) {
            throw BadRequestException("La date de mariage ne peut pas être dans le futur")
        }

        // 3. Validation du format du numéro d'acte
        if (!request.numeroActe.matches(Regex("^[A-Z0-9/-]+$"))) {
            throw BadRequestException("Le format du numéro d'acte n'est pas valide. Utilisez uniquement des lettres majuscules, chiffres, tirets et barres obliques")
        }

        // 4. Validation de la longueur des champs
        if (request.numeroActe.length < 5) {
            throw BadRequestException("Le numéro d'acte doit contenir au moins 5 caractères")
        }

        // 5. Validation des témoins (recommandation, pas obligatoire)
        if (request.temoin1.isNullOrBlank() && request.temoin2.isNullOrBlank()) {
            // Pas d'erreur mais pourrait générer une alerte/log pour l'administration
        }

        // 6. Validation que les époux ne sont pas la même personne
        if (epoux.id == epouse.id) {
            throw BadRequestException("Une personne ne peut pas se marier avec elle-même")
        }
    }

    /**
     * Générer les statistiques d'un lot traité
     */
    private fun genererStatistiquesBatch(
        resultats: List<ActeMariageBatchItemResponse>,
        actes: List<ActeMariageItemRequest>
    ): BatchStatistiques {
        val repartitionParCommune = actes.groupBy { 
            it.communeId.toString() 
        }.mapValues { it.value.size }

        val repartitionParOfficier = actes.groupBy { it.officier }
            .mapValues { it.value.size }

        val repartitionParDate = actes.groupBy { it.dateMariage }
            .mapValues { it.value.size }
            .mapKeys { it.key.toString() }

        val mariagesAvecTemoins = actes.count { 
            !it.temoin1.isNullOrBlank() || !it.temoin2.isNullOrBlank() 
        }
        val mariagesSansTemoins = actes.size - mariagesAvecTemoins

        return BatchStatistiques(
            repartitionParCommune = repartitionParCommune,
            repartitionParOfficier = repartitionParOfficier,
            repartitionParDate = repartitionParDate,
            mariagesAvecTemoins = mariagesAvecTemoins,
            mariagesSansTemoins = mariagesSansTemoins
        )
    }
}

package org.megamind.rdc_etat_civil.mariage

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.mariage.dto.*
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.utils.PaginatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/actes-mariage")
class ActeMariageController(
    private val acteMariageService: ActeMariageService
) {

    // ====== ENDPOINTS CRUD ======

    /**
     * Créer un nouvel acte de mariage
     * 
     * POST /api/actes-mariage
     */
    @PostMapping
    fun creerActe(@Valid @RequestBody request: ActeMariageRequest): ResponseEntity<ActeMariageResponse> {
        // Validations métier de base
        if (request.epouxId <= 0) {
            throw BadRequestException("L'ID de l'époux doit être un nombre positif")
        }
        
        if (request.epouseId <= 0) {
            throw BadRequestException("L'ID de l'épouse doit être un nombre positif")
        }
        
        if (request.communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (request.numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        if (request.officier.isBlank()) {
            throw BadRequestException("Le nom de l'officier d'état civil ne peut pas être vide")
        }
        
        if (request.lieuMariage.isBlank()) {
            throw BadRequestException("Le lieu de mariage ne peut pas être vide")
        }
        
        // Validation de la date de mariage
        if (request.dateMariage.isAfter(LocalDate.now())) {
            throw BadRequestException("La date de mariage ne peut pas être dans le futur")
        }
        
        val acteCreee = acteMariageService.creerActeMariage(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(acteCreee)
    }

    /**
     * Récupérer un acte par son ID
     * 
     * GET /api/actes-mariage/{id}
     */
    @GetMapping("/{id}")
    fun obtenirActeParId(@PathVariable id: Long): ResponseEntity<ActeMariageResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acte = acteMariageService.obtenirActeMariage(id)
        return ResponseEntity.ok(acte)
    }

    /**
     * Mettre à jour un acte de mariage
     * 
     * PUT /api/actes-mariage/{id}
     */
    @PutMapping("/{id}")
    fun mettreAJourActe(
        @PathVariable id: Long,
        @Valid @RequestBody request: ActeMariageUpdateRequest
    ): ResponseEntity<ActeMariageResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        // Validations des champs modifiables
        if (request.officier?.isBlank() == true) {
            throw BadRequestException("Le nom de l'officier d'état civil ne peut pas être vide")
        }
        
        if (request.dateMariage?.isAfter(LocalDate.now()) == true) {
            throw BadRequestException("La date de mariage ne peut pas être dans le futur")
        }
        
        val acteModifie = acteMariageService.modifierActeMariage(id, request)
        return ResponseEntity.ok(acteModifie)
    }

    /**
     * Supprimer un acte de mariage
     * 
     * DELETE /api/actes-mariage/{id}
     */
    @DeleteMapping("/{id}")
    fun supprimerActe(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        acteMariageService.supprimerActeMariage(id)
        return ResponseEntity.ok(mapOf("message" to "Acte de mariage supprimé avec succès"))
    }

    // ====== ENDPOINTS DE RECHERCHE ======

    /**
     * Lister tous les actes avec pagination
     * 
     * GET /api/actes-mariage
     */
    @GetMapping
    fun listerActes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.listerActesMariage(page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Rechercher par numéro d'acte
     * 
     * GET /api/actes-mariage/numero/{numeroActe}
     */
    @GetMapping("/numero/{numeroActe}")
    fun rechercherParNumero(@PathVariable numeroActe: String): ResponseEntity<ActeMariageResponse> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val acte = acteMariageService.rechercherParNumeroActe(numeroActe)
            ?: throw EntityNotFoundException("Aucun acte trouvé avec le numéro: $numeroActe")
        
        return ResponseEntity.ok(acte)
    }

    /**
     * Rechercher par nom des époux
     * 
     * GET /api/actes-mariage/epoux/nom
     */
    @GetMapping("/epoux/nom")
    fun rechercherParNomEpoux(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (terme.isBlank()) {
            throw BadRequestException("Le terme de recherche ne peut pas être vide")
        }
        
        if (terme.length < 2) {
            throw BadRequestException("Le terme de recherche doit contenir au moins 2 caractères")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.rechercherParNomEpoux(terme, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Rechercher par nom des épouses
     * 
     * GET /api/actes-mariage/epouse/nom
     */
    @GetMapping("/epouse/nom")
    fun rechercherParNomEpouse(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (terme.isBlank()) {
            throw BadRequestException("Le terme de recherche ne peut pas être vide")
        }
        
        if (terme.length < 2) {
            throw BadRequestException("Le terme de recherche doit contenir au moins 2 caractères")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.rechercherParNomEpouse(terme, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Rechercher par nom des époux ou épouses
     * 
     * GET /api/actes-mariage/nom
     */
    @GetMapping("/nom")
    fun rechercherParNomEpouxOuEpouse(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (terme.isBlank()) {
            throw BadRequestException("Le terme de recherche ne peut pas être vide")
        }
        
        if (terme.length < 2) {
            throw BadRequestException("Le terme de recherche doit contenir au moins 2 caractères")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.rechercherParNomEpouxOuEpouse(terme, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Rechercher les mariages d'une personne par son ID
     * 
     * GET /api/actes-mariage/personne/{personneId}
     */
    @GetMapping("/personne/{personneId}")
    fun obtenirMariagesParPersonne(@PathVariable personneId: Long): ResponseEntity<List<ActeMariageResponse>> {
        if (personneId <= 0) {
            throw BadRequestException("L'ID de la personne doit être un nombre positif")
        }
        
        val mariages = acteMariageService.obtenirMariagesParPersonne(personneId)
        return ResponseEntity.ok(mariages)
    }

    /**
     * Obtenir le dernier mariage d'une personne
     * 
     * GET /api/actes-mariage/personne/{personneId}/dernier
     */
    @GetMapping("/personne/{personneId}/dernier")
    fun obtenirDernierMariageParPersonne(@PathVariable personneId: Long): ResponseEntity<ActeMariageResponse> {
        if (personneId <= 0) {
            throw BadRequestException("L'ID de la personne doit être un nombre positif")
        }
        
        val mariage = acteMariageService.obtenirDernierMariageParPersonne(personneId)
            ?: throw EntityNotFoundException("Aucun mariage trouvé pour la personne avec l'ID: $personneId")
        
        return ResponseEntity.ok(mariage)
    }

    /**
     * Rechercher les actes d'une commune
     * 
     * GET /api/actes-mariage/commune/{communeId}
     */
    @GetMapping("/commune/{communeId}")
    fun obtenirActesParCommune(
        @PathVariable communeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParCommune(communeId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Obtenir le nombre total d'actes d'une commune
     * 
     * GET /api/actes-mariage/commune/{communeId}/count
     */
    @GetMapping("/commune/{communeId}/count")
    fun compterActesParCommune(@PathVariable communeId: Long): ResponseEntity<Map<String, Any>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParCommune(communeId)
        return ResponseEntity.ok(mapOf(
            "communeId" to communeId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une entité
     * 
     * GET /api/actes-mariage/entite/{entiteId}
     */
    @GetMapping("/entite/{entiteId}")
    fun obtenirActesParEntite(
        @PathVariable entiteId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParEntite(entiteId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Obtenir le nombre total d'actes d'une entité
     * 
     * GET /api/actes-mariage/entite/{entiteId}/count
     */
    @GetMapping("/entite/{entiteId}/count")
    fun compterActesParEntite(@PathVariable entiteId: Long): ResponseEntity<Map<String, Any>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParEntite(entiteId)
        return ResponseEntity.ok(mapOf(
            "entiteId" to entiteId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une province
     * 
     * GET /api/actes-mariage/province/{provinceId}
     */
    @GetMapping("/province/{provinceId}")
    fun obtenirActesParProvince(
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParProvince(provinceId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Obtenir le nombre total d'actes d'une province
     * 
     * GET /api/actes-mariage/province/{provinceId}/count
     */
    @GetMapping("/province/{provinceId}/count")
    fun compterActesParProvince(@PathVariable provinceId: Long): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParProvince(provinceId)
        return ResponseEntity.ok(mapOf(
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS DE TRAITEMENT EN LOT ======

    /**
     * Créer plusieurs actes de mariage en lot
     * 
     * POST /api/actes-mariage/lot
     */
    @PostMapping("/lot")
    fun creerActesEnLot(@Valid @RequestBody request: ActeMariageBatchRequest): ResponseEntity<ActeMariageBatchResponse> {
        // Validations de base
        if (request.actes.isEmpty()) {
            throw BadRequestException("Le lot ne peut pas être vide")
        }
        
        if (request.actes.size > 100) {
            throw BadRequestException("Un lot ne peut pas contenir plus de 100 actes")
        }
        
        // Validation des numéros d'acte uniques dans le lot
        val numerosActes = request.actes.map { it.numeroActe }
        val numerosDupliques = numerosActes.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        
        if (numerosDupliques.isNotEmpty()) {
            throw BadRequestException("Numéros d'acte dupliqués dans le lot: ${numerosDupliques.joinToString(", ")}")
        }
        
        val response = acteMariageService.creerActesEnLot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ====== ENDPOINTS DE VALIDATION ======

    /**
     * Vérifier si un numéro d'acte existe
     * 
     * GET /api/actes-mariage/verification/numero/{numeroActe}
     */
    @GetMapping("/verification/numero/{numeroActe}")
    fun verifierNumeroActe(@PathVariable numeroActe: String): ResponseEntity<Map<String, Any>> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val existe = acteMariageService.verifierNumeroActe(numeroActe)
        return ResponseEntity.ok(mapOf(
            "numeroActe" to numeroActe,
            "existe" to existe,
            "disponible" to !existe
        ))
    }

    /**
     * Vérifier si une personne est mariée
     * 
     * GET /api/actes-mariage/verification/personne/{personneId}
     */
    @GetMapping("/verification/personne/{personneId}")
    fun verifierPersonneMariee(@PathVariable personneId: Long): ResponseEntity<Map<String, Any>> {
        if (personneId <= 0) {
            throw BadRequestException("L'ID de la personne doit être un nombre positif")
        }
        
        val estMariee = acteMariageService.verifierPersonneMariee(personneId)
        return ResponseEntity.ok(mapOf(
            "personneId" to personneId,
            "estMariee" to estMariee,
            "peutSeMarier" to !estMariee
        ))
    }

    /**
     * Obtenir un acte en version simple
     * 
     * GET /api/actes-mariage/{id}/simple
     */
    @GetMapping("/{id}/simple")
    fun obtenirActeSimple(@PathVariable id: Long): ResponseEntity<ActeMariageSimple> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acteSimple = acteMariageService.obtenirActeSimple(id)
        return ResponseEntity.ok(acteSimple)
    }

    // ====== ENDPOINTS PAR SEXE ======

    /**
     * Récupérer tous les actes d'époux d'un sexe donné
     * 
     * GET /api/actes-mariage/sexe/epoux/{sexe}
     */
    @GetMapping("/sexe/epoux/{sexe}")
    fun obtenirActesParSexeEpoux(
        @PathVariable sexe: Sexe,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size !in 1..100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParSexeEpoux(sexe, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Récupérer tous les actes d'épouses d'un sexe donné
     * 
     * GET /api/actes-mariage/sexe/epouse/{sexe}
     */
    @GetMapping("/sexe/epouse/{sexe}")
    fun obtenirActesParSexeEpouse(
        @PathVariable sexe: Sexe,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size !in 1..100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParSexeEpouse(sexe, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par sexe des époux
     * 
     * GET /api/actes-mariage/sexe/epoux/{sexe}/count
     */
    @GetMapping("/sexe/epoux/{sexe}/count")
    fun compterActesParSexeEpoux(@PathVariable sexe: Sexe): ResponseEntity<Map<String, Any>> {
        val count = acteMariageService.compterActesParSexeEpoux(sexe)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "totalActes" to count
        ))
    }

    /**
     * Compter les actes par sexe des épouses
     * 
     * GET /api/actes-mariage/sexe/epouse/{sexe}/count
     */
    @GetMapping("/sexe/epouse/{sexe}/count")
    fun compterActesParSexeEpouse(@PathVariable sexe: Sexe): ResponseEntity<Map<String, Any>> {
        val count = acteMariageService.compterActesParSexeEpouse(sexe)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes d'époux d'un sexe dans une province
     * 
     * GET /api/actes-mariage/sexe/epoux/{sexe}/province/{provinceId}
     */
    @GetMapping("/sexe/epoux/{sexe}/province/{provinceId}")
    fun obtenirActesParSexeEpouxEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParSexeEpouxEtProvince(sexe, provinceId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Récupérer les actes d'épouses d'un sexe dans une province
     * 
     * GET /api/actes-mariage/sexe/epouse/{sexe}/province/{provinceId}
     */
    @GetMapping("/sexe/epouse/{sexe}/province/{provinceId}")
    fun obtenirActesParSexeEpouseEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParSexeEpouseEtProvince(sexe, provinceId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par sexe des époux dans une province
     * 
     * GET /api/actes-mariage/sexe/epoux/{sexe}/province/{provinceId}/count
     */
    @GetMapping("/sexe/epoux/{sexe}/province/{provinceId}/count")
    fun compterActesParSexeEpouxEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParSexeEpouxEtProvince(sexe, provinceId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    /**
     * Compter les actes par sexe des épouses dans une province
     * 
     * GET /api/actes-mariage/sexe/epouse/{sexe}/province/{provinceId}/count
     */
    @GetMapping("/sexe/epouse/{sexe}/province/{provinceId}/count")
    fun compterActesParSexeEpouseEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParSexeEpouseEtProvince(sexe, provinceId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS PAR RÉGIME MATRIMONIAL ======

    /**
     * Récupérer tous les actes d'un régime matrimonial donné
     * 
     * GET /api/actes-mariage/regime/{regimeMatrimonial}
     */
    @GetMapping("/regime/{regimeMatrimonial}")
    fun obtenirActesParRegimeMatrimonial(
        @PathVariable regimeMatrimonial: RegimeMatrimonial,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size !in 1..100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParRegimeMatrimonial(regimeMatrimonial, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par régime matrimonial
     * 
     * GET /api/actes-mariage/regime/{regimeMatrimonial}/count
     */
    @GetMapping("/regime/{regimeMatrimonial}/count")
    fun compterActesParRegimeMatrimonial(@PathVariable regimeMatrimonial: RegimeMatrimonial): ResponseEntity<Map<String, Any>> {
        val count = acteMariageService.compterActesParRegimeMatrimonial(regimeMatrimonial)
        return ResponseEntity.ok(mapOf(
            "regimeMatrimonial" to regimeMatrimonial,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes d'un régime matrimonial dans une province
     * 
     * GET /api/actes-mariage/regime/{regimeMatrimonial}/province/{provinceId}
     */
    @GetMapping("/regime/{regimeMatrimonial}/province/{provinceId}")
    fun obtenirActesParRegimeMatrimonialEtProvince(
        @PathVariable regimeMatrimonial: RegimeMatrimonial,
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParRegimeMatrimonialEtProvince(regimeMatrimonial, provinceId, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par régime matrimonial dans une province
     * 
     * GET /api/actes-mariage/regime/{regimeMatrimonial}/province/{provinceId}/count
     */
    @GetMapping("/regime/{regimeMatrimonial}/province/{provinceId}/count")
    fun compterActesParRegimeMatrimonialEtProvince(
        @PathVariable regimeMatrimonial: RegimeMatrimonial,
        @PathVariable provinceId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteMariageService.compterActesParRegimeMatrimonialEtProvince(regimeMatrimonial, provinceId)
        return ResponseEntity.ok(mapOf(
            "regimeMatrimonial" to regimeMatrimonial,
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS PAR PÉRIODE ======

    /**
     * Récupérer les actes par période
     * 
     * GET /api/actes-mariage/periode
     */
    @GetMapping("/periode")
    fun obtenirActesParPeriode(
        @RequestParam dateDebut: LocalDate,
        @RequestParam dateFin: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (dateDebut.isAfter(dateFin)) {
            throw BadRequestException("La date de début ne peut pas être postérieure à la date de fin")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParPeriode(dateDebut, dateFin, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par période
     * 
     * GET /api/actes-mariage/periode/count
     */
    @GetMapping("/periode/count")
    fun compterActesParPeriode(
        @RequestParam dateDebut: LocalDate,
        @RequestParam dateFin: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        if (dateDebut.isAfter(dateFin)) {
            throw BadRequestException("La date de début ne peut pas être postérieure à la date de fin")
        }
        
        val count = acteMariageService.compterActesParPeriode(dateDebut, dateFin)
        return ResponseEntity.ok(mapOf(
            "dateDebut" to dateDebut,
            "dateFin" to dateFin,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes par période dans une province
     * 
     * GET /api/actes-mariage/periode/province/{provinceId}
     */
    @GetMapping("/periode/province/{provinceId}")
    fun obtenirActesParPeriodeEtProvince(
        @PathVariable provinceId: Long,
        @RequestParam dateDebut: LocalDate,
        @RequestParam dateFin: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (dateDebut.isAfter(dateFin)) {
            throw BadRequestException("La date de début ne peut pas être postérieure à la date de fin")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.obtenirActesParPeriodeEtProvince(provinceId, dateDebut, dateFin, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }

    /**
     * Compter les actes par période dans une province
     * 
     * GET /api/actes-mariage/periode/province/{provinceId}/count
     */
    @GetMapping("/periode/province/{provinceId}/count")
    fun compterActesParPeriodeEtProvince(
        @PathVariable provinceId: Long,
        @RequestParam dateDebut: LocalDate,
        @RequestParam dateFin: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (dateDebut.isAfter(dateFin)) {
            throw BadRequestException("La date de début ne peut pas être postérieure à la date de fin")
        }
        
        val count = acteMariageService.compterActesParPeriodeEtProvince(provinceId, dateDebut, dateFin)
        return ResponseEntity.ok(mapOf(
            "provinceId" to provinceId,
            "dateDebut" to dateDebut,
            "dateFin" to dateFin,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS DE STATISTIQUES ======

    /**
     * Obtenir les statistiques générales
     * 
     * GET /api/actes-mariage/statistiques
     */
    @GetMapping("/statistiques")
    fun obtenirStatistiques(): ResponseEntity<ActeMariageStatistiques> {
        val statistiques = acteMariageService.obtenirStatistiques()
        return ResponseEntity.ok(statistiques)
    }

    // ====== ENDPOINTS DE RECHERCHE MULTICRITÈRES ======

    /**
     * Recherche multicritères avancée
     * 
     * POST /api/actes-mariage/recherche
     */
    @PostMapping("/recherche")
    fun rechercherActes(@Valid @RequestBody criteria: ActeMariageSearchCriteria): ResponseEntity<PaginatedResponse<ActeMariageSimple>> {
        if (criteria.page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (criteria.size <= 0 || criteria.size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteMariageService.rechercherActes(criteria)
        return ResponseEntity.ok(PaginatedResponse.fromPage(actes))
    }
}
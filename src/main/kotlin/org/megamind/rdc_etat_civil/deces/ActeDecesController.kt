package org.megamind.rdc_etat_civil.deces

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.deces.dto.*
import org.megamind.rdc_etat_civil.personne.Sexe
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/actes-deces")
class ActeDecesController(
    private val acteDecesService: ActeDecesService
) {

    // ====== ENDPOINTS CRUD ======

    /**
     * Créer un nouvel acte de décès
     * 
     * POST /api/actes-deces
     */
    @PostMapping
    fun creerActe(@Valid @RequestBody request: ActeDecesRequest): ResponseEntity<ActeDecesResponse> {
        // Validations métier de base
        if (request.defuntId <= 0) {
            throw BadRequestException("L'ID du défunt doit être un nombre positif")
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
        
        if (request.lieuDeces.isBlank()) {
            throw BadRequestException("Le lieu de décès ne peut pas être vide")
        }
        
        // Validation de la date d'enregistrement
        if (request.dateEnregistrement.isAfter(LocalDate.now())) {
            throw BadRequestException("La date d'enregistrement ne peut pas être dans le futur")
        }
        
        // Validation de la cohérence des dates
        if (request.dateEnregistrement.isBefore(request.dateDeces)) {
            throw BadRequestException("La date d'enregistrement ne peut pas être antérieure à la date de décès")
        }
        
        val acteCreee = acteDecesService.creerActeDeces(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(acteCreee)
    }

    /**
     * Récupérer un acte par son ID
     * 
     * GET /api/actes-deces/{id}
     */
    @GetMapping("/{id}")
    fun obtenirActeParId(@PathVariable id: Long): ResponseEntity<ActeDecesResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acte = acteDecesService.obtenirActeDeces(id)
        return ResponseEntity.ok(acte)
    }

    /**
     * Mettre à jour un acte de décès
     * 
     * PUT /api/actes-deces/{id}
     */
    @PutMapping("/{id}")
    fun mettreAJourActe(
        @PathVariable id: Long,
        @Valid @RequestBody request: ActeDecesUpdateRequest
    ): ResponseEntity<ActeDecesResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        // Validations des champs modifiables
        if (request.officier?.isBlank() == true) {
            throw BadRequestException("Le nom de l'officier d'état civil ne peut pas être vide")
        }
        
        if (request.dateEnregistrement?.isAfter(LocalDate.now()) == true) {
            throw BadRequestException("La date d'enregistrement ne peut pas être dans le futur")
        }
        
        val acteModifie = acteDecesService.modifierActeDeces(id, request)
        return ResponseEntity.ok(acteModifie)
    }

    /**
     * Supprimer un acte de décès
     * 
     * DELETE /api/actes-deces/{id}
     */
    @DeleteMapping("/{id}")
    fun supprimerActe(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        acteDecesService.supprimerActeDeces(id)
        return ResponseEntity.ok(mapOf("message" to "Acte de décès supprimé avec succès"))
    }

    // ====== ENDPOINTS DE RECHERCHE ======

    /**
     * Lister tous les actes avec pagination
     * 
     * GET /api/actes-deces
     */
    @GetMapping
    fun listerActes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.listerActesDeces(page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Rechercher par numéro d'acte
     * 
     * GET /api/actes-deces/numero/{numeroActe}
     */
    @GetMapping("/numero/{numeroActe}")
    fun rechercherParNumero(@PathVariable numeroActe: String): ResponseEntity<ActeDecesResponse> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val acte = acteDecesService.rechercherParNumeroActe(numeroActe)
            ?: throw EntityNotFoundException("Aucun acte trouvé avec le numéro: $numeroActe")
        
        return ResponseEntity.ok(acte)
    }

    /**
     * Rechercher par nom du défunt
     * 
     * GET /api/actes-deces/defunt/nom
     */
    @GetMapping("/defunt/nom")
    fun rechercherParNomDefunt(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
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
        
        val actes = acteDecesService.rechercherParNomDefunt(terme, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Rechercher l'acte d'un défunt par son ID
     * 
     * GET /api/actes-deces/defunt/{defuntId}
     */
    @GetMapping("/defunt/{defuntId}")
    fun obtenirActeParDefunt(@PathVariable defuntId: Long): ResponseEntity<ActeDecesResponse> {
        if (defuntId <= 0) {
            throw BadRequestException("L'ID du défunt doit être un nombre positif")
        }
        
        val acte = acteDecesService.obtenirActeParDefunt(defuntId)
            ?: throw EntityNotFoundException("Aucun acte de décès trouvé pour le défunt avec l'ID: $defuntId")
        
        return ResponseEntity.ok(acte)
    }

    /**
     * Rechercher les actes d'une commune
     * 
     * GET /api/actes-deces/commune/{communeId}
     */
    @GetMapping("/commune/{communeId}")
    fun obtenirActesParCommune(
        @PathVariable communeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParCommune(communeId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une commune
     * 
     * GET /api/actes-deces/commune/{communeId}/count
     */
    @GetMapping("/commune/{communeId}/count")
    fun compterActesParCommune(@PathVariable communeId: Long): ResponseEntity<Map<String, Any>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParCommune(communeId)
        return ResponseEntity.ok(mapOf(
            "communeId" to communeId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une entité
     * 
     * GET /api/actes-deces/entite/{entiteId}
     */
    @GetMapping("/entite/{entiteId}")
    fun obtenirActesParEntite(
        @PathVariable entiteId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParEntite(entiteId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une entité
     * 
     * GET /api/actes-deces/entite/{entiteId}/count
     */
    @GetMapping("/entite/{entiteId}/count")
    fun compterActesParEntite(@PathVariable entiteId: Long): ResponseEntity<Map<String, Any>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParEntite(entiteId)
        return ResponseEntity.ok(mapOf(
            "entiteId" to entiteId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une province
     * 
     * GET /api/actes-deces/province/{provinceId}
     */
    @GetMapping("/province/{provinceId}")
    fun obtenirActesParProvince(
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParProvince(provinceId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une province
     * 
     * GET /api/actes-deces/province/{provinceId}/count
     */
    @GetMapping("/province/{provinceId}/count")
    fun compterActesParProvince(@PathVariable provinceId: Long): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParProvince(provinceId)
        return ResponseEntity.ok(mapOf(
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS DE TRAITEMENT EN LOT ======

    /**
     * Créer plusieurs actes de décès en lot
     * 
     * POST /api/actes-deces/lot
     */
    @PostMapping("/lot")
    fun creerActesEnLot(@Valid @RequestBody request: ActeDecesBatchRequest): ResponseEntity<ActeDecesBatchResponse> {
        // Validations de base
        if (request.actes.isEmpty()) {
            throw BadRequestException("Le lot ne peut pas être vide")
        }
        
        if (request.actes.size > 100) {
            throw BadRequestException("Un lot ne peut pas contenir plus de 100 actes")
        }
        
        if (request.responsableLot.isBlank()) {
            throw BadRequestException("Le responsable du lot est obligatoire")
        }
        
        // Validation des numéros d'acte uniques dans le lot
        val numerosActes = request.actes.map { it.numeroActe }
        val numerosDupliques = numerosActes.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        
        if (numerosDupliques.isNotEmpty()) {
            throw BadRequestException("Numéros d'acte dupliqués dans le lot: ${numerosDupliques.joinToString(", ")}")
        }
        
        val response = acteDecesService.creerActesEnLot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Valider un lot d'actes avant traitement
     * 
     * POST /api/actes-deces/lot/validation
     */
    @PostMapping("/lot/validation")
    fun validerLot(@Valid @RequestBody request: BatchValidationRequest): ResponseEntity<BatchValidationResponse> {
        // Validations de base
        if (request.actes.isEmpty()) {
            throw BadRequestException("Le lot ne peut pas être vide")
        }
        
        if (request.actes.size > 100) {
            throw BadRequestException("Un lot ne peut pas contenir plus de 100 actes")
        }
        
        val response = acteDecesService.validerLot(request)
        return ResponseEntity.ok(response)
    }

    // ====== ENDPOINTS DE VALIDATION ======

    /**
     * Vérifier si un numéro d'acte existe
     * 
     * GET /api/actes-deces/verification/numero/{numeroActe}
     */
    @GetMapping("/verification/numero/{numeroActe}")
    fun verifierNumeroActe(@PathVariable numeroActe: String): ResponseEntity<Map<String, Any>> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val existe = acteDecesService.verifierNumeroActe(numeroActe)
        return ResponseEntity.ok(mapOf(
            "numeroActe" to numeroActe,
            "existe" to existe,
            "disponible" to !existe
        ))
    }

    /**
     * Vérifier si un défunt a déjà un acte
     * 
     * GET /api/actes-deces/verification/defunt/{defuntId}
     */
    @GetMapping("/verification/defunt/{defuntId}")
    fun verifierDefuntActe(@PathVariable defuntId: Long): ResponseEntity<Map<String, Any>> {
        if (defuntId <= 0) {
            throw BadRequestException("L'ID du défunt doit être un nombre positif")
        }
        
        val aDejaActe = acteDecesService.verifierDefuntAActe(defuntId)
        return ResponseEntity.ok(mapOf(
            "defuntId" to defuntId,
            "aDejaActe" to aDejaActe,
            "peutCreerActe" to !aDejaActe
        ))
    }

    /**
     * Obtenir un acte en version simple
     * 
     * GET /api/actes-deces/{id}/simple
     */
    @GetMapping("/{id}/simple")
    fun obtenirActeSimple(@PathVariable id: Long): ResponseEntity<ActeDecesSimple> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acteSimple = acteDecesService.obtenirActeSimple(id)
        return ResponseEntity.ok(acteSimple)
    }

    // ====== ENDPOINTS PAR SEXE ======

    /**
     * Récupérer tous les actes de défunts d'un sexe donné
     * 
     * GET /api/actes-deces/sexe/{sexe}
     */
    @GetMapping("/sexe/{sexe}")
    fun obtenirActesParSexe(
        @PathVariable sexe: Sexe,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size !in 1..100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParSexe(sexe, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe
     * 
     * GET /api/actes-deces/sexe/{sexe}/count
     */
    @GetMapping("/sexe/{sexe}/count")
    fun compterActesParSexe(@PathVariable sexe: Sexe): ResponseEntity<Map<String, Any>> {
        val count = acteDecesService.compterActesParSexe(sexe)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes de défunts d'un sexe dans une province
     * 
     * GET /api/actes-deces/sexe/{sexe}/province/{provinceId}
     */
    @GetMapping("/sexe/{sexe}/province/{provinceId}")
    fun obtenirActesParSexeEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParSexeEtProvince(sexe, provinceId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une province
     * 
     * GET /api/actes-deces/sexe/{sexe}/province/{provinceId}/count
     */
    @GetMapping("/sexe/{sexe}/province/{provinceId}/count")
    fun compterActesParSexeEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParSexeEtProvince(sexe, provinceId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes de défunts d'un sexe dans une entité
     * 
     * GET /api/actes-deces/sexe/{sexe}/entite/{entiteId}
     */
    @GetMapping("/sexe/{sexe}/entite/{entiteId}")
    fun obtenirActesParSexeEtEntite(
        @PathVariable sexe: Sexe,
        @PathVariable entiteId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParSexeEtEntite(sexe, entiteId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une entité
     * 
     * GET /api/actes-deces/sexe/{sexe}/entite/{entiteId}/count
     */
    @GetMapping("/sexe/{sexe}/entite/{entiteId}/count")
    fun compterActesParSexeEtEntite(
        @PathVariable sexe: Sexe,
        @PathVariable entiteId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParSexeEtEntite(sexe, entiteId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "entiteId" to entiteId,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes de défunts d'un sexe dans une commune
     * 
     * GET /api/actes-deces/sexe/{sexe}/commune/{communeId}
     */
    @GetMapping("/sexe/{sexe}/commune/{communeId}")
    fun obtenirActesParSexeEtCommune(
        @PathVariable sexe: Sexe,
        @PathVariable communeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeDecesSimple>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.obtenirActesParSexeEtCommune(sexe, communeId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une commune
     * 
     * GET /api/actes-deces/sexe/{sexe}/commune/{communeId}/count
     */
    @GetMapping("/sexe/{sexe}/commune/{communeId}/count")
    fun compterActesParSexeEtCommune(
        @PathVariable sexe: Sexe,
        @PathVariable communeId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        val count = acteDecesService.compterActesParSexeEtCommune(sexe, communeId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "communeId" to communeId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS DE STATISTIQUES ======

    /**
     * Obtenir les statistiques générales
     * 
     * GET /api/actes-deces/statistiques
     */
    @GetMapping("/statistiques")
    fun obtenirStatistiques(): ResponseEntity<ActeDecesStatistiques> {
        val statistiques = acteDecesService.obtenirStatistiques()
        return ResponseEntity.ok(statistiques)
    }

    // ====== ENDPOINTS DE RECHERCHE MULTICRITÈRES ======

    /**
     * Recherche multicritères avancée
     * 
     * POST /api/actes-deces/recherche
     */
    @PostMapping("/recherche")
    fun rechercherActes(@Valid @RequestBody criteria: ActeDecesSearchCriteria): ResponseEntity<Page<ActeDecesSimple>> {
        if (criteria.page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (criteria.size <= 0 || criteria.size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteDecesService.rechercherActes(criteria)
        return ResponseEntity.ok(actes)
    }
}
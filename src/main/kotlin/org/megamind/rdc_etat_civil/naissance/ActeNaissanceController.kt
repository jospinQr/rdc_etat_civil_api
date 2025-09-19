package org.megamind.rdc_etat_civil.naissance

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.naissance.dto.*
import org.megamind.rdc_etat_civil.naissance.pdf.ActeNaissancePdfService
import org.megamind.rdc_etat_civil.personne.Sexe
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/actes-naissance")
class ActeNaissanceController(
    private val acteNaissanceService: ActeNaissanceService,
    private val acteNaissancePdfService: ActeNaissancePdfService
) {

    // ====== ENDPOINTS CRUD ======

    /**
     * Créer un nouvel acte de naissance
     * 
     * POST /api/actes-naissance
     */
    @PostMapping
    fun creerActe(@Valid @RequestBody request: ActeNaissanceRequest): ResponseEntity<ActeNaissanceResponse> {
        // Validations métier de base
        if (request.enfantId <= 0) {
            throw BadRequestException("L'ID de l'enfant doit être un nombre positif")
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
        
        // Validation de la date d'enregistrement
        if (request.dateEnregistrement.isAfter(LocalDate.now())) {
            throw BadRequestException("La date d'enregistrement ne peut pas être dans le futur")
        }
        
        val acteCreee = acteNaissanceService.creerActeNaissance(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(acteCreee)
    }

    /**
     * Récupérer un acte par son ID
     * 
     * GET /api/actes-naissance/{id}
     */
    @GetMapping("/{id}")
    fun obtenirActeParId(@PathVariable id: Long): ResponseEntity<ActeNaissanceResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acte = acteNaissanceService.obtenirActeNaissance(id)
        return ResponseEntity.ok(acte)
    }

    /**
     * Mettre à jour un acte de naissance
     * 
     * PUT /api/actes-naissance/{id}
     */
    @PutMapping("/{id}")
    fun mettreAJourActe(
        @PathVariable id: Long,
        @Valid @RequestBody request: ActeNaissanceUpdateRequest
    ): ResponseEntity<ActeNaissanceResponse> {
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
        
        val acteModifie = acteNaissanceService.modifierActeNaissance(id, request)
        return ResponseEntity.ok(acteModifie)
    }

    /**
     * Supprimer un acte de naissance
     * 
     * DELETE /api/actes-naissance/{id}
     */
    @DeleteMapping("/{id}")
    fun supprimerActe(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        acteNaissanceService.supprimerActeNaissance(id)
        return ResponseEntity.ok(mapOf("message" to "Acte de naissance supprimé avec succès"))
    }

    // ====== ENDPOINTS DE RECHERCHE ======

    /**
     * Lister tous les actes avec pagination
     * 
     * GET /api/actes-naissance
     */
    @GetMapping
    fun listerActes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.listerActesNaissance(page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Rechercher par numéro d'acte
     * 
     * GET /api/actes-naissance/numero/{numeroActe}
     */
    @GetMapping("/numero/{numeroActe}")
    fun rechercherParNumero(@PathVariable numeroActe: String): ResponseEntity<ActeNaissanceResponse> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val acte = acteNaissanceService.rechercherParNumeroActe(numeroActe)
            ?: throw EntityNotFoundException("Aucun acte trouvé avec le numéro: $numeroActe")
        
        return ResponseEntity.ok(acte)
    }

    /**
     * Rechercher par nom de l'enfant
     * 
     * GET /api/actes-naissance/enfant/nom
     */
    @GetMapping("/enfant/nom")
    fun rechercherParNomEnfant(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
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
        
        val actes = acteNaissanceService.rechercherParNomEnfant(terme, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Rechercher l'acte d'un enfant par son ID
     * 
     * GET /api/actes-naissance/enfant/{enfantId}
     */
    @GetMapping("/enfant/{enfantId}")
    fun obtenirActeParEnfant(@PathVariable enfantId: Long): ResponseEntity<ActeNaissanceResponse> {
        if (enfantId <= 0) {
            throw BadRequestException("L'ID de l'enfant doit être un nombre positif")
        }
        
        val acte = acteNaissanceService.obtenirActeParEnfant(enfantId)
            ?: throw EntityNotFoundException("Aucun acte de naissance trouvé pour l'enfant avec l'ID: $enfantId")
        
        return ResponseEntity.ok(acte)
    }

    /**
     * Rechercher les actes d'une commune
     * 
     * GET /api/actes-naissance/commune/{communeId}
     */
    @GetMapping("/commune/{communeId}")
    fun obtenirActesParCommune(
        @PathVariable communeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParCommune(communeId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une commune
     * 
     * GET /api/actes-naissance/commune/{communeId}/count
     */
    @GetMapping("/commune/{communeId}/count")
    fun compterActesParCommune(@PathVariable communeId: Long): ResponseEntity<Map<String, Any>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParCommune(communeId)
        return ResponseEntity.ok(mapOf(
            "communeId" to communeId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une entité
     * 
     * GET /api/actes-naissance/entite/{entiteId}
     */
    @GetMapping("/entite/{entiteId}")
    fun obtenirActesParEntite(
        @PathVariable entiteId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParEntite(entiteId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une entité
     * 
     * GET /api/actes-naissance/entite/{entiteId}/count
     */
    @GetMapping("/entite/{entiteId}/count")
    fun compterActesParEntite(@PathVariable entiteId: Long): ResponseEntity<Map<String, Any>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParEntite(entiteId)
        return ResponseEntity.ok(mapOf(
            "entiteId" to entiteId,
            "totalActes" to count
        ))
    }

    /**
     * Rechercher tous les actes d'une province
     * 
     * GET /api/actes-naissance/province/{provinceId}
     */
    @GetMapping("/province/{provinceId}")
    fun obtenirActesParProvince(
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParProvince(provinceId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Obtenir le nombre total d'actes d'une province
     * 
     * GET /api/actes-naissance/province/{provinceId}/count
     */
    @GetMapping("/province/{provinceId}/count")
    fun compterActesParProvince(@PathVariable provinceId: Long): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParProvince(provinceId)
        return ResponseEntity.ok(mapOf(
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS DE TRAITEMENT EN LOT ======

    /**
     * Créer plusieurs actes de naissance en lot
     * 
     * POST /api/actes-naissance/lot
     */
    @PostMapping("/lot")
    fun creerActesEnLot(@Valid @RequestBody request: ActeNaissanceBatchRequest): ResponseEntity<ActeNaissanceBatchResponse> {
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
        
        val response = acteNaissanceService.creerActesEnLot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Valider un lot d'actes avant traitement
     * 
     * POST /api/actes-naissance/lot/validation
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
        
        val response = acteNaissanceService.validerLot(request)
        return ResponseEntity.ok(response)
    }

    // ====== ENDPOINTS DE VALIDATION ======

    /**
     * Vérifier si un numéro d'acte existe
     * 
     * GET /api/actes-naissance/verification/numero/{numeroActe}
     */
    @GetMapping("/verification/numero/{numeroActe}")
    fun verifierNumeroActe(@PathVariable numeroActe: String): ResponseEntity<Map<String, Any>> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        val existe = acteNaissanceService.verifierNumeroActe(numeroActe)
        return ResponseEntity.ok(mapOf(
            "numeroActe" to numeroActe,
            "existe" to existe,
            "disponible" to !existe
        ))
    }

    /**
     * Vérifier si un enfant a déjà un acte
     * 
     * GET /api/actes-naissance/verification/enfant/{enfantId}
     */
    @GetMapping("/verification/enfant/{enfantId}")
    fun verifierEnfantActe(@PathVariable enfantId: Long): ResponseEntity<Map<String, Any>> {
        if (enfantId <= 0) {
            throw BadRequestException("L'ID de l'enfant doit être un nombre positif")
        }
        
        val aDejaActe = acteNaissanceService.verifierEnfantAActe(enfantId)
        return ResponseEntity.ok(mapOf(
            "enfantId" to enfantId,
            "aDejaActe" to aDejaActe,
            "peutCreerActe" to !aDejaActe
        ))
    }

    /**
     * Obtenir un acte en version simple
     * 
     * GET /api/actes-naissance/{id}/simple
     */
    @GetMapping("/{id}/simple")
    fun obtenirActeSimple(@PathVariable id: Long): ResponseEntity<ActeNaissanceSimple> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val acteSimple = acteNaissanceService.obtenirActeSimple(id)
        return ResponseEntity.ok(acteSimple)
    }

    // ====== ENDPOINTS PAR SEXE ======

    /**
     * Récupérer tous les actes d'enfants d'un sexe donné
     * 
     * GET /api/actes-naissance/sexe/{sexe}
     */
    @GetMapping("/sexe/{sexe}")
    fun obtenirActesParSexe(
        @PathVariable sexe: Sexe,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size !in 1..100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParSexe(sexe, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe
     * 
     * GET /api/actes-naissance/sexe/{sexe}/count
     */
    @GetMapping("/sexe/{sexe}/count")
    fun compterActesParSexe(@PathVariable sexe: Sexe): ResponseEntity<Map<String, Any>> {
        val count = acteNaissanceService.compterActesParSexe(sexe)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes d'enfants d'un sexe dans une province
     * 
     * GET /api/actes-naissance/sexe/{sexe}/province/{provinceId}
     */
    @GetMapping("/sexe/{sexe}/province/{provinceId}")
    fun obtenirActesParSexeEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParSexeEtProvince(sexe, provinceId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une province
     * 
     * GET /api/actes-naissance/sexe/{sexe}/province/{provinceId}/count
     */
    @GetMapping("/sexe/{sexe}/province/{provinceId}/count")
    fun compterActesParSexeEtProvince(
        @PathVariable sexe: Sexe,
        @PathVariable provinceId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (provinceId <= 0) {
            throw BadRequestException("L'ID de la province doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParSexeEtProvince(sexe, provinceId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "provinceId" to provinceId,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes d'enfants d'un sexe dans une entité
     * 
     * GET /api/actes-naissance/sexe/{sexe}/entite/{entiteId}
     */
    @GetMapping("/sexe/{sexe}/entite/{entiteId}")
    fun obtenirActesParSexeEtEntite(
        @PathVariable sexe: Sexe,
        @PathVariable entiteId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParSexeEtEntite(sexe, entiteId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une entité
     * 
     * GET /api/actes-naissance/sexe/{sexe}/entite/{entiteId}/count
     */
    @GetMapping("/sexe/{sexe}/entite/{entiteId}/count")
    fun compterActesParSexeEtEntite(
        @PathVariable sexe: Sexe,
        @PathVariable entiteId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (entiteId <= 0) {
            throw BadRequestException("L'ID de l'entité doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParSexeEtEntite(sexe, entiteId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "entiteId" to entiteId,
            "totalActes" to count
        ))
    }

    /**
     * Récupérer les actes d'enfants d'un sexe dans une commune
     * 
     * GET /api/actes-naissance/sexe/{sexe}/commune/{communeId}
     */
    @GetMapping("/sexe/{sexe}/commune/{communeId}")
    fun obtenirActesParSexeEtCommune(
        @PathVariable sexe: Sexe,
        @PathVariable communeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ActeNaissanceSimple>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val actes = acteNaissanceService.obtenirActesParSexeEtCommune(sexe, communeId, page, size)
        return ResponseEntity.ok(actes)
    }

    /**
     * Compter les actes par sexe dans une commune
     * 
     * GET /api/actes-naissance/sexe/{sexe}/commune/{communeId}/count
     */
    @GetMapping("/sexe/{sexe}/commune/{communeId}/count")
    fun compterActesParSexeEtCommune(
        @PathVariable sexe: Sexe,
        @PathVariable communeId: Long
    ): ResponseEntity<Map<String, Any>> {
        if (communeId <= 0) {
            throw BadRequestException("L'ID de la commune doit être un nombre positif")
        }
        
        val count = acteNaissanceService.compterActesParSexeEtCommune(sexe, communeId)
        return ResponseEntity.ok(mapOf(
            "sexe" to sexe,
            "communeId" to communeId,
            "totalActes" to count
        ))
    }

    // ====== ENDPOINTS PDF ======

    /**
     * Générer un PDF pour un acte de naissance
     * 
     * GET /api/actes-naissance/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    fun genererPdfActe(@PathVariable id: Long): ResponseEntity<ByteArray> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        // Récupérer l'acte complet
        val acteResponse = acteNaissanceService.obtenirActeNaissance(id)
        val acteComplet = ActeNaissanceCompletDto.fromActeNaissanceResponse(acteResponse)
        
        // Générer le PDF
        val pdfBytes = acteNaissancePdfService.generateActeNaissancePdf(acteComplet)
        
        // Préparer les headers pour le téléchargement
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDisposition(
            org.springframework.http.ContentDisposition.attachment()
                .filename("acte_naissance_${acteComplet.numeroActe.replace("/", "_")}.pdf")
                .build()
        )
        headers.contentLength = pdfBytes.size.toLong()
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }

    /**
     * Générer un PDF pour un acte de naissance par numéro d'acte
     * 
     * GET /api/actes-naissance/numero/{numeroActe}/pdf
     */
    @GetMapping("/numero/{numeroActe}/pdf")
    fun genererPdfActeParNumero(@PathVariable numeroActe: String): ResponseEntity<ByteArray> {
        if (numeroActe.isBlank()) {
            throw BadRequestException("Le numéro d'acte ne peut pas être vide")
        }
        
        // Récupérer l'acte par numéro
        val acteResponse = acteNaissanceService.rechercherParNumeroActe(numeroActe)
            ?: throw EntityNotFoundException("Aucun acte trouvé avec le numéro: $numeroActe")
        
        val acteComplet = ActeNaissanceCompletDto.fromActeNaissanceResponse(acteResponse)
        
        // Générer le PDF
        val pdfBytes = acteNaissancePdfService.generateActeNaissancePdf(acteComplet)
        
        // Préparer les headers pour le téléchargement
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDisposition(
            org.springframework.http.ContentDisposition.attachment()
                .filename("acte_naissance_${numeroActe.replace("/", "_")}.pdf")
                .build()
        )
        headers.contentLength = pdfBytes.size.toLong()
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }

    /**
     * Générer un PDF pour un acte de naissance d'un enfant
     * 
     * GET /api/actes-naissance/enfant/{enfantId}/pdf
     */
    @GetMapping("/enfant/{enfantId}/pdf")
    fun genererPdfActeParEnfant(@PathVariable enfantId: Long): ResponseEntity<ByteArray> {
        if (enfantId <= 0) {
            throw BadRequestException("L'ID de l'enfant doit être un nombre positif")
        }
        
        // Récupérer l'acte par enfant
        val acteResponse = acteNaissanceService.obtenirActeParEnfant(enfantId)
            ?: throw EntityNotFoundException("Aucun acte de naissance trouvé pour l'enfant avec l'ID: $enfantId")
        
        val acteComplet = ActeNaissanceCompletDto.fromActeNaissanceResponse(acteResponse)
        
        // Générer le PDF
        val pdfBytes = acteNaissancePdfService.generateActeNaissancePdf(acteComplet)
        
        // Préparer les headers pour le téléchargement
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDisposition(
            org.springframework.http.ContentDisposition.attachment()
                .filename("acte_naissance_enfant_${enfantId}.pdf")
                .build()
        )
        headers.contentLength = pdfBytes.size.toLong()
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }
}
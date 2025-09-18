package org.megamind.rdc_etat_civil.personne

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.personne.dto.*
import org.megamind.rdc_etat_civil.utils.PaginatedResponse
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/personnes")
@CrossOrigin(origins = ["*"])
class PersonneController(
    private val personneService: PersonneService
) {

    // ====== OPÉRATIONS CRUD DE BASE ======

    /**
     * POST /personnes - Créer une nouvelle personne
     */
    @PostMapping
    fun creerPersonne(@Valid @RequestBody request: PersonneRequest): ResponseEntity<PersonneResponse> {
        // Vérifier si une personne avec les mêmes informations existe déjà
        request.dateNaissance?.let { dateNaissance ->
            val prenomPourVerification = request.prenom ?: ""
            if (personneService.verifierDoublon(
                    request.nom, 
                    request.postnom, 
                    prenomPourVerification, 
                    dateNaissance
                )) {
                throw BadRequestException("Une personne avec le même nom, postnom, prénom et date de naissance existe déjà")
            }
        }

        // Vérifier que les parents existent s'ils sont spécifiés

        request.pereId?.let { pereId ->
            if (!personneService.personneExiste(pereId)) {
                throw EntityNotFoundException("Père avec l'ID $pereId non trouvé")
            }
        }
        
        request.mereId?.let { mereId ->
            if (!personneService.personneExiste(mereId)) {
                throw EntityNotFoundException("Mère avec l'ID $mereId non trouvée")
            }
        }
        
        // Validations métier simples
        if (request.pereId != null && request.mereId != null && request.pereId == request.mereId) {
            throw BadRequestException("Le père et la mère ne peuvent pas être la même personne")
        }
        
        val personne = personneService.creerPersonne(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(personne)
    }

    /**
     * POST /personnes/batch - Créer plusieurs personnes en une seule fois
     */
    @PostMapping("/batch")
    fun creerPersonnesEnLot(@Valid @RequestBody request: PersonneBatchRequest): ResponseEntity<PersonneBatchResponse> {
        // Validation de la liste des personnes
        if (request.personnes.isEmpty()) {
            throw BadRequestException("La liste des personnes ne peut pas être vide")
        }
        
        // Limitation du nombre de personnes pour éviter la surcharge
        if (request.personnes.size > 1000) {
            throw BadRequestException("Le nombre maximum de personnes par lot est de 1000. Reçu: ${request.personnes.size}")
        }
        
        // Validation rapide de chaque personne dans le lot
        request.personnes.forEachIndexed { index, personneRequest ->
            // Vérifier les champs obligatoires de base
            if (personneRequest.nom.isBlank()) {
                throw BadRequestException("Le nom est obligatoire pour la personne à l'index $index")
            }
            
            if (personneRequest.postnom.isBlank()) {
                throw BadRequestException("Le postnom est obligatoire pour la personne à l'index $index")
            }
            
            // Validation des IDs de parents s'ils sont spécifiés
            personneRequest.pereId?.let { pereId ->
                if (pereId <= 0) {
                    throw BadRequestException("L'ID du père doit être positif pour la personne à l'index $index")
                }
            }
            
            personneRequest.mereId?.let { mereId ->
                if (mereId <= 0) {
                    throw BadRequestException("L'ID de la mère doit être positif pour la personne à l'index $index")
                }
            }
            
            // Validation métier simple
            if (personneRequest.pereId != null && personneRequest.mereId != null && 
                personneRequest.pereId == personneRequest.mereId) {
                throw BadRequestException("Le père et la mère ne peuvent pas être la même personne pour la personne à l'index $index")
            }
        }
        
        val resultat = personneService.creerPersonnesEnLot(request)
        
        // Si toutes les personnes ont été créées avec succès, retourner 201 (CREATED)
        // Sinon, retourner 207 (MULTI_STATUS) pour indiquer un succès partiel
        val status = if (resultat.totalEchecs == 0) HttpStatus.CREATED else HttpStatus.MULTI_STATUS
        
        return ResponseEntity.status(status).body(resultat)
    }

    // ====== LISTING ET RECHERCHES (ROUTES SPÉCIFIQUES AVANT /{id}) ======

    /**
     * GET /personnes - Lister toutes les personnes avec pagination
     */
    @GetMapping
    fun listerPersonnes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val resultats = personneService.listerPersonnes(page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(resultats))
    }

    /**
     * GET /personnes/rechercher - Recherche globale par nom
     */
    @GetMapping("/rechercher")
    fun rechercherParNom(
        @RequestParam terme: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
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
        
        val resultats = personneService.rechercherParNom(terme, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(resultats))
    }

    /**
     * POST /personnes/recherche-avancee - Recherche multicritères
     */
    @PostMapping("/recherche-avancee")
    fun rechercheAvancee(
        @RequestBody criteria: PersonneSearchCriteria
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
        val resultats = personneService.rechercherPersonnes(criteria)
        return ResponseEntity.ok(PaginatedResponse.fromPage(resultats))
    }

    /**
     * GET /personnes/recherche-multicriteres - Recherche multicritères via GET
     */
    @GetMapping("/recherche-multicriteres")
    fun rechercheMulticriteresGet(
        @RequestParam(required = false) nom: String?,
        @RequestParam(required = false) postnom: String?,
        @RequestParam(required = false) prenom: String?,
        @RequestParam(required = false) sexe: Sexe?,
        @RequestParam(required = false) statut: StatutPersonne?,
        @RequestParam(required = false) situationMatrimoniale: SituationMatrimoniale?,
        @RequestParam(required = false) commune: String?,
        @RequestParam(required = false) lieuNaissance: String?,
        @RequestParam(required = false) dateNaissanceDebut: String?, // Format: YYYY-MM-DD
        @RequestParam(required = false) dateNaissanceFin: String?,
        @RequestParam(required = false) ageMin: Int?,
        @RequestParam(required = false) ageMax: Int?,
        @RequestParam(required = false) nationalite: String?,
        @RequestParam(required = false) profession: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "nom") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
        // Validation de la pagination
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        // Validation des âges
        if (ageMin != null && ageMin < 0) {
            throw BadRequestException("L'âge minimum ne peut pas être négatif")
        }
        
        if (ageMax != null && ageMax < 0) {
            throw BadRequestException("L'âge maximum ne peut pas être négatif")
        }
        
        if (ageMin != null && ageMax != null && ageMin > ageMax) {
            throw BadRequestException("L'âge minimum ne peut pas être supérieur à l'âge maximum")
        }
        
        // Validation et parsing des dates
        val dateDebutParsee = dateNaissanceDebut?.let { dateStr ->
            try {
                java.time.LocalDate.parse(dateStr)
            } catch (e: Exception) {
                throw BadRequestException("Format de date invalide pour dateNaissanceDebut. Utilisez le format YYYY-MM-DD")
            }
        }
        
        val dateFinParsee = dateNaissanceFin?.let { dateStr ->
            try {
                java.time.LocalDate.parse(dateStr)
            } catch (e: Exception) {
                throw BadRequestException("Format de date invalide pour dateNaissanceFin. Utilisez le format YYYY-MM-DD")
            }
        }
        
        // Validation de la logique des dates
        if (dateDebutParsee != null && dateFinParsee != null && dateDebutParsee.isAfter(dateFinParsee)) {
            throw BadRequestException("La date de début ne peut pas être postérieure à la date de fin")
        }
        
        // Validation du tri
        val sortDirectionUpper = sortDirection.uppercase()
        if (sortDirectionUpper !in listOf("ASC", "DESC")) {
            throw BadRequestException("Direction de tri invalide: $sortDirection. Valeurs autorisées: ASC, DESC")
        }

        val criteria = PersonneSearchCriteria(
            nom = nom,
            postnom = postnom,
            prenom = prenom,
            sexe = sexe,
            statut = statut,
            situationMatrimoniale = situationMatrimoniale,
            commune = commune,
            lieuNaissance = lieuNaissance,
            dateNaissanceDebut = dateDebutParsee,
            dateNaissanceFin = dateFinParsee,
            ageMin = ageMin,
            ageMax = ageMax,
            nationalite = nationalite,
            profession = profession,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirectionUpper
        )

        val resultats = personneService.rechercherPersonnes(criteria)
        return ResponseEntity.ok(PaginatedResponse.fromPage(resultats))
    }

    /**
     * GET /personnes/statistiques/generales - Statistiques générales
     */
    @GetMapping("/statistiques/generales")
    fun obtenirStatistiquesGenerales(): ResponseEntity<Map<String, Any>> {
        val statistiques = personneService.obtenirStatistiquesGenerales()
        return ResponseEntity.ok(statistiques)
    }

    /**
     * GET /personnes/statistiques/par-commune - Statistiques par commune
     */
    @GetMapping("/statistiques/par-commune")
    fun obtenirStatistiquesParCommune(): ResponseEntity<List<Map<String, Any>>> {
        val statistiques = personneService.obtenirStatistiquesParCommune()
        return ResponseEntity.ok(statistiques)
    }

    /**
     * GET /personnes/statistiques/par-sexe - Statistiques par sexe
     */
    @GetMapping("/statistiques/par-sexe")
    fun obtenirStatistiquesParSexe(): ResponseEntity<List<Map<String, Any>>> {
        val statistiques = personneService.obtenirStatistiquesParSexe()
        return ResponseEntity.ok(statistiques)
    }

    /**
     * GET /personnes/enums - Obtenir les énumérations disponibles
     */
    @GetMapping("/enums")
    fun obtenirEnums(): ResponseEntity<Map<String, List<String>>> {
        return ResponseEntity.ok(
            mapOf(
            "sexe" to Sexe.entries.map { it.name },
            "statutPersonne" to StatutPersonne.entries.map { it.name },
            "situationMatrimoniale" to SituationMatrimoniale.entries.map { it.name }
        ))
    }

    // ====== OPÉRATIONS CRUD SUR PERSONNE SPÉCIFIQUE (ROUTES AVEC {id}) ======

    /**
     * GET /personnes/{id} - Obtenir une personne par ID
     */
    @GetMapping("id/{id}")
    fun obtenirPersonne(@PathVariable id: Long): ResponseEntity<PersonneResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        val personne = personneService.obtenirPersonne(id)
        return ResponseEntity.ok(personne)
    }

    /**
     * PUT /personnes/{id} - Modifier une personne
     */
    @PutMapping("/{id}")
    fun modifierPersonne(
        @PathVariable id: Long,
        @Valid @RequestBody request: PersonneRequest
    ): ResponseEntity<PersonneResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        // Vérifier que les parents existent s'ils sont spécifiés
        request.pereId?.let { pereId ->
            if (pereId == id) {
                throw BadRequestException("Une personne ne peut pas être son propre père")
            }
            if (!personneService.personneExiste(pereId)) {
                throw EntityNotFoundException("Père avec l'ID $pereId non trouvé")
            }
        }
        
        request.mereId?.let { mereId ->
            if (mereId == id) {
                throw BadRequestException("Une personne ne peut pas être sa propre mère")
            }
            if (!personneService.personneExiste(mereId)) {
                throw EntityNotFoundException("Mère avec l'ID $mereId non trouvée")
            }
        }
        
        val personne = personneService.modifierPersonne(id, request)
        return ResponseEntity.ok(personne)
    }

    /**
     * DELETE /personnes/{id} - Supprimer une personne
     */
    @DeleteMapping("/{id}")
    fun supprimerPersonne(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        personneService.supprimerPersonne(id)
        return ResponseEntity.ok(mapOf("message" to "Personne supprimée avec succès"))
    }

    /**
     * GET /personnes/{id}/enfants - Obtenir les enfants d'une personne
     */
    @GetMapping("/{id}/enfants")
    fun obtenirEnfants(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        if (page < 0) {
            throw BadRequestException("Le numéro de page ne peut pas être négatif")
        }
        
        if (size <= 0 || size > 100) {
            throw BadRequestException("La taille de page doit être entre 1 et 100")
        }
        
        val enfants = personneService.obtenirEnfants(id, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(enfants))
    }

    /**
     * GET /personnes/{id}/simple - Obtenir une version simplifiée d'une personne
     */
    @GetMapping("/{id}/simple")
    fun obtenirPersonneSimple(@PathVariable id: Long): ResponseEntity<PersonneSimple> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        val personne = personneService.obtenirPersonneSimple(id)
        return ResponseEntity.ok(personne)
    }

    /**
     * PUT /personnes/{id}/statut - Changer le statut d'une personne
     */
    @PutMapping("/{id}/statut")
    fun changerStatut(
        @PathVariable id: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<PersonneResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        val statutString = request["statut"] ?: throw BadRequestException("Le statut est requis")
        
        val nouveauStatut = try {
            StatutPersonne.valueOf(statutString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Statut invalide: $statutString. Valeurs autorisées: ${StatutPersonne.values().joinToString()}")
        }
        
        val personne = personneService.changerStatut(id, nouveauStatut)
        return ResponseEntity.ok(personne)
    }

    /**
     * PUT /personnes/{id}/situation-matrimoniale - Changer la situation matrimoniale
     */
    @PutMapping("/{id}/situation-matrimoniale")
    fun changerSituationMatrimoniale(
        @PathVariable id: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<PersonneResponse> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        if (!personneService.personneExiste(id)) {
            throw EntityNotFoundException("Personne avec l'ID $id non trouvée")
        }
        
        val situationString = request["situationMatrimoniale"] 
            ?: throw BadRequestException("La situation matrimoniale est requise")
        
        val nouvelleSituation = try {
            SituationMatrimoniale.valueOf(situationString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Situation matrimoniale invalide: $situationString. Valeurs autorisées: ${SituationMatrimoniale.values().joinToString()}")
        }
        
        val personne = personneService.changerSituationMatrimoniale(id, nouvelleSituation)
        return ResponseEntity.ok(personne)
    }

    /**
     * GET /personnes/{id}/existe - Vérifier si une personne existe
     */
    @GetMapping("/{id}/existe")
    fun personneExiste(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        if (id <= 0) {
            throw BadRequestException("L'ID doit être un nombre positif")
        }
        
        val existe = personneService.personneExiste(id)
        return ResponseEntity.ok(mapOf("existe" to existe))
    }

}
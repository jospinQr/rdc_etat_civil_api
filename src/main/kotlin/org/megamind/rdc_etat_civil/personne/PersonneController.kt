package org.megamind.rdc_etat_civil.personne

import jakarta.validation.Valid
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

    // ====== OPÉRATIONS CRUD ======

    /**
     * POST /personnes - Créer une nouvelle personne
     */
    @PostMapping
    fun creerPersonne(@Valid @RequestBody request: PersonneRequest): ResponseEntity<PersonneResponse> {
        val personne = personneService.creerPersonne(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(personne)
    }

    /**
     * POST /personnes/batch - Créer plusieurs personnes en une seule fois
     */
    @PostMapping("/batch")
    fun creerPersonnesEnLot(@Valid @RequestBody request: PersonneBatchRequest): ResponseEntity<PersonneBatchResponse> {
        val resultat = personneService.creerPersonnesEnLot(request)
        
        // Si toutes les personnes ont été créées avec succès, retourner 201 (CREATED)
        // Sinon, retourner 207 (MULTI_STATUS) pour indiquer un succès partiel
        val status = if (resultat.totalEchecs == 0) HttpStatus.CREATED else HttpStatus.MULTI_STATUS
        
        return ResponseEntity.status(status).body(resultat)
    }

    /**
     * GET /personnes/{id} - Obtenir une personne par ID
     */
    @GetMapping("/{id}")
    fun obtenirPersonne(@PathVariable id: Long): ResponseEntity<PersonneResponse> {
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
        val personne = personneService.modifierPersonne(id, request)
        return ResponseEntity.ok(personne)
    }

    /**
     * DELETE /personnes/{id} - Supprimer une personne
     */
    @DeleteMapping("/{id}")
    fun supprimerPersonne(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        personneService.supprimerPersonne(id)
        return ResponseEntity.ok(mapOf("message" to "Personne supprimée avec succès"))
    }

    // ====== LISTING ET RECHERCHES ======

    /**
     * GET /personnes - Lister toutes les personnes avec pagination
     */
    @GetMapping
    fun listerPersonnes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
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

        val criteria = PersonneSearchCriteria(
            nom = nom,
            postnom = postnom,
            prenom = prenom,
            sexe = sexe,
            statut = statut,
            situationMatrimoniale = situationMatrimoniale,
            commune = commune,
            lieuNaissance = lieuNaissance,
            dateNaissanceDebut = dateNaissanceDebut?.let { java.time.LocalDate.parse(it) },
            dateNaissanceFin = dateNaissanceFin?.let { java.time.LocalDate.parse(it) },
            ageMin = ageMin,
            ageMax = ageMax,
            nationalite = nationalite,
            profession = profession,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val resultats = personneService.rechercherPersonnes(criteria)
        return ResponseEntity.ok(PaginatedResponse.fromPage(resultats))
    }

    // ====== RELATIONS FAMILIALES ======

    /**
     * GET /personnes/{id}/enfants - Obtenir les enfants d'une personne
     */
    @GetMapping("/{id}/enfants")
    fun obtenirEnfants(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedResponse<PersonneResponse>> {
        val enfants = personneService.obtenirEnfants(id, page, size)
        return ResponseEntity.ok(PaginatedResponse.fromPage(enfants))
    }

    /**
     * GET /personnes/{id}/simple - Obtenir une version simplifiée d'une personne
     */
    @GetMapping("/{id}/simple")
    fun obtenirPersonneSimple(@PathVariable id: Long): ResponseEntity<PersonneSimple> {
        val personne = personneService.obtenirPersonneSimple(id)
        return ResponseEntity.ok(personne)
    }

    // ====== GESTION DES STATUTS ======

    /**
     * PUT /personnes/{id}/statut - Changer le statut d'une personne
     */
    @PutMapping("/{id}/statut")
    fun changerStatut(
        @PathVariable id: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<PersonneResponse> {
        val nouveauStatut = StatutPersonne.valueOf(request["statut"] ?: throw IllegalArgumentException("Statut requis"))
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
        val nouvelleSituation = SituationMatrimoniale.valueOf(
            request["situationMatrimoniale"] ?: throw IllegalArgumentException("Situation matrimoniale requise")
        )
        val personne = personneService.changerSituationMatrimoniale(id, nouvelleSituation)
        return ResponseEntity.ok(personne)
    }

    // ====== STATISTIQUES ======

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


    // ====== UTILITAIRES ======

    /**
     * GET /personnes/{id}/existe - Vérifier si une personne existe
     */
    @GetMapping("/{id}/existe")
    fun personneExiste(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        val existe = personneService.personneExiste(id)
        return ResponseEntity.ok(mapOf("existe" to existe))
    }

    /**
     * GET /personnes/enums - Obtenir les énumérations disponibles
     */
    @GetMapping("/enums")
    fun obtenirEnums(): ResponseEntity<Map<String, List<String>>> {
        return ResponseEntity.ok(
            mapOf(
            "sexe" to Sexe.values().map { it.name },
            "statutPersonne" to StatutPersonne.values().map { it.name },
            "situationMatrimoniale" to SituationMatrimoniale.values().map { it.name }
        ))
    }


}

package org.megamind.rdc_etat_civil.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.personne.*
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.megamind.rdc_etat_civil.personne.dto.PersonneSearchCriteria
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tests d'intégration pour PersonneController
 * Confirme que toutes les routes fonctionnent correctement après réorganisation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class PersonneControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    @Autowired
    private lateinit var provinceRepository: ProvinceRepository

    @Autowired
    private lateinit var entiteRepository: EntiteRepository

    @Autowired
    private lateinit var communeRepository: CommuneRepository

    private lateinit var mockMvc: MockMvc
    private lateinit var province: Province
    private lateinit var entite: Entite
    private lateinit var commune: Commune

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()

        // Nettoyer la base de données (ordre important pour les FK)
        personneRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()

        // Créer structure géographique de test
        province = provinceRepository.save(Province(designation = "Kinshasa"))
        entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        commune = communeRepository.save(Commune(designation = "Gombe", entite = entite))

        // Créer quelques personnes de test
        personneRepository.saveAll(listOf(
            Personne(
                nom = "KABILA",
                postnom = "JOSEPH",
                prenom = "Laurent",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(1971, 6, 4),
                lieuNaiss = "Lubumbashi",
                statut = StatutPersonne.VIVANT,
                situationMatrimoniale = SituationMatrimoniale.MARIE,
                communeChefferie = "Gombe"
            ),
            Personne(
                nom = "TSHISEKEDI",
                postnom = "FELIX",
                prenom = "Antoine",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(1962, 6, 13),
                lieuNaiss = "Léopoldville",
                statut = StatutPersonne.VIVANT,
                situationMatrimoniale = SituationMatrimoniale.MARIE,
                communeChefferie = "Kalamu"
            ),
            Personne(
                nom = "MUKENDI",
                postnom = "MARIE",
                prenom = "Grace",
                sexe = Sexe.FEMININ,
                dateNaissance = LocalDate.of(1985, 3, 15),
                lieuNaiss = "Kinshasa",
                statut = StatutPersonne.VIVANT,
                situationMatrimoniale = SituationMatrimoniale.CELIBATAIRE,
                communeChefferie = "Gombe"
            )
        ))
    }

    // ====== TESTS DES ROUTES PROBLÉMATIQUES (PRÉCÉDEMMENT EN CONFLIT) ======

    @Test
    fun `test POST recherche-avancee - route spécifique fonctionne maintenant`() {
        // Given - Critères de recherche via POST
        val criteria = PersonneSearchCriteria(
            nom = "KABILA",
            sexe = Sexe.MASCULIN,
            page = 0,
            size = 10,
            sortBy = "nom",
            sortDirection = "ASC"
        )

        // When & Then - POST /personnes/recherche-avancee doit fonctionner
        mockMvc.perform(
            post("/personnes/recherche-avancee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteria))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].nom").value("KABILA"))
            .andExpect(jsonPath("$.content[0].postnom").value("JOSEPH"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `test GET recherche-multicriteres - route spécifique fonctionne maintenant`() {
        // When & Then - GET /personnes/recherche-multicriteres doit fonctionner
        mockMvc.perform(
            get("/personnes/recherche-multicriteres")
                .param("nom", "TSHISEKEDI")
                .param("sexe", "MASCULIN")
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].nom").value("TSHISEKEDI"))
            .andExpect(jsonPath("$.content[0].postnom").value("FELIX"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `test GET statistiques-generales - route spécifique fonctionne`() {
        // When & Then - GET /personnes/statistiques/generales doit fonctionner
        mockMvc.perform(get("/personnes/statistiques/generales"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalPersonnes").exists())
            .andExpect(jsonPath("$.totalHommes").exists())

    }

    @Test
    fun `test GET enums - route spécifique fonctionne`() {
        // When & Then - GET /personnes/enums doit fonctionner
        mockMvc.perform(get("/personnes/enums"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sexe").isArray)
            .andExpect(jsonPath("$.sexe[0]").value("MASCULIN"))
            .andExpect(jsonPath("$.sexe[1]").value("FEMININ"))
            .andExpect(jsonPath("$.statutPersonne").isArray)
            .andExpect(jsonPath("$.situationMatrimoniale").isArray)
    }

    // ====== TESTS DES ROUTES GÉNÉRIQUES (AVEC {id}) ======

    @Test
    fun `test GET personnes par ID - route générique continue de fonctionner`() {
        // Given - Récupérer ID d'une personne existante
        val personne = personneRepository.findAll().first()

        // When & Then - GET /personnes/{id} doit continuer de fonctionner
        mockMvc.perform(get("/personnes/id/${personne.id}"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(personne.id))
            .andExpect(jsonPath("$.nom").value(personne.nom))
            .andExpect(jsonPath("$.postnom").value(personne.postnom))
    }

    @Test
    fun `test GET personnes ID existe - route avec ID fonctionne`() {
        // Given
        val personne = personneRepository.findAll().first()

        // When & Then - GET /personnes/{id}/existe
        mockMvc.perform(get("/personnes/${personne.id}/existe"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.existe").value(true))
    }

    @Test
    fun `test GET personnes ID enfants - route avec ID fonctionne`() {
        // Given
        val personne = personneRepository.findAll().first()

        // When & Then - GET /personnes/{id}/enfants
        mockMvc.perform(get("/personnes/${personne.id}/enfants"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").exists())
    }

    // ====== TESTS DE ROUTES DE LISTING ======

    @Test
    fun `test GET personnes - listing principal fonctionne`() {
        // When & Then - GET /personnes (sans paramètres)
        mockMvc.perform(get("/personnes"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(3)) // 3 personnes créées dans setUp
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
    }

    @Test
    fun `test GET personnes rechercher - recherche par nom fonctionne`() {
        // When & Then - GET /personnes/rechercher
        mockMvc.perform(
            get("/personnes/rechercher")
                .param("terme", "KABILA")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].nom").value("KABILA"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    // ====== TESTS DE CRÉATION ======

    @Test
    fun `test POST personnes - création simple fonctionne`() {
        // Given
        val nouvellePersonne = PersonneRequest(
            nom = "MULUMBA",
            postnom = "JEAN",
            prenom = "Claude",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(1990, 8, 20),
            lieuNaiss = "Bukavu",
            heureNaissance = LocalTime.of(14, 30),
            communeChefferie = "Gombe"
        )

        // When & Then - POST /personnes
        mockMvc.perform(
            post("/personnes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nouvellePersonne))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.nom").value("MULUMBA"))
            .andExpect(jsonPath("$.postnom").value("JEAN"))
            .andExpect(jsonPath("$.prenom").value("Claude"))
            .andExpect(jsonPath("$.id").exists())
    }

    // ====== TESTS D'ERREURS ET VALIDATION ======

    @Test
    fun `test GET personnes ID invalide - erreur appropriée`() {
        // When & Then - GET /personnes/{id} avec ID invalide
        mockMvc.perform(get("/personnes/99999"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Personne avec l'ID 99999 non trouvée"))
    }

    @Test
    fun `test POST recherche-avancee - validation des critères`() {
        // Given - Critères invalides (âge minimum > âge maximum)
        val criteresInvalides = PersonneSearchCriteria(
            ageMin = 50,
            ageMax = 30, // Invalide !
            page = 0,
            size = 10
        )

        // When & Then - Doit retourner erreur de validation
        mockMvc.perform(
            post("/personnes/recherche-avancee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteresInvalides))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test GET recherche-multicriteres - validation dates`() {
        // When & Then - Dates invalides
        mockMvc.perform(
            get("/personnes/recherche-multicriteres")
                .param("dateNaissanceDebut", "2000-01-01")
                .param("dateNaissanceFin", "1990-01-01") // Fin avant début !
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("La date de début ne peut pas être postérieure à la date de fin"))
    }

    // ====== TEST DE ROUTE COMPLEXE AVEC PARAMÈTRES MULTIPLES ======

    @Test
    fun `test GET recherche-multicriteres avec tous les paramètres`() {
        // When & Then - Recherche complexe avec tous les paramètres
        mockMvc.perform(
            get("/personnes/recherche-multicriteres")
                .param("nom", "MUKENDI")
                .param("sexe", "FEMININ")
                .param("statut", "VIVANT")
                .param("situationMatrimoniale", "CELIBATAIRE")
                .param("commune", "Gombe")
                .param("dateNaissanceDebut", "1980-01-01")
                .param("dateNaissanceFin", "1990-12-31")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "nom")
                .param("sortDirection", "DESC")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].nom").value("MUKENDI"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    // ====== TEST DE CONFIRMATION QUE LES CONFLITS SONT RÉSOLUS ======

    @Test
    fun `test confirmation - toutes les routes spécifiques avant ID fonctionnent`() {
        // Test en séquence de toutes les routes qui avaient des conflits

        // 1. recherche-avancee (POST)
        val criteria = PersonneSearchCriteria(nom = "KABILA")
        mockMvc.perform(
            post("/personnes/recherche-avancee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteria))
        ).andExpect(status().isOk)

        // 2. recherche-multicriteres (GET)
        mockMvc.perform(
            get("/personnes/recherche-multicriteres")
                .param("nom", "TSHISEKEDI")
        ).andExpect(status().isOk)

        // 3. statistiques/generales
        mockMvc.perform(get("/personnes/statistiques/generales"))
            .andExpect(status().isOk)

        // 4. enums
        mockMvc.perform(get("/personnes/enums"))
            .andExpect(status().isOk)

        // 5. Et enfin, les routes avec {id} continuent de fonctionner
        val personne = personneRepository.findAll().first()
        mockMvc.perform(get("/personnes/id/${personne.id}"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/personnes/${personne.id}/existe"))
            .andExpect(status().isOk)
    }
}

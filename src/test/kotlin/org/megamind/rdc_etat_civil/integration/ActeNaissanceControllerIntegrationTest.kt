package org.megamind.rdc_etat_civil.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.naissance.ActeNaissanceRepository
import org.megamind.rdc_etat_civil.naissance.dto.*
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.Sexe
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

/**
 * Tests d'intégration pour ActeNaissanceController
 * Teste tous les endpoints du contrôleur avec des données réelles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ActeNaissanceControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var acteNaissanceRepository: ActeNaissanceRepository

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    @Autowired
    private lateinit var provinceRepository: ProvinceRepository

    @Autowired
    private lateinit var entiteRepository: EntiteRepository

    @Autowired
    private lateinit var communeRepository: CommuneRepository

    private lateinit var mockMvc: MockMvc

    // Données de test
    private lateinit var province: Province
    private lateinit var entite: Entite
    private lateinit var commune: Commune
    private lateinit var enfant: Personne

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()

        // Nettoyer les données de test
        acteNaissanceRepository.deleteAll()
        personneRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()

        // Créer les données de base
        province = Province(
            designation = "Kinshasa"
        )
        province = provinceRepository.save(province)

        entite = Entite(
            designation = "Kinshasa",
            estVille = true,
            province = province
        )
        entite = entiteRepository.save(entite)

        commune = Commune(
            designation = "Kinshasa",
            entite = entite
        )
        commune = communeRepository.save(commune)

        enfant = Personne(
            nom = "MUKAMBA",
            postnom = "KABONGO",
            prenom = "Jean",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2020, 3, 15)
        )
        enfant = personneRepository.save(enfant)
    }

    // ====== TESTS CRUD ======

    @Test
    fun `devrait créer un acte de naissance avec succès`() {
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/001",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.numeroActe").value("KIN/2024/001"))
            .andExpect(jsonPath("$.enfant.nom").value("MUKAMBA"))
            .andExpect(jsonPath("$.commune.nom").value("Kinshasa"))
            .andExpect(jsonPath("$.officier").value("KABONGO Jean-Pierre"))
    }

    @Test
    fun `devrait récupérer un acte par son ID`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/002",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        val response = mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val acteCree = objectMapper.readTree(response.response.contentAsString)
        val acteId = acteCree.get("id").asLong()

        // When & Then - Récupérer l'acte
        mockMvc.perform(get("/actes-naissance/$acteId"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(acteId))
            .andExpect(jsonPath("$.numeroActe").value("KIN/2024/002"))
    }

    @Test
    fun `devrait mettre à jour un acte de naissance`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/003",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        val response = mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val acteCree = objectMapper.readTree(response.response.contentAsString)
        val acteId = acteCree.get("id").asLong()

        // When - Mettre à jour l'acte
        val updateRequest = ActeNaissanceUpdateRequest(
            officier = "MWANZA Paul",
            dateEnregistrement = LocalDate.now().minusDays(1),
            declarant = "MUKAMBA Joseph",
            temoin1 = "KABONGO Grace",
            temoin2 = "MUKAMBA Sarah"
        )

        mockMvc.perform(
            put("/actes-naissance/$acteId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.officier").value("MWANZA Paul"))
            .andExpect(jsonPath("$.declarant").value("MUKAMBA Joseph"))
    }

    @Test
    fun `devrait supprimer un acte de naissance`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/004",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        val response = mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val acteCree = objectMapper.readTree(response.response.contentAsString)
        val acteId = acteCree.get("id").asLong()

        // When & Then - Supprimer l'acte
        mockMvc.perform(delete("/actes-naissance/$acteId"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Acte de naissance supprimé avec succès"))
    }

    // ====== TESTS DE VALIDATION ======

    @Test
    fun `devrait rejeter la création avec un ID d'enfant invalide`() {
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/005",
            enfantId = -1L, // ID invalide
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `devrait rejeter la création avec un numéro d'acte vide`() {
        val request = ActeNaissanceRequest(
            numeroActe = "", // Numéro vide
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `devrait rejeter la création avec une date d'enregistrement future`() {
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/006",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now().plusDays(1), // Date future
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    // ====== TESTS DE RECHERCHE ======

    @Test
    fun `devrait lister tous les actes avec pagination`() {
        // Given - Créer plusieurs actes
        repeat(3) { index ->
            val enfantUnique = Personne(
                nom = "MUKAMBA$index",
                postnom = "KABONGO$index",
                prenom = "Jean$index",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(2020, 3, 15 + index)
            )
            val savedEnfant = personneRepository.save(enfantUnique)

            val request = ActeNaissanceRequest(
                numeroActe = "KIN/2024/00${index + 1}",
                enfantId = savedEnfant.id!!,
                communeId = commune.id!!,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now(),
                declarant = "MUKAMBA Paul",
                temoin1 = "KABONGO Marie",
                temoin2 = "MUKAMBA Julie"
            )

            mockMvc.perform(
                post("/actes-naissance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
        }

        // When & Then - Lister les actes
        mockMvc.perform(get("/actes-naissance?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(3))
    }

    @Test
    fun `devrait rechercher par numéro d'acte`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "00007",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par numéro
        mockMvc.perform(get("/actes-naissance/numero/00007"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.numeroActe").value("00007"))
    }

    @Test
    fun `devrait rechercher par nom d'enfant`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/008",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par nom
        mockMvc.perform(get("/actes-naissance/enfant/nom?terme=MUKAMBA&page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `devrait rechercher par ID d'enfant`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/009",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par ID d'enfant
        mockMvc.perform(get("/actes-naissance/enfant/${enfant.id}"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enfant.id").value(enfant.id))
    }

    // ====== TESTS PAR TERRITOIRE ======

    @Test
    fun `devrait rechercher par commune`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/010",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par commune
        mockMvc.perform(get("/actes-naissance/commune/${commune.id}?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `devrait compter les actes par commune`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/011",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Compter par commune
        mockMvc.perform(get("/actes-naissance/commune/${commune.id}/count"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.communeId").value(commune.id))
            .andExpect(jsonPath("$.totalActes").value(1))
    }

    @Test
    fun `devrait rechercher par entité`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/012",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par entité
        mockMvc.perform(get("/actes-naissance/entite/${entite.id}?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `devrait rechercher par province`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/013",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par province
        mockMvc.perform(get("/actes-naissance/province/${province.id}?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    // ====== TESTS DE VALIDATION AVANCÉE ======

    @Test
    fun `devrait vérifier si un numéro d'acte existe`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "01400",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Vérifier l'existence du numéro
        mockMvc.perform(get("/actes-naissance/verification/numero/01400"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.numeroActe").value("01400"))
            .andExpect(jsonPath("$.existe").value(true))
            .andExpect(jsonPath("$.disponible").value(false))
    }

    @Test
    fun `devrait vérifier si un enfant a déjà un acte`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/015",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Vérifier si l'enfant a un acte
        mockMvc.perform(get("/actes-naissance/verification/enfant/${enfant.id}"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enfantId").value(enfant.id))
            .andExpect(jsonPath("$.aDejaActe").value(true))
            .andExpect(jsonPath("$.peutCreerActe").value(false))
    }

    // ====== TESTS PAR SEXE ======

    @Test
    fun `devrait rechercher les actes par sexe`() {
        // Given - Créer un acte pour un enfant masculin
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/016",
            enfantId = enfant.id!!, // enfant masculin
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par sexe masculin
        mockMvc.perform(get("/actes-naissance/sexe/MASCULIN?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `devrait compter les actes par sexe`() {
        // Given - Créer un acte pour un enfant masculin
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/017",
            enfantId = enfant.id!!, // enfant masculin
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Compter par sexe masculin
        mockMvc.perform(get("/actes-naissance/sexe/MASCULIN/count"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sexe").value("MASCULIN"))
            .andExpect(jsonPath("$.totalActes").value(1))
    }

    @Test
    fun `devrait rechercher les actes par sexe et commune`() {
        // Given - Créer un acte pour un enfant masculin
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/018",
            enfantId = enfant.id!!, // enfant masculin
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Rechercher par sexe et commune
        mockMvc.perform(get("/actes-naissance/sexe/MASCULIN/commune/${commune.id}?page=0&size=10"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `devrait compter les actes par sexe et commune`() {
        // Given - Créer un acte pour un enfant masculin
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/019",
            enfantId = enfant.id!!, // enfant masculin
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Compter par sexe et commune
        mockMvc.perform(get("/actes-naissance/sexe/MASCULIN/commune/${commune.id}/count"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sexe").value("MASCULIN"))
            .andExpect(jsonPath("$.communeId").value(commune.id))
            .andExpect(jsonPath("$.totalActes").value(1))
    }

    // ====== TESTS DE TRAITEMENT EN LOT ======

    @Test
    fun `devrait créer plusieurs actes en lot avec succès`() {
        // Given - Créer plusieurs enfants uniques
        val enfants = (1..3).map { index ->
            val enfantUnique = Personne(
                nom = "MUKAMBA$index",
                postnom = "KABONGO$index",
                prenom = "Jean$index",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(2020, 3, 15 + index)
            )
            personneRepository.save(enfantUnique)
        }

        val request = ActeNaissanceBatchRequest(
            actes = enfants.mapIndexed { index, enfant ->
                ActeNaissanceItemRequest(
                    numeroActe = "KIN/2024/BATCH${index + 1}",
                    enfantId = enfant.id!!,
                    communeId = commune.id!!,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now(),
                    declarant = "MUKAMBA Paul",
                    temoin1 = "KABONGO Marie",
                    temoin2 = "MUKAMBA Julie",
                    numeroOrdre = index + 1
                )
            },
            descriptionLot = "Lot de test pour 3 actes",
            responsableLot = "KABONGO Jean-Pierre",
            dateTraitement = LocalDate.now(),
            validationStricte = true
        )

        // When & Then
        mockMvc.perform(
            post("/actes-naissance/lot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.totalActes").value(3))
            .andExpect(jsonPath("$.actesReussis").value(3))
            .andExpect(jsonPath("$.actesEchecs").value(0))
            .andExpect(jsonPath("$.resultats").isArray)
            .andExpect(jsonPath("$.resultats.length()").value(3))
            .andExpect(jsonPath("$.resultats[0].success").value(true))
            .andExpect(jsonPath("$.resultats[0].numeroActe").value("KIN/2024/BATCH1"))
    }

    @Test
    fun `devrait rejeter un lot vide`() {
        val request = ActeNaissanceBatchRequest(
            actes = emptyList(),
            descriptionLot = "Lot vide",
            responsableLot = "KABONGO Jean-Pierre"
        )

        mockMvc.perform(
            post("/actes-naissance/lot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `devrait rejeter un lot avec des numéros d'acte dupliqués`() {
        // Given - Créer un enfant
        val enfant = Personne(
            nom = "MUKAMBA1",
            postnom = "KABONGO1",
            prenom = "Jean1",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2020, 3, 15)
        )
        val savedEnfant = personneRepository.save(enfant)

        val request = ActeNaissanceBatchRequest(
            actes = listOf(
                ActeNaissanceItemRequest(
                    numeroActe = "KIN/2024/DUPLICATE",
                    enfantId = savedEnfant.id!!,
                    communeId = commune.id!!,
                    officier = "KABONGO Jean-Pierre"
                ),
                ActeNaissanceItemRequest(
                    numeroActe = "KIN/2024/DUPLICATE", // Dupliqué !
                    enfantId = savedEnfant.id!!,
                    communeId = commune.id!!,
                    officier = "KABONGO Jean-Pierre"
                )
            ),
            descriptionLot = "Lot avec doublons",
            responsableLot = "KABONGO Jean-Pierre"
        )

        mockMvc.perform(
            post("/actes-naissance/lot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `devrait valider un lot avant traitement`() {
        // Given - Créer plusieurs enfants uniques
        val enfants = (1..2).map { index ->
            val enfantUnique = Personne(
                nom = "MUKAMBA$index",
                postnom = "KABONGO$index",
                prenom = "Jean$index",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(2020, 3, 15 + index)
            )
            personneRepository.save(enfantUnique)
        }

        val request = BatchValidationRequest(
            actes = enfants.mapIndexed { index, enfant ->
                ActeNaissanceItemRequest(
                    numeroActe = "KIN/2024/VALID${index + 1}",
                    enfantId = enfant.id!!,
                    communeId = commune.id!!,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now(),
                    declarant = "MUKAMBA Paul",
                    temoin1 = "KABONGO Marie",
                    temoin2 = "MUKAMBA Julie",
                    numeroOrdre = index + 1
                )
            },
            validationComplete = true
        )

        // When & Then
        mockMvc.perform(
            post("/actes-naissance/lot/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valide").value(true))
            .andExpect(jsonPath("$.nombreActes").value(2))
            .andExpect(jsonPath("$.erreursValidation").isArray)
            .andExpect(jsonPath("$.erreursValidation.length()").value(0))
    }

    // ====== TESTS PDF ======

    @Test
    fun `devrait générer un PDF pour un acte par ID`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/PDF001",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        val response = mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val acteCree = objectMapper.readTree(response.response.contentAsString)
        val acteId = acteCree.get("id").asLong()

        // When & Then - Générer le PDF
        mockMvc.perform(get("/actes-naissance/$acteId/pdf"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/pdf"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"acte_naissance_KIN_2024_PDF001.pdf\""))
    }

    @Test
    fun `devrait générer un PDF pour un acte par numéro`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "PDF002",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Générer le PDF par numéro
        mockMvc.perform(get("/actes-naissance/numero/PDF002/pdf"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/pdf"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"acte_naissance_PDF002.pdf\""))
    }

    @Test
    fun `devrait générer un PDF pour un acte par enfant`() {
        // Given - Créer un acte
        val request = ActeNaissanceRequest(
            numeroActe = "KIN/2024/PDF003",
            enfantId = enfant.id!!,
            communeId = commune.id!!,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now(),
            declarant = "MUKAMBA Paul",
            temoin1 = "KABONGO Marie",
            temoin2 = "MUKAMBA Julie"
        )

        mockMvc.perform(
            post("/actes-naissance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // When & Then - Générer le PDF par enfant
        mockMvc.perform(get("/actes-naissance/enfant/${enfant.id}/pdf"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/pdf"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"acte_naissance_enfant_${enfant.id}.pdf\""))
    }
}

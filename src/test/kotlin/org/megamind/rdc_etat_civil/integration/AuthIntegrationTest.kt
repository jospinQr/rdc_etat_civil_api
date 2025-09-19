package org.megamind.rdc_etat_civil.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.megamind.rdc_etat_civil.utlisat.Role
import org.megamind.rdc_etat_civil.utlisat.Utilisateur
import org.megamind.rdc_etat_civil.utlisateur.LoginRequest
import org.megamind.rdc_etat_civil.utlisateur.RegisterRequest
import org.megamind.rdc_etat_civil.utlisateur.UtilisateurRepository
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

/**
 * Test d'intégration pour vérifier que l'authentification fonctionne 
 * de bout en bout avec Spring Security et la base de données
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UtilisateurRepository

    @Autowired
    private lateinit var provinceRepository: ProvinceRepository

    @Autowired
    private lateinit var entiteRepository: EntiteRepository

    @Autowired
    private lateinit var communeRepository: CommuneRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        // Configurer MockMvc avec le contexte Spring complet (incluant Security)
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
            
        // Nettoyer la base de données (ordre important pour les FK)
        userRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()
        
        // Créer un utilisateur de test
        val testUser = Utilisateur(
            username = "testuser",
            password = passwordEncoder.encode("password123"),
            role = Role.ADMIN
        )
        userRepository.save(testUser)
    }

    @Test
    fun `test d'intégration - login complet avec vraie base de données`() {
        // Given
        val loginRequest = LoginRequest(
            username = "testuser",
            password = "password123",
            role = Role.ADMIN
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print()) // Pour débugger si nécessaire
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").isString)
            .andExpect(jsonPath("$.token").isNotEmpty)
    }

    @Test
    fun `test d'intégration - register puis login`() {
        // Given - Register
        val registerRequest = RegisterRequest(
            username = "newuser",
            password = "newpassword123",
            role = Role.ADMIN
        )

        // When - Register
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())

        // Then - Login with new user
        val loginRequest = LoginRequest(
            username = "newuser",
            password = "newpassword123",
            role = Role.ADMIN
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test d'intégration - login avec mauvais mot de passe`() {
        // Given
        val loginRequest = LoginRequest(
            username = "testuser",
            password = "newpassword123",
            role = Role.ADMIN
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test d'intégration - validation Jackson avec champs optionnels`() {
        // Given - JSON avec seulement les champs obligatoires (role ajouté)
        val minimalJson = """
            {
                "username": "testuser",
                "password": "password123",
                "role": "ADMIN"
            }
        """.trimIndent()

        // When & Then - Devrait fonctionner avec les champs optionnels à null
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(minimalJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test d'intégration - JSON incomplet doit échouer`() {
        // Given - JSON avec champ obligatoire manquant
        val incompleteJson = """
            {
                "username": "testuser",
                "password": "password123"
            }
        """.trimIndent()

        // When & Then - Devrait échouer car le role est manquant
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson)
        )
            .andExpect(status().isBadRequest)  // Erreur 400 attendue
    }

    // ====== TESTS DES RÔLES ET RESTRICTIONS GÉOGRAPHIQUES ======

    @Test
    fun `test login CD avec province correcte`() {
        // Given - Créer et sauvegarder la province d'abord
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        
        // Puis créer l'utilisateur CD
        val cdUser = Utilisateur(
            username = "cd_kinshasa",
            password = passwordEncoder.encode("password123"),
            role = Role.CD,
            province = province
        )
        userRepository.save(cdUser)

        val loginRequest = LoginRequest(
            username = "cd_kinshasa",
            password = "password123",
            role = Role.CD,
            province = province
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test login CD avec province incorrecte doit échouer`() {
        // Given - Créer et sauvegarder les provinces
        val provinceKinshasa = provinceRepository.save(Province(designation = "Kinshasa"))
        val provinceLubumbashi = provinceRepository.save(Province(designation = "Lubumbashi"))
        
        // Créer utilisateur CD de Kinshasa
        val cdUser = Utilisateur(
            username = "cd_kinshasa",
            password = passwordEncoder.encode("password123"),
            role = Role.CD,
            province = provinceKinshasa
        )
        userRepository.save(cdUser)

        val loginRequest = LoginRequest(
            username = "cd_kinshasa",
            password = "password123",
            role = Role.CD,
            province = provinceLubumbashi  // Province incorrecte
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Province incorrecte"))
    }

    @Test
    fun `test login CB avec province et entité correctes`() {
        // Given - Créer et sauvegarder structure géographique
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        val entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        
        val cbUser = Utilisateur(
            username = "cb_kinshasa",
            password = passwordEncoder.encode("password123"),
            role = Role.CB,
            province = province,
            entite = entite
        )
        userRepository.save(cbUser)

        val loginRequest = LoginRequest(
            username = "cb_kinshasa",
            password = "password123",
            role = Role.CB,
            province = province,
            entite = entite
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test login CB avec entité incorrecte doit échouer`() {
        // Given - Créer et sauvegarder structure géographique
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        val entiteCorrect = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        val entiteIncorrecte = entiteRepository.save(Entite(designation = "Autre entité", province = province, estVille = false))
        
        val cbUser = Utilisateur(
            username = "cb_kinshasa",
            password = passwordEncoder.encode("password123"),
            role = Role.CB,
            province = province,
            entite = entiteCorrect
        )
        userRepository.save(cbUser)

        val loginRequest = LoginRequest(
            username = "cb_kinshasa", 
            password = "password123",
            role = Role.CB,
            province = province,
            entite = entiteIncorrecte  // Entité incorrecte
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Entité incorrecte"))
    }

    @Test
    fun `test login OEC avec province, entité et commune correctes`() {
        // Given - Créer et sauvegarder structure géographique complète
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        val entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        val commune = communeRepository.save(Commune(designation = "Gombe", entite = entite))
        
        val oecUser = Utilisateur(
            username = "oec_gombe",
            password = passwordEncoder.encode("password123"),
            role = Role.OEC,
            province = province,
            entite = entite,
            commune = commune
        )
        userRepository.save(oecUser)

        val loginRequest = LoginRequest(
            username = "oec_gombe",
            password = "password123",
            role = Role.OEC,
            province = province,
            entite = entite,
            commune = commune
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test login OEC avec commune incorrecte doit échouer`() {
        // Given - Créer et sauvegarder structure géographique complète
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        val entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        val communeCorrect = communeRepository.save(Commune(designation = "Gombe", entite = entite))
        val communeIncorrecte = communeRepository.save(Commune(designation = "Kalamu", entite = entite))
        
        val oecUser = Utilisateur(
            username = "oec_gombe",
            password = passwordEncoder.encode("password123"),
            role = Role.OEC,
            province = province,
            entite = entite,
            commune = communeCorrect
        )
        userRepository.save(oecUser)

        val loginRequest = LoginRequest(
            username = "oec_gombe",
            password = "password123",
            role = Role.OEC,
            province = province,
            entite = entite,
            commune = communeIncorrecte  // Commune incorrecte
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Commune ou chefferie incorrecte"))
    }

    @Test
    fun `test login ADMIN sans restrictions géographiques`() {
        // Given - Admin sans province/entité/commune
        val adminUser = Utilisateur(
            username = "admin_general",
            password = passwordEncoder.encode("password123"),
            role = Role.ADMIN
        )
        userRepository.save(adminUser)

        val loginRequest = LoginRequest(
            username = "admin_general",
            password = "password123",
            role = Role.ADMIN
            // Pas de province/entité/commune requis pour ADMIN
        )

        // When & Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test register CD avec province`() {
        // Given - Créer et sauvegarder la province
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        
        val registerRequest = RegisterRequest(
            username = "nouveau_cd",
            password = "password123",
            role = Role.CD,
            province = province
        )

        // When & Then
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())

        // Vérifier que l'utilisateur a été créé en base
        val userCreated = userRepository.findByUsername("nouveau_cd")
        assert(userCreated != null)
        assert(userCreated!!.role == Role.CD)
        assert(userCreated.province?.designation == "Kinshasa")
    }

    @Test
    fun `test register OEC avec structure géographique complète`() {
        // Given - Créer et sauvegarder structure géographique
        val province = provinceRepository.save(Province(designation = "Kinshasa"))
        val entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        
        val registerRequest = RegisterRequest(
            username = "nouveau_oec",
            password = "password123",
            role = Role.OEC,
            province = province,
            entite = entite
        )

        // When & Then
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())

        // Vérifier la création en base
        val userCreated = userRepository.findByUsername("nouveau_oec")
        assert(userCreated != null)
        assert(userCreated!!.role == Role.OEC)
        assert(userCreated.province?.designation == "Kinshasa")
        assert(userCreated.entite?.designation == "Ville de Kinshasa")
    }
}

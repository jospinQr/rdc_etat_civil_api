package org.megamind.rdc_etat_civil.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.TestUtils.toJson
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Tests d'API endpoints pour PersonneController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'API - PersonneController")
class PersonneApiTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    private lateinit var baseUrl: String

    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$port/api/personnes"
        personneRepository.deleteAll()
    }

    @Test
    @DisplayName("POST /api/personnes - Devrait créer une personne")
    fun `POST should create person`() {
        // Given
        val request = PersonneTestBuilder.createDefaultRequest()
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(toJson(request), headers)

        // When
        val response = restTemplate.postForEntity(baseUrl, entity, Map::class.java)

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body?.containsKey("id") == true)
        assertEquals(request.nom, response.body?.get("nom"))
        assertEquals(request.prenom, response.body?.get("prenom"))
    }

    @Test
    @DisplayName("GET /api/personnes/{id} - Devrait récupérer une personne")
    fun `GET should retrieve person by id`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)

        // When
        val response = restTemplate.getForEntity("$baseUrl/${savedPersonne.id}", Map::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(savedPersonne.id.toString(), response.body?.get("id").toString())
        assertEquals(savedPersonne.nom, response.body?.get("nom"))
        assertEquals(savedPersonne.prenom, response.body?.get("prenom"))
    }

    @Test
    @DisplayName("GET /api/personnes/{id} - Devrait retourner 404 si personne non trouvée")
    fun `GET should return 404 if person not found`() {
        // Given
        val nonExistentId = 999L

        // When
        val response = restTemplate.getForEntity("$baseUrl/$nonExistentId", Map::class.java)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    @DisplayName("GET /api/personnes - Devrait lister toutes les personnes")
    fun `GET should list all persons`() {
        // Given
        val personne1 = PersonneTestBuilder.create()
            .withNom("Dupont")
            .withPrenom("Jean")
            .build()
        
        val personne2 = PersonneTestBuilder.create()
            .withNom("Martin")
            .withPrenom("Marie")
            .build()

        personneRepository.save(personne1)
        personneRepository.save(personne2)

        // When
        val response = restTemplate.getForEntity(baseUrl, Map::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body?.containsKey("content") ?:true)
        
        @Suppress("UNCHECKED_CAST")
        val content = (response.body?.get("content") ?:"" ) as List<Map<String, Any>>
        assertEquals(2, content.size)
        
        val noms = content.map { it["nom"] as String }
        assertTrue(noms.contains("Dupont"))
        assertTrue(noms.contains("Martin"))
    }

    @Test
    @DisplayName("GET /api/personnes - Devrait supporter la pagination")
    fun `GET should support pagination`() {
        // Given
        repeat(5) { index ->
            val personne = PersonneTestBuilder.create()
                .withNom("Personne$index")
                .withPrenom("Prénom$index")
                .build()
            personneRepository.save(personne)
        }

        // When
        val response = restTemplate.getForEntity("$baseUrl?page=0&size=3", Map::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        
        @Suppress("UNCHECKED_CAST")
        val content = (response.body?.get("content") ?:"") as List<Map<String, Any>>
        assertEquals(3, content.size)
        assertEquals(5L, response.body?.get("totalElements"))
        assertEquals(0, response.body?.get("number"))
        assertEquals(3, response.body?.get("size"))
    }

    @Test
    @DisplayName("PUT /api/personnes/{id} - Devrait mettre à jour une personne")
    fun `PUT should update person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)

        val updateRequest = PersonneTestBuilder.createDefaultRequest().copy(
            nom = "Nouveau Nom",
            prenom = "Nouveau Prénom"
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(toJson(updateRequest), headers)

        // When
        val response = restTemplate.exchange(
            "$baseUrl/${savedPersonne.id}",
            HttpMethod.PUT,
            entity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Nouveau Nom", response.body?.get("nom"))
        assertEquals("Nouveau Prénom", response.body?.get("prenom"))
    }

    @Test
    @DisplayName("PUT /api/personnes/{id} - Devrait retourner 404 si personne non trouvée")
    fun `PUT should return 404 if person not found`() {
        // Given
        val nonExistentId = 999L
        val updateRequest = PersonneTestBuilder.createDefaultRequest()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(toJson(updateRequest), headers)

        // When
        val response = restTemplate.exchange(
            "$baseUrl/$nonExistentId",
            HttpMethod.PUT,
            entity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    @DisplayName("DELETE /api/personnes/{id} - Devrait supprimer une personne")
    fun `DELETE should delete person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)

        // When
        val response = restTemplate.exchange(
            "$baseUrl/${savedPersonne.id}",
            HttpMethod.DELETE,
            null,
            Void::class.java
        )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        
        // Vérifier que la personne a été supprimée
        val deletedPersonne = personneRepository.findById(savedPersonne.id!!)
        assertFalse(deletedPersonne.isPresent)
    }

    @Test
    @DisplayName("DELETE /api/personnes/{id} - Devrait retourner 404 si personne non trouvée")
    fun `DELETE should return 404 if person not found`() {
        // Given
        val nonExistentId = 999L

        // When
        val response = restTemplate.exchange(
            "$baseUrl/$nonExistentId",
            HttpMethod.DELETE,
            null,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    @DisplayName("POST /api/personnes - Devrait valider les données de la requête")
    fun `POST should validate request data`() {
        // Given
        val invalidRequest = PersonneTestBuilder.createDefaultRequest().copy(
            nom = "", // Nom vide
            prenom = "" // Prénom vide
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(toJson(invalidRequest), headers)

        // When
        val response = restTemplate.postForEntity(baseUrl, entity, Map::class.java)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    @DisplayName("Devrait gérer les erreurs de sérialisation JSON")
    fun `should handle JSON serialization errors`() {
        // Given
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val invalidJson = "{ invalid json }"
        val entity = HttpEntity(invalidJson, headers)

        // When
        val response = restTemplate.postForEntity(baseUrl, entity, Map::class.java)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}


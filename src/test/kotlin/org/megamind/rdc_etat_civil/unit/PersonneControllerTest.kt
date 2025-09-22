package org.megamind.rdc_etat_civil.unit

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.common.toJson
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.megamind.rdc_etat_civil.personne.dto.PersonneResponse
import org.megamind.rdc_etat_civil.utils.PaginatedResponse
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Tests unitaires pour PersonneController
 */
@DisplayName("Tests unitaires - PersonneController")
class PersonneControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var personneService: PersonneService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        personneService = mockk()
        objectMapper = ObjectMapper()
        
        val controller = org.megamind.rdc_etat_civil.personne.PersonneController(personneService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Nested
    @DisplayName("POST /api/personnes")
    inner class CreatePersonne {

        @Test
        @DisplayName("Devrait créer une personne avec succès")
        fun `should create person successfully`() {
            // Given
            val request = PersonneTestBuilder.createDefaultRequest()
            val response = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneService.creerPersonne(any()) } returns response

            // When & Then
            mockMvc.perform(
                post("/api/personnes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value(request.nom))
                .andExpect(jsonPath("$.prenom").value(request.prenom ?: ""))

            verify { personneService.creerPersonne(any()) }
        }

        @Test
        @DisplayName("Devrait retourner 400 pour une requête invalide")
        fun `should return 400 for invalid request`() {
            // Given
            val invalidRequest = PersonneTestBuilder.createDefaultRequest().copy(nom = "")

            // When & Then
            mockMvc.perform(
                post("/api/personnes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(invalidRequest))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/personnes/{id}")
    inner class GetPersonneById {

        @Test
        @DisplayName("Devrait retourner une personne par ID")
        fun `should return person by id`() {
            // Given
            val personneId = 1L
            val personne = PersonneTestBuilder.createDefault().apply { id = personneId }
            
            every { personneService.obtenirPersonne(personneId) } returns personne

            // When & Then
            mockMvc.perform(get("/api/personnes/$personneId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(personneId))
                .andExpect(jsonPath("$.nom").value(personne.nom))

            verify { personneService.obtenirPersonne(personneId) }
        }

        @Test
        @DisplayName("Devrait retourner 404 si la personne n'existe pas")
        fun `should return 404 if person not found`() {
            // Given
            val personneId = 999L
            every { personneService.obtenirPersonne(personneId) } throws IllegalArgumentException("Personne introuvable")

            // When & Then
            mockMvc.perform(get("/api/personnes/$personneId"))
                .andExpect(status().isNotFound)

            verify { personneService.findById(personneId) }
        }
    }

    @Nested
    @DisplayName("GET /api/personnes")
    inner class GetAllPersonnes {

        @Test
        @DisplayName("Devrait retourner la liste paginée des personnes")
        fun `should return paginated list of persons`() {
            // Given
            val personnes = listOf(
                PersonneTestBuilder.createDefault().apply { id = 1L },
                PersonneTestBuilder.createDefault().apply { id = 2L }
            )
            val page = PageImpl(personnes)
            val paginatedResponse = PaginatedResponse(page.content, page.totalElements, page.number, page.size)
            
            every { personneService.listerPersonnes(0, 10) } returns paginatedResponse

            // When & Then
            mockMvc.perform(get("/api/personnes?page=0&size=10"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))

            verify { personneService.listerPersonnes(0, 10) }
        }
    }

    @Nested
    @DisplayName("PUT /api/personnes/{id}")
    inner class UpdatePersonne {

        @Test
        @DisplayName("Devrait mettre à jour une personne existante")
        fun `should update existing person`() {
            // Given
            val personneId = 1L
            val request = PersonneTestBuilder.createDefaultRequest().copy(nom = "Nouveau Nom")
            val updatedPersonne = PersonneTestBuilder.createDefault().apply { 
                id = personneId
                nom = "Nouveau Nom"
            }
            
            every { personneService.modifierPersonne(personneId, any()) } returns updatedPersonne

            // When & Then
            mockMvc.perform(
                put("/api/personnes/$personneId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(personneId))
                .andExpect(jsonPath("$.nom").value("Nouveau Nom"))

            verify { personneService.modifierPersonne(personneId, any()) }
        }
    }

    @Nested
    @DisplayName("DELETE /api/personnes/{id}")
    inner class DeletePersonne {

        @Test
        @DisplayName("Devrait supprimer une personne")
        fun `should delete person`() {
            // Given
            val personneId = 1L
            every { personneService.supprimerPersonne(personneId) } returns Unit

            // When & Then
            mockMvc.perform(delete("/api/personnes/$personneId"))
                .andExpect(status().isNoContent)

            verify { personneService.supprimerPersonne(personneId) }
        }
    }
}

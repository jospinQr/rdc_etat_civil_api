package org.megamind.rdc_etat_civil.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.megamind.rdc_etat_civil.personne.dto.PersonneResponse
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.*

/**
 * Tests unitaires pour PersonneService
 */
@DisplayName("Tests unitaires - PersonneService")
class PersonneServiceTest {

    private lateinit var personneRepository: PersonneRepository
    private lateinit var personneService: PersonneService

    @BeforeEach
    fun setUp() {
        personneRepository = mockk()
        personneService = PersonneService(personneRepository)
    }

    @Nested
    @DisplayName("Création de personne")
    inner class CreatePersonne {

        @Test
        @DisplayName("Devrait créer une personne avec succès")
        fun `should create person successfully`() {
            // Given
            val personneRequest = PersonneTestBuilder.createDefaultRequest()
            val personneSaved = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneRepository.save(any()) } returns personneSaved

            // When
            val result = personneService.creerPersonne(personneRequest)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertEquals(personneRequest.nom, result.nom)
            assertEquals(personneRequest.postnom, result.postnom)
            assertEquals(personneRequest.prenom, result.prenom)
            verify { personneRepository.save(any()) }
        }

        @Test
        @DisplayName("Devrait lancer une exception si la personne existe déjà")
        fun `should throw exception if person already exists`() {
            // Given
            val personneRequest = PersonneTestBuilder.createDefaultRequest()
            every { 
                personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(
                    any(), any(), any(), any()
                ) 
            } returns PersonneTestBuilder.createDefault()

            // When & Then
            assertThrows(IllegalArgumentException::class.java) {
                personneService.creerPersonne(personneRequest)
            }
        }
    }

    @Nested
    @DisplayName("Recherche de personne")
    inner class FindPersonne {

        @Test
        @DisplayName("Devrait trouver une personne par ID")
        fun `should find person by id`() {
            // Given
            val personneId = 1L
            val personne = PersonneTestBuilder.createDefault().apply { id = personneId }
            every { personneRepository.findById(personneId) } returns Optional.of(personne)

            // When
            val result = personneService.obtenirPersonne(personneId)

            // Then
            assertNotNull(result)
            assertEquals(personneId, result.id)
            verify { personneRepository.findById(personneId) }
        }

        @Test
        @DisplayName("Devrait lancer une exception si la personne n'existe pas")
        fun `should throw exception if person not found`() {
            // Given
            val personneId = 999L
            every { personneRepository.findById(personneId) } returns Optional.empty()

            // When & Then
            assertThrows(IllegalArgumentException::class.java) {
                personneService.obtenirPersonne(personneId)
            }
        }

        @Test
        @DisplayName("Devrait rechercher des personnes avec pagination")
        fun `should search persons with pagination`() {
            // Given
            val personnes = listOf(
                PersonneTestBuilder.createDefault().apply { id = 1L },
                PersonneTestBuilder.createDefault().apply { id = 2L }
            )
            val page = PageImpl(personnes)
            val pageRequest = PageRequest.of(0, 10)
            
            every { personneRepository.findAll(pageRequest) } returns page

            // When
            val result = personneService.listerPersonnes(0, 10)

            // Then
            assertNotNull(result)
            assertEquals(2, result.content.size)
            verify { personneRepository.findAll(pageRequest) }
        }
    }

    @Nested
    @DisplayName("Mise à jour de personne")
    inner class UpdatePersonne {

        @Test
        @DisplayName("Devrait mettre à jour une personne existante")
        fun `should update existing person`() {
            // Given
            val personneId = 1L
            val existingPersonne = PersonneTestBuilder.createDefault().apply { id = personneId }
            val updateRequest = PersonneTestBuilder.createDefaultRequest().copy(nom = "Nouveau Nom")
            val updatedPersonne = PersonneTestBuilder.create()
                .withId(personneId)
                .withNom("Nouveau Nom")
                .build()
            
            every { personneRepository.findById(personneId) } returns Optional.of(existingPersonne)
            every { personneRepository.save(any()) } returns updatedPersonne

            // When
            val result = personneService.modifierPersonne(personneId, updateRequest)

            // Then
            assertNotNull(result)
            assertEquals("Nouveau Nom", result.nom)
            verify { personneRepository.findById(personneId) }
            verify { personneRepository.save(any()) }
        }

        @Test
        @DisplayName("Devrait lancer une exception si la personne à mettre à jour n'existe pas")
        fun `should throw exception if person to update not found`() {
            // Given
            val personneId = 999L
            val updateRequest = PersonneTestBuilder.createDefaultRequest()
            every { personneRepository.findById(personneId) } returns Optional.empty()

            // When & Then
            assertThrows(IllegalArgumentException::class.java) {
                personneService.modifierPersonne(personneId, updateRequest)
            }
        }
    }

    @Nested
    @DisplayName("Suppression de personne")
    inner class DeletePersonne {

        @Test
        @DisplayName("Devrait supprimer une personne existante")
        fun `should delete existing person`() {
            // Given
            val personneId = 1L
            every { personneRepository.existsById(personneId) } returns true
            every { personneRepository.deleteById(personneId) } returns Unit

            // When
            personneService.supprimerPersonne(personneId)

            // Then
            verify { personneRepository.existsById(personneId) }
            verify { personneRepository.deleteById(personneId) }
        }

        @Test
        @DisplayName("Devrait lancer une exception si la personne à supprimer n'existe pas")
        fun `should throw exception if person to delete not found`() {
            // Given
            val personneId = 999L
            every { personneRepository.existsById(personneId) } returns false

            // When & Then
            assertThrows(IllegalArgumentException::class.java) {
                personneService.supprimerPersonne(personneId)
            }
        }
    }

    @Nested
    @DisplayName("Gestion des valeurs nulles")
    inner class NullHandling {

        @Test
        @DisplayName("Devrait gérer les champs optionnels nulls")
        fun `should handle optional null fields`() {
            // Given
            val personneRequest = PersonneTestBuilder.create()
                .withPrenom(null)
                .withDateNaissance(null)
                .withLieuNaiss(null)
                .withProfession(null)
                .buildRequest()
            
            val personneSaved = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneRepository.save(any()) } returns personneSaved

            // When
            val result = personneService.creerPersonne(personneRequest)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertEquals(personneRequest.nom, result.nom)
            assertEquals(personneRequest.postnom, result.postnom)
            assertNull(personneRequest.prenom) // Vérifier que le prénom est bien null
            verify { personneRepository.save(any()) }
        }

        @Test
        @DisplayName("Devrait valider les champs obligatoires non nulls")
        fun `should validate required non-null fields`() {
            // Given
            val personneRequest = PersonneTestBuilder.create()
                .withNom("") // Nom vide
                .withPostnom("") // Postnom vide
                .buildRequest()

            // When & Then
            assertThrows(Exception::class.java) {
                personneService.creerPersonne(personneRequest)
            }
        }

        @Test
        @DisplayName("Devrait gérer les parents nulls")
        fun `should handle null parents`() {
            // Given
            val personneRequest = PersonneTestBuilder.create()
                .withPrenom(null)
                .buildRequest()
            
            val personneSaved = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneRepository.save(any()) } returns personneSaved

            // When
            val result = personneService.creerPersonne(personneRequest)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertNull(result.prenom)
            verify { personneRepository.save(any()) }
        }

        @Test
        @DisplayName("Devrait gérer les champs de contact nulls")
        fun `should handle null contact fields`() {
            // Given
            val personneRequest = PersonneTestBuilder.create()
                .withTelephone(null)
                .withEmail(null)
                .withCommuneChefferie(null)
                .buildRequest()
            
            val personneSaved = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneRepository.save(any()) } returns personneSaved

            // When
            val result = personneService.creerPersonne(personneRequest)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertNull(result.telephone)
            assertNull(result.email)
            assertNull(result.communeChefferie)
            verify { personneRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Tests de validation des enums")
    inner class EnumValidation {

        @Test
        @DisplayName("Devrait accepter les valeurs d'enum valides")
        fun `should accept valid enum values`() {
            // Given
            val personneRequest = PersonneTestBuilder.create()
                .withSexe(Sexe.FEMININ)
                .withStatut(StatutPersonne.VIVANT)
                .withSituationMatrimoniale(SituationMatrimoniale.MARIE)
                .buildRequest()
            
            val personneSaved = PersonneTestBuilder.createDefault().apply { id = 1L }
            
            every { personneRepository.save(any()) } returns personneSaved

            // When
            val result = personneService.creerPersonne(personneRequest)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertEquals(Sexe.FEMININ, result.sexe)
            assertEquals(StatutPersonne.VIVANT, result.statut)
            assertEquals(SituationMatrimoniale.MARIE, result.situationMatrimoniale)
            verify { personneRepository.save(any()) }
        }
    }
}

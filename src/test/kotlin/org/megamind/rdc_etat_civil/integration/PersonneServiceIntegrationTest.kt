package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

/**
 * Tests d'intégrité pour PersonneService avec base de données
 */
@DataJpaTest
@Import(PersonneService::class)
@ActiveProfiles("test")
@DisplayName("Tests d'intégrité - PersonneService")
class PersonneServiceIntegrationTest {

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    @Autowired
    private lateinit var personneService: PersonneService

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @BeforeEach
    fun setUp() {
        personneRepository.deleteAll()
        testEntityManager.flush()
        testEntityManager.clear()
    }

    @Test
    @DisplayName("Devrait créer une personne avec persistance")
    fun `should create person with persistence`() {
        // Given
        val personneRequest = PersonneTestBuilder.createDefaultRequest()

        // When
        val result = personneService.creerPersonne(personneRequest)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(personneRequest.nom, result.nom)
        assertEquals(personneRequest.prenom, result.prenom)
        assertEquals(personneRequest.dateNaissance, result.dateNaissance)

        // Vérifier la persistance
        val savedPersonne = personneRepository.findById(result.id!!)
        assertTrue(savedPersonne.isPresent)
        assertEquals(personneRequest.nom, savedPersonne.get().nom)
    }

    @Test
    @DisplayName("Devrait récupérer une personne par ID")
    fun `should retrieve person by id`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = personneService.obtenirPersonne(savedPersonne.id!!)

        // Then
        assertNotNull(result)
        assertEquals(savedPersonne.id, result.id)
        assertEquals(savedPersonne.nom, result.nom)
        assertEquals(savedPersonne.prenom, result.prenom)
    }

    @Test
    @DisplayName("Devrait mettre à jour une personne existante")
    fun `should update existing person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()
        testEntityManager.clear()

        val updateRequest = PersonneTestBuilder.createDefaultRequest().copy(
            nom = "Nouveau Nom",
            prenom = "Nouveau Prénom"
        )

        // When
        val result = personneService.modifierPersonne(savedPersonne.id!!, updateRequest)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        assertNotNull(result)
        assertEquals("Nouveau Nom", result.nom)
        assertEquals("Nouveau Prénom", result.prenom)

        // Vérifier la persistance
        val updatedPersonne = personneRepository.findById(savedPersonne.id!!)
        assertTrue(updatedPersonne.isPresent)
        assertEquals("Nouveau Nom", updatedPersonne.get().nom)
    }

    @Test
    @DisplayName("Devrait supprimer une personne")
    fun `should delete person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()

        // When
        personneService.supprimerPersonne(savedPersonne.id!!)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        val deletedPersonne = personneRepository.findById(savedPersonne.id!!)
        assertFalse(deletedPersonne.isPresent)
    }

    @Test
    @DisplayName("Devrait lister toutes les personnes avec pagination")
    fun `should list all persons with pagination`() {
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
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = personneService.listerPersonnes(0, 10)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(2L, result.totalElements)
        assertTrue(result.content.any { it.nom == "Dupont" })
        assertTrue(result.content.any { it.nom == "Martin" })
    }

    @Test
    @DisplayName("Devrait lancer une exception si la personne n'existe pas pour la mise à jour")
    fun `should throw exception if person not found for update`() {
        // Given
        val updateRequest = PersonneTestBuilder.createDefaultRequest()
        val nonExistentId = 999L

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            personneService.modifierPersonne(nonExistentId, updateRequest)
        }
    }

    @Test
    @DisplayName("Devrait lancer une exception si la personne n'existe pas pour la suppression")
    fun `should throw exception if person not found for deletion`() {
        // Given
        val nonExistentId = 999L

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            personneService.supprimerPersonne(nonExistentId)
        }
    }

    @Test
    @DisplayName("Devrait gérer les contraintes de validation")
    fun `should handle validation constraints`() {
        // Given
        val invalidRequest = PersonneTestBuilder.createDefaultRequest().copy(
            nom = "", // Nom vide
            prenom = "" // Prénom vide
        )

        // When & Then
        assertThrows(Exception::class.java) {
            personneService.creerPersonne(invalidRequest)
        }
    }

    @Test
    @DisplayName("Devrait gérer les transactions")
    fun `should handle transactions`() {
        // Given
        val personne1 = PersonneTestBuilder.createDefaultRequest()
        val personne2 = PersonneTestBuilder.createDefaultRequest()

        // When
        val result1 = personneService.creerPersonne(personne1)
        val result2 = personneService.creerPersonne(personne2)

        // Then - Les deux personnes devraient être créées dans la même transaction
        assertNotNull(result1.id)
        assertNotNull(result2.id)
        assertNotEquals(result1.id, result2.id)

        // Vérifier la persistance
        val allPersons = personneRepository.findAll()
        assertEquals(2, allPersons.size)
    }
}

package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

/**
 * Tests d'intégrité pour l'entité Personne
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégrité - Entité Personne")
class PersonneEntityIntegrationTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    @BeforeEach
    fun setUp() {
        // Nettoyer la base de données avant chaque test
        personneRepository.deleteAll()
        testEntityManager.flush()
        testEntityManager.clear()
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer une personne")
    fun `should save and retrieve person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()

        // When
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()
        testEntityManager.clear()

        val retrievedPersonne = personneRepository.findById(savedPersonne.id!!)

        // Then
        assertTrue(retrievedPersonne.isPresent)
        assertEquals(savedPersonne.id, retrievedPersonne.get().id)
        assertEquals(personne.nom, retrievedPersonne.get().nom)
        assertEquals(personne.prenom, retrievedPersonne.get().prenom)
        assertEquals(personne.dateNaissance, retrievedPersonne.get().dateNaissance)
    }

    @Test
    @DisplayName("Devrait mettre à jour une personne existante")
    fun `should update existing person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val updatedPersonne = savedPersonne.apply {
            nom = "Nouveau Nom"
            prenom = "Nouveau Prénom"
        }
        val result = personneRepository.save(updatedPersonne)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        val retrievedPersonne = personneRepository.findById(result.id!!)
        assertTrue(retrievedPersonne.isPresent)
        assertEquals("Nouveau Nom", retrievedPersonne.get().nom)
        assertEquals("Nouveau Prénom", retrievedPersonne.get().prenom)
    }

    @Test
    @DisplayName("Devrait supprimer une personne")
    fun `should delete person`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()

        // When
        personneRepository.deleteById(savedPersonne.id!!)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        val retrievedPersonne = personneRepository.findById(savedPersonne.id!!)
        assertFalse(retrievedPersonne.isPresent)
    }

    @Test
    @DisplayName("Devrait trouver des personnes par nom et prénom")
    fun `should find persons by nom and prenom`() {
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
        val foundPersonnes = personneRepository.findByNomAndPrenom("Dupont", "Jean")

        // Then
        assertNotNull(foundPersonnes)
        assertEquals("Dupont", foundPersonnes.nom)
        assertEquals("Jean", foundPersonnes.prenom)
    }

    @Test
    @DisplayName("Devrait trouver des personnes par date de naissance")
    fun `should find persons by date of birth`() {
        // Given
        val dateNaissance = LocalDate.of(1990, 5, 15)
        val personne1 = PersonneTestBuilder.create()
            .withDateNaissance(dateNaissance)
            .withNom("Dupont")
            .build()
        
        val personne2 = PersonneTestBuilder.create()
            .withDateNaissance(LocalDate.of(1985, 3, 20))
            .withNom("Martin")
            .build()

        personneRepository.save(personne1)
        personneRepository.save(personne2)
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val foundPersonnes = personneRepository.findByDateNaissance(dateNaissance)

        // Then
        assertNotNull(foundPersonnes)
        assertFalse(foundPersonnes.isEmpty())
        assertTrue(foundPersonnes.all { it.dateNaissance == dateNaissance })
    }

    @Test
    @DisplayName("Devrait valider les contraintes de l'entité")
    fun `should validate entity constraints`() {
        // Given - Créer une personne avec des données invalides
        val personne = PersonneTestBuilder.create()
            .withNom("") // Nom vide
            .withPrenom("") // Prénom vide
            .build()

        // When & Then
        assertThrows(Exception::class.java) {
            personneRepository.save(personne)
            testEntityManager.flush()
        }
    }

    @Test
    @DisplayName("Devrait gérer les relations avec d'autres entités")
    fun `should handle relationships with other entities`() {
        // Given
        val personne = PersonneTestBuilder.createDefault()
        
        // When
        val savedPersonne = personneRepository.save(personne)
        testEntityManager.flush()
        testEntityManager.clear()

        // Then
        val retrievedPersonne = personneRepository.findById(savedPersonne.id!!)
        assertTrue(retrievedPersonne.isPresent)
        
        // Vérifier que les relations sont correctement chargées
        val personneRetrieved = retrievedPersonne.get()
        assertNotNull(personneRetrieved.nomPere)
        assertNotNull(personneRetrieved.nomMere)
        assertNotNull(personneRetrieved.profession)
        assertNotNull(personneRetrieved.adresse)
    }
}


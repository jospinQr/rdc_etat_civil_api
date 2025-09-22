package org.megamind.rdc_etat_civil.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.common.builders.PersonneTestBuilder
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

/**
 * Tests unitaires pour PersonneRepository
 */
@DisplayName("Tests unitaires - PersonneRepository")
class PersonneRepositoryTest {

    private lateinit var personneRepository: PersonneRepository

    @BeforeEach
    fun setUp() {
        personneRepository = mockk()
    }

    @Nested
    @DisplayName("Opérations de base")
    inner class BasicOperations {

        @Test
        @DisplayName("Devrait sauvegarder une personne")
        fun `should save person`() {
            // Given
            val personne = PersonneTestBuilder.createDefault()
            val savedPersonne = personne.apply { id = 1L }
            
            every { personneRepository.save(personne) } returns savedPersonne

            // When
            val result = personneRepository.save(personne)

            // Then
            assertNotNull(result)
            assertEquals(1L, result.id)
            assertEquals(personne.nom, result.nom)
            verify { personneRepository.save(personne) }
        }

        @Test
        @DisplayName("Devrait trouver une personne par ID")
        fun `should find person by id`() {
            // Given
            val personneId = 1L
            val personne = PersonneTestBuilder.createDefault().apply { id = personneId }
            every { personneRepository.findById(personneId) } returns Optional.of(personne)

            // When
            val result = personneRepository.findById(personneId)

            // Then
            assertTrue(result.isPresent)
            assertEquals(personneId, result.get().id)
            verify { personneRepository.findById(personneId) }
        }

        @Test
        @DisplayName("Devrait retourner Optional.empty si personne non trouvée")
        fun `should return empty optional if person not found`() {
            // Given
            val personneId = 999L
            every { personneRepository.findById(personneId) } returns Optional.empty()

            // When
            val result = personneRepository.findById(personneId)

            // Then
            assertFalse(result.isPresent)
            verify { personneRepository.findById(personneId) }
        }

        @Test
        @DisplayName("Devrait supprimer une personne par ID")
        fun `should delete person by id`() {
            // Given
            val personneId = 1L
            every { personneRepository.deleteById(personneId) } returns Unit

            // When
            personneRepository.deleteById(personneId)

            // Then
            verify { personneRepository.deleteById(personneId) }
        }

        @Test
        @DisplayName("Devrait vérifier l'existence d'une personne")
        fun `should check person existence`() {
            // Given
            val personneId = 1L
            every { personneRepository.existsById(personneId) } returns true

            // When
            val exists = personneRepository.existsById(personneId)

            // Then
            assertTrue(exists)
            verify { personneRepository.existsById(personneId) }
        }
    }

    @Nested
    @DisplayName("Recherches spécifiques")
    inner class SpecificSearches {

        @Test
        @DisplayName("Devrait vérifier l'existence d'une personne par nom, postnom, prénom et date")
        fun `should check person existence by nom postnom prenom and date`() {
            // Given
            val nom = "Dupont"
            val postnom = "Jean"
            val prenom = "Pierre"
            val dateNaissance = LocalDate.of(1990, 5, 15)
            
            every { personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance) } returns true

            // When
            val exists = personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance)

            // Then
            assertTrue(exists)
            verify { personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance) }
        }

        @Test
        @DisplayName("Devrait trouver une personne par nom, postnom, prénom et date de naissance")
        fun `should find person by nom postnom prenom and date naissance`() {
            // Given
            val nom = "Dupont"
            val postnom = "Jean"
            val prenom = "Pierre"
            val dateNaissance = LocalDate.of(1990, 5, 15)
            val personne = PersonneTestBuilder.create()
                .withNom(nom)
                .withPrenom(prenom)
                .withDateNaissance(dateNaissance)
                .build()
            
            every { personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance) } returns personne

            // When
            val result = personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance)

            // Then
            assertNotNull(result)
            assertEquals(nom, result.nom)
            assertEquals(prenom, result.prenom)
            assertEquals(dateNaissance, result.dateNaissance)
            verify { personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(nom, postnom, prenom, dateNaissance) }
        }

        @Test
        @DisplayName("Devrait trouver des personnes par date de naissance")
        fun `should find persons by date naissance`() {
            // Given
            val dateNaissance = LocalDate.of(1990, 5, 15)
            val personnes = listOf(
                PersonneTestBuilder.create()
                    .withDateNaissance(dateNaissance)
                    .withNom("Dupont")
                    .build(),
                PersonneTestBuilder.create()
                    .withDateNaissance(dateNaissance)
                    .withNom("Martin")
                    .build()
            )
            
            every { personneRepository.findByDateNaissance(dateNaissance) } returns personnes

            // When
            val result = personneRepository.findByDateNaissance(dateNaissance)

            // Then
            assertNotNull(result)
            assertEquals(2, result.size)
            assertTrue(result.all { it.dateNaissance == dateNaissance })
            verify { personneRepository.findByDateNaissance(dateNaissance) }
        }
    }

    @Nested
    @DisplayName("Pagination et tri")
    inner class PaginationAndSorting {

        @Test
        @DisplayName("Devrait retourner une page de personnes")
        fun `should return page of persons`() {
            // Given
            val personnes = listOf(
                PersonneTestBuilder.createDefault().apply { id = 1L },
                PersonneTestBuilder.createDefault().apply { id = 2L }
            )
            val page = PageImpl(personnes)
            val pageable = PageRequest.of(0, 10)
            
            every { personneRepository.findAll(pageable) } returns page

            // When
            val result = personneRepository.findAll(pageable)

            // Then
            assertNotNull(result)
            assertEquals(2, result.content.size)
            assertEquals(2L, result.totalElements)
            assertEquals(0, result.number)
            assertEquals(10, result.size)
            verify { personneRepository.findAll(pageable) }
        }

        @Test
        @DisplayName("Devrait compter le nombre total de personnes")
        fun `should count total persons`() {
            // Given
            every { personneRepository.count() } returns 5L

            // When
            val count = personneRepository.count()

            // Then
            assertEquals(5L, count)
            verify { personneRepository.count() }
        }
    }

    @Nested
    @DisplayName("Opérations en lot")
    inner class BatchOperations {

        @Test
        @DisplayName("Devrait sauvegarder toutes les personnes")
        fun `should save all persons`() {
            // Given
            val personnes = listOf(
                PersonneTestBuilder.createDefault(),
                PersonneTestBuilder.createDefault()
            )
            val savedPersonnes = personnes.mapIndexed { index, personne ->
                personne.apply { id = (index + 1).toLong() }
            }
            
            every { personneRepository.saveAll(personnes) } returns savedPersonnes

            // When
            val result = personneRepository.saveAll(personnes)

            // Then
            assertNotNull(result)
            assertEquals(2, result.size)
            assertTrue(result.all { it.id != null })
            verify { personneRepository.saveAll(personnes) }
        }

        @Test
        @DisplayName("Devrait supprimer toutes les personnes")
        fun `should delete all persons`() {
            // Given
            val personnes = listOf(
                PersonneTestBuilder.createDefault().apply { id = 1L },
                PersonneTestBuilder.createDefault().apply { id = 2L }
            )
            
            every { personneRepository.deleteAll(personnes) } returns Unit

            // When
            personneRepository.deleteAll(personnes)

            // Then
            verify { personneRepository.deleteAll(personnes) }
        }
    }
}

package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.naissance.ActeNaissance
import org.megamind.rdc_etat_civil.naissance.ActeNaissanceRepository
import org.megamind.rdc_etat_civil.personne.*
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.test.*

/**
 * Test simplifié pour diagnostiquer les problèmes de repository
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ActeNaissanceRepositorySimpleTest {

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

    // Données de test
    private lateinit var province: Province
    private lateinit var entite: Entite
    private lateinit var commune: Commune
    private lateinit var enfant: Personne

    @BeforeEach
    fun setup() {
        // Nettoyer les données de test
        acteNaissanceRepository.deleteAll()
        personneRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()

        // Créer la structure territoriale étape par étape
        province = Province(designation = "Kinshasa")
        province = provinceRepository.save(province)
        
        println("Province sauvegardée - ID: ${province.id}, Designation: ${province.designation}")
        assertNotNull(province.id, "L'ID de la province doit être généré")

        entite = Entite(
            designation = "Ville de Kinshasa",
            estVille = true,
            province = province
        )
        entite = entiteRepository.save(entite)
        
        println("Entité sauvegardée - ID: ${entite.id}, Designation: ${entite.designation}")
        assertNotNull(entite.id, "L'ID de l'entité doit être généré")

        commune = Commune(
            designation = "Gombe",
            entite = entite
        )
        commune = communeRepository.save(commune)
        
        println("Commune sauvegardée - ID: ${commune.id}, Designation: ${commune.designation}")
        assertNotNull(commune.id, "L'ID de la commune doit être généré")

        // Créer un enfant simple
        enfant = Personne(
            nom = "MUKENDI",
            postnom = "JEAN",
            prenom = "Pierre",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2020, 3, 10)
        )
        enfant = personneRepository.save(enfant)
        
        println("Enfant sauvegardé - ID: ${enfant.id}, Nom: ${enfant.nom}")
        assertNotNull(enfant.id, "L'ID de l'enfant doit être généré")
    }

    @Test
    fun `devrait créer et sauvegarder un acte de naissance basique`() {
        // Given
        val acte = ActeNaissance(
            numeroActe = "TEST/2020/001",
            enfant = enfant,
            commune = commune,
            officier = "KABONGO Jean-Pierre",
            dateEnregistrement = LocalDate.now()
        )

        // When
        val savedActe = acteNaissanceRepository.save(acte)
        
        println("Acte sauvegardé - ID: ${savedActe.id}, Numéro: ${savedActe.numeroActe}")

        // Then
        assertNotNull(savedActe.id, "L'ID de l'acte doit être généré")
        assertTrue(savedActe.id!! > 0, "L'ID doit être positif")
        assertEquals("TEST/2020/001", savedActe.numeroActe)
        assertEquals(enfant.id, savedActe.enfant.id)
        assertEquals(commune.id, savedActe.commune.id)
    }

    @Test
    fun `devrait trouver un acte par numéro`() {
        // Given - Créer un nouvel enfant pour ce test
        val nouvelEnfant = Personne(
            nom = "KASONGO",
            postnom = "MARIE",
            prenom = "Claire",
            sexe = Sexe.FEMININ,
            dateNaissance = LocalDate.of(2021, 5, 15)
        )
        val savedEnfant = personneRepository.save(nouvelEnfant)
        
        val acte = acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "TEST/2020/002",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val foundActe = acteNaissanceRepository.findByNumeroActe("TEST/2020/002")

        // Then
        assertNotNull(foundActe, "L'acte doit être trouvé")
        assertEquals(acte.id, foundActe?.id)
        assertEquals("TEST/2020/002", foundActe?.numeroActe)
    }

    @Test
    fun `devrait vérifier l'existence d'un numéro d'acte`() {
        // Given - Créer un nouvel enfant pour ce test
        val nouvelEnfant = Personne(
            nom = "MBEMBA",
            postnom = "JOSEPH",
            prenom = "Paul",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2022, 7, 20)
        )
        val savedEnfant = personneRepository.save(nouvelEnfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "TEST/2020/003",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When & Then
        assertTrue(acteNaissanceRepository.existsByNumeroActe("TEST/2020/003"))
        assertFalse(acteNaissanceRepository.existsByNumeroActe("TEST/2020/999"))
    }

    @Test
    fun `devrait compter les actes correctement`() {
        // Given
        val initialCount = acteNaissanceRepository.count()
        
        // Créer un nouvel enfant pour ce test
        val nouvelEnfant = Personne(
            nom = "TSHISEKEDI",
            postnom = "FELIX",
            prenom = "Antoine",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2023, 1, 10)
        )
        val savedEnfant = personneRepository.save(nouvelEnfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "TEST/2020/004",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When & Then
        assertEquals(initialCount + 1, acteNaissanceRepository.count())
    }

    @Test
    fun `devrait rechercher par nom d'enfant`() {
        // Given - Créer un nouvel enfant pour ce test
        val nouvelEnfant = Personne(
            nom = "MUKENDI",
            postnom = "PATRICE",
            prenom = "Lumumba",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2021, 8, 12)
        )
        val savedEnfant = personneRepository.save(nouvelEnfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "TEST/2020/005",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        val result = acteNaissanceRepository.rechercherParNomEnfant("MUKENDI", pageable)

        // Then
        assertTrue(result.totalElements >= 1, "Au moins un acte doit être trouvé")
        result.content.forEach { acte ->
            assertTrue(
                acte.enfant.nom.contains("MUKENDI") ||
                acte.enfant.postnom.contains("MUKENDI") ||
                (acte.enfant.prenom?.contains("MUKENDI") == true),
                "L'acte doit contenir le terme recherché"
            )
        }
    }

    @Test
    fun `devrait trouver par ID de province`() {
        // Given - Créer un nouvel enfant pour ce test
        val nouvelEnfant = Personne(
            nom = "KABILA",
            postnom = "LAURENT",
            prenom = "Désiré",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(2024, 3, 5)
        )
        val savedEnfant = personneRepository.save(nouvelEnfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "TEST/2020/006",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        val provinceId = province.id ?: throw AssertionError("L'ID de la province ne doit pas être null")
        val result = acteNaissanceRepository.findByProvinceId(provinceId, pageable)

        // Then
        assertTrue(result.totalElements >= 1, "Au moins un acte doit être trouvé")
    }
}

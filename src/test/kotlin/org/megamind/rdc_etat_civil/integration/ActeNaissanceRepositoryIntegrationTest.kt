package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.*

/**
 * Tests d'intégration pour ActeNaissanceRepository
 * Teste toutes les requêtes personnalisées et la persistance des données
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ActeNaissanceRepositoryIntegrationTest {

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
    private lateinit var enfantMasculin: Personne
    private lateinit var enfantFeminin: Personne
    private lateinit var pere: Personne
    private lateinit var mere: Personne

    @BeforeEach
    fun setup() {
        // Nettoyer les données de test dans l'ordre inverse des dépendances
        acteNaissanceRepository.deleteAll()
        personneRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()
        
        // S'assurer que les actes sont bien supprimés
        acteNaissanceRepository.flush()

        // Créer les données de test - Structure territoriale
        province = provinceRepository.save(
            Province(designation = "Kinshasa")
        )

        entite = entiteRepository.save(
            Entite(
                designation = "Ville de Kinshasa",
                estVille = true,
                province = province
            )
        )

        commune = communeRepository.save(
            Commune(
                designation = "Gombe",
                entite = entite
            )
        )

        // Créer les personnes de test
        pere = personneRepository.save(
            Personne(
                nom = "MUKENDI",
                postnom = "JACQUES",
                prenom = "Joseph",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(1980, 5, 15),
                profession = "Enseignant",
                nationalite = "Congolaise"
            )
        )

        mere = personneRepository.save(
            Personne(
                nom = "MBUYI",
                postnom = "MARIE",
                prenom = "Grace",
                sexe = Sexe.FEMININ,
                dateNaissance = LocalDate.of(1985, 8, 22),
                profession = "Infirmière",
                nationalite = "Congolaise"
            )
        )

        enfantMasculin = personneRepository.save(
            Personne(
                nom = "MUKENDI",
                postnom = "MBUYI",
                prenom = "Jean",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(2020, 3, 10),
                heureNaissance = LocalTime.of(14, 30),
                lieuNaiss = "Hôpital Général de Kinshasa",
                pere = pere,
                mere = mere
            )
        )

        enfantFeminin = personneRepository.save(
            Personne(
                nom = "MUKENDI",
                postnom = "MBUYI",
                prenom = "Marie",
                sexe = Sexe.FEMININ,
                dateNaissance = LocalDate.of(2022, 7, 25),
                heureNaissance = LocalTime.of(9, 15),
                lieuNaiss = "Clinique Ngaliema",
                pere = pere,
                mere = mere
            )
        )
    }

    // Méthode utilitaire pour créer des enfants uniques
    private fun creerEnfantsUniques(count: Int, prefixe: String = "TEST"): List<Personne> {
        return (1..count).map { index ->
            val nouvelEnfant = Personne(
                nom = "${prefixe}${index}",
                postnom = "ENFANT${index}",
                prenom = if (index % 2 == 0) "Jean${index}" else "Marie${index}",
                sexe = if (index % 2 == 0) Sexe.MASCULIN else Sexe.FEMININ,
                dateNaissance = LocalDate.of(2020, 3, 10 + index)
            )
            personneRepository.save(nouvelEnfant)
        }
    }

    // Méthode utilitaire pour nettoyer et créer des actes avec des enfants uniques
    private fun creerActesAvecEnfantsUniques(
        count: Int, 
        prefixeNumero: String = "KIN/2020/",
        prefixeEnfant: String = "TEST",
        startNumero: Int = 1000
    ): List<ActeNaissance> {
        // Nettoyer d'abord tous les actes existants
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        // Créer des enfants uniques
        val enfants = creerEnfantsUniques(count, prefixeEnfant)
        
        // Créer les actes
        return (0 until count).map { index ->
            val acte = ActeNaissance(
                numeroActe = "${prefixeNumero}${startNumero + index}",
                enfant = enfants[index],
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now().minusDays(index.toLong())
            )
            acteNaissanceRepository.save(acte)
        }
    }

    // ====== TESTS DES OPÉRATIONS CRUD DE BASE ======

    @Test
    fun `devrait sauvegarder et récupérer un acte de naissance`() {
        // Given
        val acte = ActeNaissance(
            numeroActe = "KIN/2020/001",
            enfant = enfantMasculin,
            commune = commune,
            officier = "KABONGO Jean-Pierre",
            declarant = "MUKENDI Jacques",
            dateEnregistrement = LocalDate.of(2020, 3, 15),
            temoin1 = "LWANGO Paul",
            temoin2 = "KASONGO Pierre"
        )

        // When
        val savedActe = acteNaissanceRepository.save(acte)

        // Then
        assertNotNull(savedActe.id)
        assertTrue(savedActe.id > 0)
        assertEquals("KIN/2020/001", savedActe.numeroActe)
        assertEquals(enfantMasculin.id, savedActe.enfant.id)
        assertEquals(commune.id, savedActe.commune.id)
    }

    @Test
    fun `devrait trouver un acte par numéro`() {
        // Given
        val acte = acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/002",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val foundActe = acteNaissanceRepository.findByNumeroActe("KIN/2020/002")

        // Then
        assertNotNull(foundActe)
        assertEquals(acte.id, foundActe?.id)
        assertEquals("KIN/2020/002", foundActe?.numeroActe)
    }

    @Test
    fun `devrait vérifier si un numéro d'acte existe`() {
        // Given
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/003",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When & Then
        assertTrue(acteNaissanceRepository.existsByNumeroActe("KIN/2020/003"))
        assertFalse(acteNaissanceRepository.existsByNumeroActe("KIN/2020/999"))
    }

    @Test
    fun `devrait trouver un acte par enfant`() {
        // Given
        val acte = acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/004",
                enfant = enfantFeminin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val foundActe = acteNaissanceRepository.findByEnfant(enfantFeminin)

        // Then
        assertNotNull(foundActe)
        assertEquals(acte.id, foundActe?.id)
        assertEquals(enfantFeminin.id, foundActe?.enfant?.id)
    }

    @Test
    fun `devrait vérifier si un enfant a un acte`() {
        // Given
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/005",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When & Then
        assertTrue(acteNaissanceRepository.existsByEnfant(enfantMasculin))
        assertFalse(acteNaissanceRepository.existsByEnfant(enfantFeminin))
    }

    // ====== TESTS DES RECHERCHES GÉOGRAPHIQUES ======

    @Test
    fun `devrait trouver les actes par commune avec pagination`() {
        // Given - Nettoyer d'abord tous les actes existants
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        // Créer de nouveaux enfants pour ce test
        val enfants = creerEnfantsUniques(5, "COMMUNE")
        
        repeat(5) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${100 + index}",
                    enfant = enfants[index],
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now().minusDays(index.toLong())
                )
            )
        }

        // When
        val pageable = PageRequest.of(0, 3)
        val result = acteNaissanceRepository.findByCommune(commune, pageable)

        // Then
        assertEquals(5, result.totalElements)
        assertEquals(3, result.content.size)
        assertTrue(result.hasNext())
    }

    @Test
    fun `devrait trouver les actes par ID de province`() {
        // Given - Créer une autre province pour tester la distinction
        val autreProvince = provinceRepository.save(Province(designation = "Haut-Katanga"))
        val autreEntite = entiteRepository.save(
            Entite(designation = "Ville de Lubumbashi", estVille = true, province = autreProvince)
        )
        val autreCommune = communeRepository.save(Commune(designation = "Kenya", entite = autreEntite))

        // Nettoyer d'abord tous les actes existants
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        // Créer des enfants uniques pour Kinshasa
        val enfantsKinshasa = creerEnfantsUniques(3, "KINSHASA")
        
        // Actes dans Kinshasa
        repeat(3) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${200 + index}",
                    enfant = enfantsKinshasa[index],
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // Créer des enfants uniques pour Haut-Katanga
        val enfantsKatanga = creerEnfantsUniques(2, "KATANGA")
        
        // Actes dans Haut-Katanga
        repeat(2) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "LBV/2020/${300 + index}",
                    enfant = enfantsKatanga[index],
                    commune = autreCommune,
                    officier = "MWANZA Paul",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // When
        val pageable = PageRequest.of(0, 10)
        val actesKinshasa = acteNaissanceRepository.findByProvinceId(province.id ?: 0L, pageable)
        val actesKatanga = acteNaissanceRepository.findByProvinceId(autreProvince.id ?: 0L, pageable)

        // Then
        assertEquals(3, actesKinshasa.totalElements)
        assertEquals(2, actesKatanga.totalElements)
    }

    @Test
    fun `devrait trouver les actes par ID d'entité`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(4, "KIN/2020/", "ENTITE", 400)

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.findByEntiteId(entite.id ?: 0L, pageable)

        // Then
        assertEquals(4, result.totalElements)
        result.content.forEach { acte ->
            assertEquals(entite.id, acte.commune.entite.id)
        }
    }

    // ====== TESTS DES RECHERCHES MULTICRITÈRES ======

    @Test
    fun `devrait effectuer une recherche avec plusieurs critères`() {
        // Given - Créer des actes avec différentes données
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/500",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                declarant = "MUKENDI Jacques",
                dateEnregistrement = LocalDate.of(2020, 6, 15)
            )
        )

        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/501",
                enfant = enfantFeminin,
                commune = commune,
                officier = "MWANZA Paul",
                declarant = "KASONGO Pierre",
                dateEnregistrement = LocalDate.of(2020, 8, 20)
            )
        )

        // When - Recherche par nom d'enfant et officier
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.rechercheMulticriteres(
            numeroActe = null,
            nomEnfant = "MUKENDI",
            postnomEnfant = null,
            prenomEnfant = "Jean",
            communeNom = null,
            officier = "KABONGO",
            declarant = null,
            dateDebutEnreg = null,
            dateFinEnreg = null,
            dateDebutNaiss = null,
            dateFinNaiss = null,
            pageable = pageable
        )

        // Then
        assertEquals(1, result.totalElements)
        val acte = result.content.first()
        assertEquals("KIN/2020/500", acte.numeroActe)
        assertEquals("Jean", acte.enfant.prenom)
        assertTrue(acte.officier.contains("KABONGO"))
    }

    @Test
    fun `devrait rechercher par nom d'enfant`() {
        // Given - Nettoyer d'abord et créer des enfants avec le nom MUKENDI
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val enfants = (1..3).map { index ->
            val nouvelEnfant = Personne(
                nom = "MUKENDI", // Même nom pour tester la recherche
                postnom = "ENFANT${index}",
                prenom = if (index < 2) "Jean${index}" else "Marie${index}",
                sexe = if (index < 2) Sexe.MASCULIN else Sexe.FEMININ,
                dateNaissance = LocalDate.of(2020, 3, 10 + index)
            )
            personneRepository.save(nouvelEnfant)
        }
        
        repeat(3) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${600 + index}",
                    enfant = enfants[index],
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.rechercherParNomEnfant("MUKENDI", pageable)

        // Then
        assertEquals(3, result.totalElements)
        result.content.forEach { acte ->
            assertTrue(acte.enfant.nom.contains("MUKENDI"))
        }
    }

    // ====== TESTS DES RECHERCHES PAR SEXE ======

    @Test
    fun `devrait trouver les actes par sexe d'enfant`() {
        // Given - Créer plusieurs actes avec des enfants de sexes différents
        repeat(3) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${700 + index}",
                    enfant = enfantMasculin,
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now().minusDays(index.toLong())
                )
            )
        }

        repeat(2) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${800 + index}",
                    enfant = enfantFeminin,
                    commune = commune,
                    officier = "MWANZA Paul",
                    dateEnregistrement = LocalDate.now().minusDays(index.toLong())
                )
            )
        }

        // When
        val pageable = PageRequest.of(0, 10)
        val actesMasculins = acteNaissanceRepository.findBySexeEnfant(Sexe.MASCULIN, pageable)
        val actesFeminins = acteNaissanceRepository.findBySexeEnfant(Sexe.FEMININ, pageable)

        // Then
        assertEquals(3, actesMasculins.totalElements)
        assertEquals(2, actesFeminins.totalElements)

        actesMasculins.content.forEach { acte ->
            assertEquals(Sexe.MASCULIN, acte.enfant.sexe)
        }

        actesFeminins.content.forEach { acte ->
            assertEquals(Sexe.FEMININ, acte.enfant.sexe)
        }
    }

    @Test
    fun `devrait trouver les actes par sexe et province`() {
        // Given - Nettoyer d'abord et créer des enfants uniques
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val enfants = creerEnfantsUniques(2, "SEXEPROV")
        
        repeat(2) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${900 + index}",
                    enfant = enfants[index],
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.findBySexeEnfantAndProvinceId(
            Sexe.MASCULIN, 
            province.id ?: 0L, 
            pageable
        )

        // Then
        assertEquals(1, result.totalElements)
        val acte = result.content.first()
        assertEquals(Sexe.MASCULIN, acte.enfant.sexe)
        assertEquals(province.id, acte.commune.entite.province.id)
    }

    @Test
    fun `devrait trouver les actes par sexe et entité`() {
        // Given
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/950",
                enfant = enfantFeminin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.findBySexeEnfantAndEntiteId(
            Sexe.FEMININ, 
            entite.id ?: 0L, 
            pageable
        )

        // Then
        assertEquals(1, result.totalElements)
        val acte = result.content.first()
        assertEquals(Sexe.FEMININ, acte.enfant.sexe)
        assertEquals(entite.id, acte.commune.entite.id)
    }

    @Test
    fun `devrait trouver les actes par sexe et commune`() {
        // Given
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/960",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.findBySexeEnfantAndCommuneId(
            Sexe.MASCULIN, 
            commune.id ?: 0L, 
            pageable
        )

        // Then
        assertEquals(1, result.totalElements)
        val acte = result.content.first()
        assertEquals(Sexe.MASCULIN, acte.enfant.sexe)
        assertEquals(commune.id, acte.commune.id)
    }

    // ====== TESTS DES COMPTAGES ET STATISTIQUES ======

    @Test
    fun `devrait compter les actes correctement`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(5, "KIN/2020/", "COUNT", 1000)

        // When & Then
        assertEquals(5, acteNaissanceRepository.count())
    }

    @Test
    fun `devrait compter par commune`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(3, "KIN/2020/", "COMMUNE", 1100)

        // When
        val count = acteNaissanceRepository.countByCommune(commune)

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `devrait compter par ID de province`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(4, "KIN/2020/", "PROVINCE", 1200)

        // When
        val count = acteNaissanceRepository.countByProvinceId(province.id ?: 0L)

        // Then
        assertEquals(4, count)
    }

    @Test
    fun `devrait compter par ID d'entité`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(2, "KIN/2020/", "ENTITE", 1300)

        // When
        val count = acteNaissanceRepository.countByEntiteId(entite.id ?: 0L)

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `devrait compter par sexe d'enfant`() {
        // Given - Nettoyer d'abord tous les actes existants
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        // Créer explicitement 3 enfants masculins
        val enfantsMasculins = (1..3).map { index ->
            val enfant = Personne(
                nom = "MASC${index}",
                postnom = "ENFANT${index}",
                prenom = "Jean${index}",
                sexe = Sexe.MASCULIN, // Explicitement masculin
                dateNaissance = LocalDate.of(2020, 3, 10 + index)
            )
            personneRepository.save(enfant)
        }
        
        repeat(3) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${1400 + index}",
                    enfant = enfantsMasculins[index],
                    commune = commune,
                    officier = "KABONGO Jean-Pierre",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // Créer explicitement 2 enfants féminins
        val enfantsFeminins = (1..2).map { index ->
            val enfant = Personne(
                nom = "FEM${index}",
                postnom = "ENFANT${index}",
                prenom = "Marie${index}",
                sexe = Sexe.FEMININ, // Explicitement féminin
                dateNaissance = LocalDate.of(2020, 3, 20 + index)
            )
            personneRepository.save(enfant)
        }
        
        repeat(2) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/2020/${1500 + index}",
                    enfant = enfantsFeminins[index],
                    commune = commune,
                    officier = "MWANZA Paul",
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        // When
        val countMasculin = acteNaissanceRepository.countBySexeEnfant(Sexe.MASCULIN)
        val countFeminin = acteNaissanceRepository.countBySexeEnfant(Sexe.FEMININ)

        // Then
        assertEquals(3, countMasculin)
        assertEquals(2, countFeminin)
    }

    @Test
    fun `devrait compter par sexe et province`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(2, "KIN/2020/", "SEXEPROV", 1600)

        // When
        val countMasculin = acteNaissanceRepository.countBySexeEnfantAndProvinceId(Sexe.MASCULIN, province.id ?: 0L)
        val countFeminin = acteNaissanceRepository.countBySexeEnfantAndProvinceId(Sexe.FEMININ, province.id ?: 0L)

        // Then
        assertEquals(1, countMasculin)
        assertEquals(1, countFeminin)
    }

    @Test
    fun `devrait compter par sexe et entité`() {
        // Given - Nettoyer d'abord et créer un enfant féminin unique
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val enfant = Personne(
            nom = "ENTITE1",
            postnom = "ENFANT1",
            prenom = "Marie1",
            sexe = Sexe.FEMININ, // Explicitement féminin
            dateNaissance = LocalDate.of(2020, 3, 10)
        )
        val savedEnfant = personneRepository.save(enfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/1700",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val count = acteNaissanceRepository.countBySexeEnfantAndEntiteId(Sexe.FEMININ, entite.id ?: 0L)

        // Then
        assertEquals(1, count)
    }

    @Test
    fun `devrait compter par sexe et commune`() {
        // Given - Nettoyer d'abord et créer un enfant masculin unique
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val enfant = Personne(
            nom = "COMMUNE1",
            postnom = "ENFANT1",
            prenom = "Jean1",
            sexe = Sexe.MASCULIN, // Explicitement masculin
            dateNaissance = LocalDate.of(2020, 3, 10)
        )
        val savedEnfant = personneRepository.save(enfant)
        
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/1800",
                enfant = savedEnfant,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val count = acteNaissanceRepository.countBySexeEnfantAndCommuneId(Sexe.MASCULIN, commune.id ?: 0L)

        // Then
        assertEquals(1, count)
    }

    @Test
    fun `devrait compter par date d'enregistrement`() {
        // Given - Nettoyer d'abord et créer des enfants uniques
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        val enfants = creerEnfantsUniques(2, "DATE")

        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/1900",
                enfant = enfants[0],
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = today
            )
        )

        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/1901",
                enfant = enfants[1],
                commune = commune,
                officier = "MWANZA Paul",
                dateEnregistrement = yesterday
            )
        )

        // When
        val countToday = acteNaissanceRepository.countByDateEnregistrement(today)
        val countYesterday = acteNaissanceRepository.countByDateEnregistrement(yesterday)

        // Then
        assertEquals(1, countToday)
        assertEquals(1, countYesterday)
    }

    @Test
    fun `devrait compter par période d'enregistrement`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(3, "KIN/2020/", "PERIODE", 2000)

        // When
        val startDate = LocalDate.now().minusDays(5)
        val endDate = LocalDate.now()
        val count = acteNaissanceRepository.countByDateEnregistrementBetween(startDate, endDate)

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `devrait obtenir les statistiques par commune`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(3, "KIN/2020/", "STATS", 2100)

        // When
        val statistiques = acteNaissanceRepository.statistiquesParCommune()

        // Then
        assertTrue(statistiques.isNotEmpty())
        val stat = statistiques.find { it[0] == commune.designation }
        assertNotNull(stat)
        assertEquals(3L, stat[1])
    }

    @Test
    fun `devrait obtenir les statistiques par province`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(2, "KIN/2020/", "PROVSTATS", 2200)

        // When
        val statistiques = acteNaissanceRepository.statistiquesParProvince()

        // Then
        assertTrue(statistiques.isNotEmpty())
        val stat = statistiques.find { it[0] == province.designation }
        assertNotNull(stat)
        assertEquals(2L, stat[1])
    }

    @Test
    fun `devrait obtenir les statistiques par officier`() {
        // Given - Nettoyer d'abord et créer des enfants uniques
        acteNaissanceRepository.deleteAll()
        acteNaissanceRepository.flush()
        
        val officier1 = "KABONGO Jean-Pierre"
        val officier2 = "MWANZA Paul"
        
        val enfants = creerEnfantsUniques(3, "OFFICIER")

        repeat(2) { index ->
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "KIN/200/${2300 + index}",
                    enfant = enfants[index],
                    commune = commune,
                    officier = officier1,
                    dateEnregistrement = LocalDate.now()
                )
            )
        }

        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "KIN/2020/2400",
                enfant = enfants[2],
                commune = commune,
                officier = officier2,
                dateEnregistrement = LocalDate.now()
            )
        )

        // When
        val statistiques = acteNaissanceRepository.statistiquesParOfficier()

        // Then
        assertTrue(statistiques.size >= 2)
        
        val statOfficier1 = statistiques.find { it[0] == officier1 }
        val statOfficier2 = statistiques.find { it[0] == officier2 }
        
        assertNotNull(statOfficier1)
        assertNotNull(statOfficier2)
        assertEquals(2L, statOfficier1!![1])
        assertEquals(1L, statOfficier2!![1])
    }

    // ====== TESTS DES CAS LIMITES ET D'ERREUR ======

    @Test
    fun `devrait retourner un résultat vide quand aucune donnée ne correspond`() {
        // Given - Aucun acte correspondant aux critères

        // When
        val pageable = PageRequest.of(0, 10)
        val result = acteNaissanceRepository.findBySexeEnfant(Sexe.MASCULIN, pageable)

        // Then
        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `devrait gérer la pagination correctement`() {
        // Given - Utiliser la méthode utilitaire pour éviter les conflits
        creerActesAvecEnfantsUniques(10, "KIN/2020/", "PAGE", 3000)

        // When
        val firstPage = acteNaissanceRepository.findAll(PageRequest.of(0, 3))
        val secondPage = acteNaissanceRepository.findAll(PageRequest.of(1, 3))

        // Then
        assertEquals(10, firstPage.totalElements)
        assertEquals(3, firstPage.content.size)
        assertEquals(3, secondPage.content.size)
        assertTrue(firstPage.hasNext())
        assertNotEquals(firstPage.content[0].id, secondPage.content[0].id)
    }

    @Test
    fun `devrait respecter les contraintes d'unicité`() {
        // Given
        acteNaissanceRepository.save(
            ActeNaissance(
                numeroActe = "UNIQUE/2020/001",
                enfant = enfantMasculin,
                commune = commune,
                officier = "KABONGO Jean-Pierre",
                dateEnregistrement = LocalDate.now()
            )
        )

        // When & Then - Tenter de créer un acte avec le même numéro devrait échouer
        assertThrows<Exception> {
            acteNaissanceRepository.save(
                ActeNaissance(
                    numeroActe = "UNIQUE/2020/001", // Même numéro - devrait échouer
                    enfant = enfantFeminin,
                    commune = commune,
                    officier = "MWANZA Paul",
                    dateEnregistrement = LocalDate.now()
                )
            )
            acteNaissanceRepository.flush() // Force l'exécution SQL
        }
    }
}

package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests d'intégration pour PersonneRepository
 * Teste les requêtes personnalisées et la persistance des données
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class PersonneRepositoryIntegrationTest {

    @Autowired
    private lateinit var personneRepository: PersonneRepository

    @Autowired
    private lateinit var provinceRepository: ProvinceRepository

    @Autowired
    private lateinit var entiteRepository: EntiteRepository

    @Autowired
    private lateinit var communeRepository: CommuneRepository

    private lateinit var province: Province
    private lateinit var entite: Entite
    private lateinit var commune: Commune

    @BeforeEach
    fun setUp() {
        // Nettoyer la base de données (ordre important pour les FK)
        personneRepository.deleteAll()
        communeRepository.deleteAll()
        entiteRepository.deleteAll()
        provinceRepository.deleteAll()

        // Créer structure géographique de test
        province = provinceRepository.save(Province(designation = "Kinshasa"))
        entite = entiteRepository.save(Entite(designation = "Ville de Kinshasa", province = province, estVille = true))
        commune = communeRepository.save(Commune(designation = "Gombe", entite = entite))
    }

    // ====== TESTS DE CRÉATION ET RECHERCHE DE BASE ======

    @Test
    fun `test création et récupération d'une personne complète`() {
        // Given
        val personne = Personne(
            nom = "KABILA",
            postnom = "JOSEPH",
            prenom = "Laurent",
            sexe = Sexe.MASCULIN,
            lieuNaiss = "Lubumbashi",
            dateNaissance = LocalDate.of(1971, 6, 4),
            heureNaissance = LocalTime.of(10, 30),
            profession = "Président",
            nationalite = "Congolaise",
            communeChefferie = commune.designation,
            quartierGroup = "Centre-ville",
            avenueVillage = "Avenue de la République",
            celluleLocalite = "Cellule 1",
            telephone = "+243812345678",
            email = "laurent.kabila@presidency.cd",
            statut = StatutPersonne.VIVANT,
            situationMatrimoniale = SituationMatrimoniale.MARIE
        )

        // When
        val personneSauvee = personneRepository.save(personne)

        // Then
        assertNotNull(personneSauvee.id)
        assertTrue(personneSauvee.id > 0)

        val personneRecuperee = personneRepository.findById(personneSauvee.id).orElse(null)
        assertNotNull(personneRecuperee)
        assertEquals("KABILA", personneRecuperee.nom)
        assertEquals("JOSEPH", personneRecuperee.postnom)
        assertEquals("Laurent", personneRecuperee.prenom)
        assertEquals(Sexe.MASCULIN, personneRecuperee.sexe)
        assertEquals("Lubumbashi", personneRecuperee.lieuNaiss)
        assertEquals(LocalDate.of(1971, 6, 4), personneRecuperee.dateNaissance)
        assertEquals("Président", personneRecuperee.profession)
        assertEquals(StatutPersonne.VIVANT, personneRecuperee.statut)
    }

    @Test
    fun `test création personne avec relations familiales`() {
        // Given - Créer les parents d'abord
        val pere = personneRepository.save(Personne(
            nom = "KABILA",
            postnom = "LAURENT",
            prenom = "Désiré",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(1939, 11, 27),
            statut = StatutPersonne.DECEDE
        ))

        val mere = personneRepository.save(Personne(
            nom = "SIFA",
            postnom = "MAHANYA",
            prenom = "Marceline",
            sexe = Sexe.FEMININ,
            dateNaissance = LocalDate.of(1945, 3, 15),
            statut = StatutPersonne.VIVANT
        ))

        // When - Créer l'enfant avec les relations
        val enfant = personneRepository.save(Personne(
            nom = "KABILA",
            postnom = "JOSEPH",
            prenom = "Joseph",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(1971, 6, 4),
            pere = pere,
            mere = mere,
            statut = StatutPersonne.VIVANT
        ))

        // Then
        val enfantRecupere = personneRepository.findById(enfant.id).orElse(null)
        assertNotNull(enfantRecupere)
        assertNotNull(enfantRecupere.pere)
        assertNotNull(enfantRecupere.mere)
        assertEquals("LAURENT", enfantRecupere.pere?.postnom)
        assertEquals("MAHANYA", enfantRecupere.mere?.postnom)
    }

    // ====== TESTS DES RECHERCHES PERSONNALISÉES ======

    @Test
    fun `test recherche par nom avec pagination`() {
        // Given - Créer plusieurs personnes avec des noms similaires
        personneRepository.saveAll(listOf(
            Personne(nom = "MBUJI", postnom = "KALALA", sexe = Sexe.MASCULIN),
            Personne(nom = "MBEKI", postnom = "THABO", sexe = Sexe.MASCULIN),
            Personne(nom = "MBALA", postnom = "MUANDA", sexe = Sexe.FEMININ),
            Personne(nom = "KABILA", postnom = "JOSEPH", sexe = Sexe.MASCULIN),
            Personne(nom = "KABONGO", postnom = "MARIE", sexe = Sexe.FEMININ)
        ))

        // When - Rechercher par "MB"
        val pageable = PageRequest.of(0, 3)
        val resultats = personneRepository.rechercherParNom("MB", pageable)

        // Then
        assertEquals(3, resultats.content.size) // 3 personnes trouvées (MBUJI, MBEKI, MBALA)
        assertTrue(resultats.content.any { it.nom == "MBUJI" })
        assertTrue(resultats.content.any { it.nom == "MBEKI" })
        assertTrue(resultats.content.any { it.nom == "MBALA" })
    }

    @Test
    fun `test recherche multicritères avec sexe et statut`() {
        // Given - Créer personnes avec différents critères
        personneRepository.saveAll(listOf(
            Personne(nom = "HOMME", postnom = "VIVANT", sexe = Sexe.MASCULIN, statut = StatutPersonne.VIVANT),
            Personne(nom = "HOMME", postnom = "DECEDE", sexe = Sexe.MASCULIN, statut = StatutPersonne.DECEDE),
            Personne(nom = "FEMME", postnom = "VIVANTE", sexe = Sexe.FEMININ, statut = StatutPersonne.VIVANT),
            Personne(nom = "FEMME", postnom = "DECEDEE", sexe = Sexe.FEMININ, statut = StatutPersonne.DECEDE)
        ))

        // When - Rechercher hommes vivants
        val pageable = PageRequest.of(0, 10)
        val resultats = personneRepository.rechercheMulticriteres(
            nom = null,
            postnom = null,
            prenom = null,
            sexe = Sexe.MASCULIN,
            statut = StatutPersonne.VIVANT,
            commune = null,
            dateDebut = null,
            dateFin = null,
            pageable = pageable
        )

        // Then
        assertEquals(1, resultats.content.size)
        val personne = resultats.content[0]
        assertEquals(Sexe.MASCULIN, personne.sexe)
        assertEquals(StatutPersonne.VIVANT, personne.statut)
        assertEquals("VIVANT", personne.postnom)
    }

    @Test
    fun `test recherche par plage de dates de naissance`() {
        // Given - Créer personnes avec différentes dates
        personneRepository.saveAll(listOf(
            Personne(nom = "ANCIEN", postnom = "TRES", sexe = Sexe.MASCULIN, dateNaissance = LocalDate.of(1950, 1, 1)),
            Personne(nom = "BOOMER", postnom = "OK", sexe = Sexe.MASCULIN, dateNaissance = LocalDate.of(1960, 6, 15)),
            Personne(nom = "MILLENNIAL", postnom = "GEN", sexe = Sexe.FEMININ, dateNaissance = LocalDate.of(1990, 12, 25)),
            Personne(nom = "ZOOMER", postnom = "GEN", sexe = Sexe.MASCULIN, dateNaissance = LocalDate.of(2000, 8, 10))
        ))

        // When - Rechercher personnes nées entre 1955 et 1995
        val pageable = PageRequest.of(0, 10)
        val dateDebut = LocalDate.of(1955, 1, 1)
        val dateFin = LocalDate.of(1995, 12, 31)

        val resultats = personneRepository.rechercheMulticriteres(
            nom = null, postnom = null, prenom = null, sexe = null, statut = null, commune = null,
            dateDebut = dateDebut, dateFin = dateFin, pageable = pageable
        )

        // Then
        assertEquals(2, resultats.content.size) // BOOMER et MILLENNIAL
        assertTrue(resultats.content.any { it.nom == "BOOMER" })
        assertTrue(resultats.content.any { it.nom == "MILLENNIAL" })
    }

    @Test
    fun `test recherche par commune`() {
        // Given - Créer personnes de différentes communes
        personneRepository.saveAll(listOf(
            Personne(nom = "GOMBE", postnom = "RESIDENT", sexe = Sexe.MASCULIN, communeChefferie = "Gombe"),
            Personne(nom = "KALAMU", postnom = "RESIDENT", sexe = Sexe.FEMININ, communeChefferie = "Kalamu"),
            Personne(nom = "NGALIEMA", postnom = "RESIDENT", sexe = Sexe.MASCULIN, communeChefferie = "Ngaliema"),
            Personne(nom = "AUTRE", postnom = "GOMBE", sexe = Sexe.FEMININ, communeChefferie = "Gombe")
        ))

        // When - Rechercher résidents de Gombe
        val pageable = PageRequest.of(0, 10)
        val resultats = personneRepository.rechercheMulticriteres(
            nom = null, postnom = null, prenom = null, sexe = null, statut = null,
            commune = "Gombe", dateDebut = null, dateFin = null, pageable = pageable
        )

        // Then
        assertEquals(2, resultats.content.size)
        assertTrue(resultats.content.all { it.communeChefferie == "Gombe" })
    }

    // ====== TESTS DES RELATIONS FAMILIALES ======

    @Test
    fun `test recherche enfants par père`() {
        // Given - Créer famille
        val pere = personneRepository.save(Personne(
            nom = "TSHISEKEDI",
            postnom = "ETIENNE",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(1932, 12, 14)
        ))

        val enfant1 = personneRepository.save(Personne(
            nom = "TSHISEKEDI",
            postnom = "FELIX",
            sexe = Sexe.MASCULIN,
            pere = pere,
            dateNaissance = LocalDate.of(1962, 6, 13)
        ))

        val enfant2 = personneRepository.save(Personne(
            nom = "TSHISEKEDI",
            postnom = "MARIE",
            sexe = Sexe.FEMININ,
            pere = pere,
            dateNaissance = LocalDate.of(1965, 3, 8)
        ))

        // When
        val pageable = PageRequest.of(0, 10)
        val enfants = personneRepository.findByPere(pere, pageable)

        // Then
        assertEquals(2, enfants.content.size)
        assertTrue(enfants.content.any { it.postnom == "FELIX" })
        assertTrue(enfants.content.any { it.postnom == "MARIE" })
    }

    @Test
    fun `test recherche enfants par mère`() {
        // Given - Créer famille avec mère
        val mere = personneRepository.save(Personne(
            nom = "MULUMBA",
            postnom = "MARIE",
            sexe = Sexe.FEMININ,
            dateNaissance = LocalDate.of(1940, 5, 20)
        ))

        val enfant = personneRepository.save(Personne(
            nom = "KASONGO",
            postnom = "JEAN",
            sexe = Sexe.MASCULIN,
            mere = mere,
            dateNaissance = LocalDate.of(1970, 2, 10)
        ))

        // When
        val pageable = PageRequest.of(0, 10)
        val enfants = personneRepository.findByMere(mere, pageable)

        // Then
        assertEquals(1, enfants.content.size)
        assertEquals("JEAN", enfants.content[0].postnom)
    }

    // ====== TESTS DE VÉRIFICATION DES DOUBLONS ======

    @Test
    fun `test détection doublons avec même identité complète`() {
        // Given - Créer première personne
        val personne1 = personneRepository.save(Personne(
            nom = "MUKENDI",
            postnom = "JOSEPH",
            prenom = "Pierre",
            sexe = Sexe.MASCULIN,
            dateNaissance = LocalDate.of(1985, 7, 12)
        ))

        // When & Then - Vérifier qu'un doublon est détecté
        val existeDeja = personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(
            "MUKENDI", "JOSEPH", "Pierre", LocalDate.of(1985, 7, 12)
        )
        assertTrue(existeDeja)

        // Et qu'un non-doublon n'est pas détecté
        val existePas = personneRepository.existsByNomAndPostnomAndPrenomAndDateNaissance(
            "MUKENDI", "JOSEPH", "Pierre", LocalDate.of(1985, 7, 13) // Date différente
        )
        assertTrue(!existePas)
    }

    @Test
    fun `test recherche par identité exacte`() {
        // Given
        val personne = personneRepository.save(Personne(
            nom = "NGOZI",
            postnom = "MARIE",
            prenom = "Grace",
            sexe = Sexe.FEMININ,
            dateNaissance = LocalDate.of(1992, 11, 3)
        ))

        // When
        val personneTrouvee = personneRepository.findByNomAndPostnomAndPrenomAndDateNaissance(
            "NGOZI", "MARIE", "Grace", LocalDate.of(1992, 11, 3)
        )

        // Then
        assertNotNull(personneTrouvee)
        assertEquals(personne.id, personneTrouvee!!.id)
        assertEquals("NGOZI", personneTrouvee.nom)
    }

    // ====== TESTS DES STATISTIQUES ET COMPTAGES ======

    @Test
    fun `test comptage par sexe`() {
        // Given - Créer personnes des deux sexes
        personneRepository.saveAll(listOf(
            Personne(nom = "HOMME1", postnom = "TEST", sexe = Sexe.MASCULIN),
            Personne(nom = "HOMME2", postnom = "TEST", sexe = Sexe.MASCULIN),
            Personne(nom = "HOMME3", postnom = "TEST", sexe = Sexe.MASCULIN),
            Personne(nom = "FEMME1", postnom = "TEST", sexe = Sexe.FEMININ),
            Personne(nom = "FEMME2", postnom = "TEST", sexe = Sexe.FEMININ)
        ))

        // When & Then
        val nombreHommes = personneRepository.countBySexe(Sexe.MASCULIN)
        val nombreFemmes = personneRepository.countBySexe(Sexe.FEMININ)

        assertEquals(3, nombreHommes)
        assertEquals(2, nombreFemmes)
    }

    @Test
    fun `test comptage par statut`() {
        // Given
        personneRepository.saveAll(listOf(
            Personne(nom = "VIVANT1", postnom = "TEST", sexe = Sexe.MASCULIN, statut = StatutPersonne.VIVANT),
            Personne(nom = "VIVANT2", postnom = "TEST", sexe = Sexe.FEMININ, statut = StatutPersonne.VIVANT),
            Personne(nom = "DECEDE1", postnom = "TEST", sexe = Sexe.MASCULIN, statut = StatutPersonne.DECEDE)
        ))

        // When & Then
        val nombreVivants = personneRepository.countByStatut(StatutPersonne.VIVANT)
        val nombreDecedes = personneRepository.countByStatut(StatutPersonne.DECEDE)

        assertEquals(2, nombreVivants)
        assertEquals(1, nombreDecedes)
    }

    @Test
    fun `test comptage par situation matrimoniale`() {
        // Given
        personneRepository.saveAll(listOf(
            Personne(nom = "CELIBATAIRE1", postnom = "TEST", sexe = Sexe.MASCULIN, situationMatrimoniale = SituationMatrimoniale.CELIBATAIRE),
            Personne(nom = "MARIE1", postnom = "TEST", sexe = Sexe.FEMININ, situationMatrimoniale = SituationMatrimoniale.MARIE),
            Personne(nom = "MARIE2", postnom = "TEST", sexe = Sexe.MASCULIN, situationMatrimoniale = SituationMatrimoniale.MARIE),
            Personne(nom = "DIVORCE1", postnom = "TEST", sexe = Sexe.FEMININ, situationMatrimoniale = SituationMatrimoniale.DIVORCE)
        ))

        // When & Then
        val nombreCelibataires = personneRepository.countBySituationMatrimoniale(SituationMatrimoniale.CELIBATAIRE)
        val nombreMaries = personneRepository.countBySituationMatrimoniale(SituationMatrimoniale.MARIE)
        val nombreDivorces = personneRepository.countBySituationMatrimoniale(SituationMatrimoniale.DIVORCE)

        assertEquals(1, nombreCelibataires)
        assertEquals(2, nombreMaries)
        assertEquals(1, nombreDivorces)
    }

    @Test
    fun `test statistiques démographiques par sexe`() {
        // Given - Créer personnes vivantes et décédées
        personneRepository.saveAll(listOf(
            Personne(nom = "H_VIVANT", postnom = "TEST", sexe = Sexe.MASCULIN, statut = StatutPersonne.VIVANT),
            Personne(nom = "H_DECEDE", postnom = "TEST", sexe = Sexe.MASCULIN, statut = StatutPersonne.DECEDE),
            Personne(nom = "F_VIVANTE", postnom = "TEST", sexe = Sexe.FEMININ, statut = StatutPersonne.VIVANT),
            Personne(nom = "F_VIVANTE2", postnom = "TEST", sexe = Sexe.FEMININ, statut = StatutPersonne.VIVANT)
        ))

        // When
        val statistiques = personneRepository.statistiquesParSexe()

        // Then
        assertEquals(2, statistiques.size) // 2 sexes
        
        val statHommes = statistiques.find { it[0] == Sexe.MASCULIN }
        val statFemmes = statistiques.find { it[0] == Sexe.FEMININ }
        
        assertNotNull(statHommes)
        assertNotNull(statFemmes)
        assertEquals(1L, statHommes!![1]) // 1 homme vivant
        assertEquals(2L, statFemmes!![1]) // 2 femmes vivantes
    }

    @Test
    fun `test comptage mineurs et majeurs`() {
        // Given - Créer personnes de différents âges
        val aujourd_hui = LocalDate.now()
        val dateLimite18ans = aujourd_hui.minusYears(18)
        
        personneRepository.saveAll(listOf(
            // Mineurs (moins de 18 ans)
            Personne(nom = "MINEUR1", postnom = "TEST", sexe = Sexe.MASCULIN, 
                    dateNaissance = aujourd_hui.minusYears(10), statut = StatutPersonne.VIVANT),
            Personne(nom = "MINEUR2", postnom = "TEST", sexe = Sexe.FEMININ, 
                    dateNaissance = aujourd_hui.minusYears(15), statut = StatutPersonne.VIVANT),
            
            // Majeurs (18 ans et plus)
            Personne(nom = "MAJEUR1", postnom = "TEST", sexe = Sexe.MASCULIN, 
                    dateNaissance = aujourd_hui.minusYears(25), statut = StatutPersonne.VIVANT),
            Personne(nom = "MAJEUR2", postnom = "TEST", sexe = Sexe.FEMININ, 
                    dateNaissance = aujourd_hui.minusYears(30), statut = StatutPersonne.VIVANT),
            
            // Personne décédée (ne doit pas être comptée)
            Personne(nom = "DECEDE", postnom = "TEST", sexe = Sexe.MASCULIN, 
                    dateNaissance = aujourd_hui.minusYears(20), statut = StatutPersonne.DECEDE)
        ))

        // When & Then
        val nombreMineurs = personneRepository.countMineurs(dateLimite18ans)
        val nombreMajeurs = personneRepository.countMajeurs(dateLimite18ans)

        assertEquals(2, nombreMineurs) // 2 mineurs vivants
        assertEquals(2, nombreMajeurs) // 2 majeurs vivants
    }
}


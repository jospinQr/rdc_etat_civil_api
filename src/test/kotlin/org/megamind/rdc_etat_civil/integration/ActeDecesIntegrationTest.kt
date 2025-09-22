package org.megamind.rdc_etat_civil.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.megamind.rdc_etat_civil.deces.ActeDecesService
import org.megamind.rdc_etat_civil.deces.dto.ActeDecesRequest
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import org.megamind.rdc_etat_civil.personne.dto.PersonneRequest
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActeDecesIntegrationTest {

    @Autowired
    private lateinit var acteDecesService: ActeDecesService

    @Autowired
    private lateinit var personneService: PersonneService

    @Autowired
    private lateinit var communeRepository: CommuneRepository

    @Autowired
    private lateinit var entiteRepository: EntiteRepository

    @Autowired
    private lateinit var provinceRepository: ProvinceRepository

    @Test
    fun `création d'acte de décès devrait mettre à jour automatiquement le statut de la personne`() {
        // Given - Créer une personne vivante
        val personneRequest = PersonneRequest(
            nom = "TEST",
            postnom = "PERSONNE",
            prenom = "Jean",
            sexe = org.megamind.rdc_etat_civil.personne.Sexe.MASCULIN,
            dateNaissance = LocalDate.now().minusYears(30)
        )
        val personneCreee = personneService.creerPersonne(personneRequest)
        assertEquals(StatutPersonne.VIVANT, personneCreee.statut)

        // Créer une commune de test
        val province = org.megamind.rdc_etat_civil.territoire.province.Province(
            designation = "Province Test"
        )
        val provinceSauvee = provinceRepository.save(province)

        val entite = org.megamind.rdc_etat_civil.territoire.entite.Entite(
            designation = "Entité Test",
            province = provinceSauvee
        )
        val entiteSauvee = entiteRepository.save(entite)

        val commune = org.megamind.rdc_etat_civil.territoire.commune.Commune(
            designation = "Commune Test",
            entite = entiteSauvee
        )
        val communeSauvee = communeRepository.save(commune)

        // When - Créer un acte de décès
        val acteRequest = ActeDecesRequest(
            numeroActe = "DEC-INTEGRATION-001",
            defuntId = personneCreee.id,
            communeId = communeSauvee.id,
            dateDeces = LocalDate.now().minusDays(1),
            lieuDeces = "Hôpital Test",
            officier = "Dr. Test"
        )
        val acteCree = acteDecesService.creerActeDeces(acteRequest)

        // Then - Vérifier que l'acte a été créé
        assertNotNull(acteCree)
        assertEquals("DEC-INTEGRATION-001", acteCree.numeroActe)

        // Vérifier que le statut de la personne a été mis à jour
        val personneMiseAJour = personneService.obtenirPersonne(personneCreee.id)
        assertEquals(StatutPersonne.DECEDE, personneMiseAJour.statut)
    }

    @Test
    fun `suppression d'acte de décès devrait restaurer le statut de la personne`() {
        // Given - Créer une personne et un acte de décès
        val personneRequest = PersonneRequest(
            nom = "TEST",
            postnom = "PERSONNE",
            prenom = "Marie",
            sexe = org.megamind.rdc_etat_civil.personne.Sexe.FEMININ,
            dateNaissance = LocalDate.now().minusYears(25)
        )
        val personneCreee = personneService.creerPersonne(personneRequest)

        // Créer une commune de test
        val province = org.megamind.rdc_etat_civil.territoire.province.Province(
            designation = "Province Test 2"
        )
        val provinceSauvee = provinceRepository.save(province)

        val entite = org.megamind.rdc_etat_civil.territoire.entite.Entite(
            designation = "Entité Test 2",
            province = provinceSauvee
        )
        val entiteSauvee = entiteRepository.save(entite)

        val commune = org.megamind.rdc_etat_civil.territoire.commune.Commune(
            designation = "Commune Test 2",
            entite = entiteSauvee
        )
        val communeSauvee = communeRepository.save(commune)

        val acteRequest = ActeDecesRequest(
            numeroActe = "DEC-INTEGRATION-002",
            defuntId = personneCreee.id,
            communeId = communeSauvee.id,
            dateDeces = LocalDate.now().minusDays(2),
            lieuDeces = "Centre de Santé",
            officier = "Dr. Test 2"
        )
        val acteCree = acteDecesService.creerActeDeces(acteRequest)

        // Vérifier que le statut est bien DÉCÉDÉ
        val personneApresCreation = personneService.obtenirPersonne(personneCreee.id)
        assertEquals(StatutPersonne.DECEDE, personneApresCreation.statut)

        // When - Supprimer l'acte de décès
        acteDecesService.supprimerActeDeces(acteCree.id)

        // Then - Vérifier que le statut a été restauré
        val personneApresSuppression = personneService.obtenirPersonne(personneCreee.id)
        assertEquals(StatutPersonne.VIVANT, personneApresSuppression.statut)
    }
}

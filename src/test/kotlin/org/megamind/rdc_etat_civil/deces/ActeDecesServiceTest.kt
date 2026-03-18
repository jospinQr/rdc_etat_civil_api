package org.megamind.rdc_etat_civil.deces

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.deces.dto.ActeDecesRequest
import org.megamind.rdc_etat_civil.deces.dto.ActeDecesSearchCriteria
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import org.megamind.rdc_etat_civil.testsupport.Fixtures
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

@Tag("unit")
class ActeDecesServiceTest {

    private val acteRepo: ActeDecesRepository = mock()
    private val personneRepo: PersonneRepository = mock()
    private val communeRepo: CommuneRepository = mock()
    private val entiteRepo: EntiteRepository = mock()
    private val provinceRepo: ProvinceRepository = mock()
    private val personneService: PersonneService = mock()

    private val service = ActeDecesService(
        acteDecesRepository = acteRepo,
        personneRepository = personneRepo,
        communeRepository = communeRepo,
        entiteRepository = entiteRepo,
        provinceRepository = provinceRepo,
        personneService = personneService
    )

    @Test
    fun `creerActeDeces - numero existe deja - throw`() {
        whenever(acteRepo.existsByNumeroActe("AD-2026-0001")).thenReturn(true)

        val ex = assertThrows(BadRequestException::class.java) {
            service.creerActeDeces(
                ActeDecesRequest(
                    numeroActe = "AD-2026-0001",
                    defuntId = 1L,
                    communeId = 1L,
                    dateDeces = LocalDate.now().minusDays(1),
                    heureDeces = null,
                    lieuDeces = "Hôpital",
                    causeDeces = null,
                    officier = "OEC",
                    declarant = null,
                    dateEnregistrement = LocalDate.now(),
                    temoin1 = null,
                    temoin2 = null,
                    medecin = null,
                    observations = null
                )
            )
        }
        assertTrue(ex.message!!.contains("existe déjà"))
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeDeces - defunt inexistant - throw`() {
        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) {
            service.creerActeDeces(
                ActeDecesRequest(
                    numeroActe = "AD-2026-0001",
                    defuntId = 1L,
                    communeId = 1L,
                    dateDeces = LocalDate.now().minusDays(1),
                    heureDeces = null,
                    lieuDeces = "Hôpital",
                    causeDeces = null,
                    officier = "OEC",
                    declarant = null,
                    dateEnregistrement = LocalDate.now(),
                    temoin1 = null,
                    temoin2 = null,
                    medecin = null,
                    observations = null
                )
            )
        }
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeDeces - defunt a deja un acte - throw`() {
        val defunt = Fixtures.personne(id = 1L)
        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(defunt))
        whenever(acteRepo.existsByDefunt(defunt)).thenReturn(true)

        assertThrows(BadRequestException::class.java) {
            service.creerActeDeces(
                ActeDecesRequest(
                    numeroActe = "AD-2026-0001",
                    defuntId = 1L,
                    communeId = 1L,
                    dateDeces = LocalDate.now().minusDays(1),
                    heureDeces = null,
                    lieuDeces = "Hôpital",
                    causeDeces = null,
                    officier = "OEC",
                    declarant = null,
                    dateEnregistrement = LocalDate.now(),
                    temoin1 = null,
                    temoin2 = null,
                    medecin = null,
                    observations = null
                )
            )
        }
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeDeces - success - sauvegarde et met statut DECEDE`() {
        val defunt = Fixtures.personne(id = 1L, dateNaissance = LocalDate.now().minusYears(30))
        val commune = Fixtures.commune(id = 1L)

        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(defunt))
        whenever(acteRepo.existsByDefunt(defunt)).thenReturn(false)
        whenever(communeRepo.findById(1L)).thenReturn(Optional.of(commune))

        whenever(acteRepo.save(any<ActeDeces>())).thenAnswer { it.arguments[0] as ActeDeces }

        val response = service.creerActeDeces(
            ActeDecesRequest(
                numeroActe = "AD-2026-0001",
                defuntId = 1L,
                communeId = 1L,
                dateDeces = LocalDate.now().minusDays(5),
                heureDeces = LocalTime.of(14, 30),
                lieuDeces = " Hôpital Général ",
                causeDeces = " Maladie ",
                officier = " OEC MUKENDI ",
                declarant = " DECLARANT ",
                dateEnregistrement = LocalDate.now(),
                temoin1 = " T1 ",
                temoin2 = " T2 ",
                medecin = " Dr. X ",
                observations = " RAS "
            )
        )

        assertEquals("AD-2026-0001", response.numeroActe)
        verify(personneService).changerStatut(1L, StatutPersonne.DECEDE)

        argumentCaptor<ActeDeces>().apply {
            verify(acteRepo).save(capture())
            assertEquals("AD-2026-0001", firstValue.numeroActe)
            assertEquals("Hôpital Général", firstValue.lieuDeces)
            assertEquals("Maladie", firstValue.causeDeces)
            assertEquals("OEC MUKENDI", firstValue.officier)
        }
    }

    @Test
    fun `rechercherActes - delegue au repository et mappe en simple`() {
        val pageReq = PageRequest.of(0, 20)
        val acte = Fixtures.acteDeces(id = 1L)

        whenever(
            acteRepo.rechercheMulticriteres(
                anyOrNull(), // numeroActe
                anyOrNull(), // nomDefunt
                anyOrNull(), // postnomDefunt
                anyOrNull(), // prenomDefunt
                anyOrNull(), // officier
                anyOrNull(), // declarant
                anyOrNull(), // medecin
                anyOrNull(), // dateDebutDeces
                anyOrNull(), // dateFinDeces
                anyOrNull(), // dateDebutEnreg
                anyOrNull(), // dateFinEnreg
                any()
            )
        ).thenReturn(PageImpl(listOf(acte), pageReq, 1))

        val result = service.rechercherActes(
            ActeDecesSearchCriteria(page = 0, size = 20)
        )

        assertEquals(1, result.totalElements)
        assertEquals("AD-2026-0001", result.content.first().numeroActe)
        verify(acteRepo).rechercheMulticriteres(
            numeroActe = isNull(),
            nomDefunt = isNull(),
            postnomDefunt = isNull(),
            prenomDefunt = isNull(),
            officier = isNull(),
            declarant = isNull(),
            medecin = isNull(),
            dateDebutDeces = isNull(),
            dateFinDeces = isNull(),
            dateDebutEnreg = isNull(),
            dateFinEnreg = isNull(),
            pageable = any()
        )
    }
}


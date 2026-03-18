package org.megamind.rdc_etat_civil.mariage

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.mariage.dto.ActeMariageRequest
import org.megamind.rdc_etat_civil.mariage.dto.ActeMariageSearchCriteria
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.testsupport.Fixtures
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.Optional

@Tag("unit")
class ActeMariageServiceTest {

    private val acteRepo: ActeMariageRepository = mock()
    private val personneRepo: PersonneRepository = mock()
    private val communeRepo: CommuneRepository = mock()
    private val entiteRepo: EntiteRepository = mock()
    private val provinceRepo: ProvinceRepository = mock()

    private val service = ActeMariageService(
        acteMariageRepository = acteRepo,
        personneRepository = personneRepo,
        communeRepository = communeRepo,
        entiteRepository = entiteRepo,
        provinceRepository = provinceRepo
    )

    @Test
    fun `creerActeMariage - numero existe deja - throw`() {
        whenever(acteRepo.existsByNumeroActe("AM-2026-0001")).thenReturn(true)

        assertThrows(BadRequestException::class.java) {
            service.creerActeMariage(
                ActeMariageRequest(
                    numeroActe = "AM-2026-0001",
                    epouxId = 1L,
                    epouseId = 2L,
                    communeId = 1L,
                    dateMariage = LocalDate.now().minusDays(1),
                    lieuMariage = "Lieu",
                    regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                    officier = "OEC",
                    temoin1 = null,
                    temoin2 = null
                )
            )
        }
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeMariage - epoux introuvable - throw`() {
        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) {
            service.creerActeMariage(
                ActeMariageRequest(
                    numeroActe = "AM-2026-0001",
                    epouxId = 1L,
                    epouseId = 2L,
                    communeId = 1L,
                    dateMariage = LocalDate.now().minusDays(1),
                    lieuMariage = "Lieu",
                    regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                    officier = "OEC"
                )
            )
        }
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeMariage - refuse si une personne deja mariee`() {
        val epoux = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN, dateNaissance = LocalDate.now().minusYears(30))
        val epouse = Fixtures.personne(id = 2L, sexe = Sexe.FEMININ, dateNaissance = LocalDate.now().minusYears(28))

        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(epoux))
        whenever(personneRepo.findById(2L)).thenReturn(Optional.of(epouse))
        whenever(acteRepo.estPersonneMariee(1L)).thenReturn(true)

        assertThrows(BadRequestException::class.java) {
            service.creerActeMariage(
                ActeMariageRequest(
                    numeroActe = "AM-2026-0001",
                    epouxId = 1L,
                    epouseId = 2L,
                    communeId = 1L,
                    dateMariage = LocalDate.now().minusDays(1),
                    lieuMariage = "Lieu",
                    regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                    officier = "OEC"
                )
            )
        }
        verify(acteRepo, never()).save(any())
    }

    @Test
    fun `creerActeMariage - refuse si epoux et epouse identiques`() {
        val p = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN, dateNaissance = LocalDate.now().minusYears(30))
        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(p))
        whenever(acteRepo.estPersonneMariee(any())).thenReturn(false)
        whenever(communeRepo.findById(1L)).thenReturn(Optional.of(Fixtures.commune(1L)))

        assertThrows(BadRequestException::class.java) {
            service.creerActeMariage(
                ActeMariageRequest(
                    numeroActe = "AM-2026-0001",
                    epouxId = 1L,
                    epouseId = 1L,
                    communeId = 1L,
                    dateMariage = LocalDate.now().minusDays(1),
                    lieuMariage = "Lieu",
                    regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                    officier = "OEC"
                )
            )
        }
    }

    @Test
    fun `creerActeMariage - age minimum 18 ans`() {
        val epoux = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN, dateNaissance = LocalDate.now().minusYears(17))
        val epouse = Fixtures.personne(id = 2L, sexe = Sexe.FEMININ, dateNaissance = LocalDate.now().minusYears(28))

        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(epoux))
        whenever(personneRepo.findById(2L)).thenReturn(Optional.of(epouse))
        whenever(acteRepo.estPersonneMariee(any())).thenReturn(false)
        whenever(communeRepo.findById(1L)).thenReturn(Optional.of(Fixtures.commune(1L)))

        assertThrows(BadRequestException::class.java) {
            service.creerActeMariage(
                ActeMariageRequest(
                    numeroActe = "AM-2026-0001",
                    epouxId = 1L,
                    epouseId = 2L,
                    communeId = 1L,
                    dateMariage = LocalDate.now().minusDays(1),
                    lieuMariage = "Lieu",
                    regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                    officier = "OEC"
                )
            )
        }
    }

    @Test
    fun `creerActeMariage - success - sauvegarde et normalise numero`() {
        val epoux = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN, dateNaissance = LocalDate.now().minusYears(30))
        val epouse = Fixtures.personne(id = 2L, sexe = Sexe.FEMININ, dateNaissance = LocalDate.now().minusYears(28))
        val commune = Fixtures.commune(1L)

        whenever(acteRepo.existsByNumeroActe(any())).thenReturn(false)
        whenever(personneRepo.findById(1L)).thenReturn(Optional.of(epoux))
        whenever(personneRepo.findById(2L)).thenReturn(Optional.of(epouse))
        whenever(acteRepo.estPersonneMariee(any())).thenReturn(false)
        whenever(communeRepo.findById(1L)).thenReturn(Optional.of(commune))
        whenever(acteRepo.save(any<ActeMariage>())).thenAnswer { it.arguments[0] as ActeMariage }

        val response = service.creerActeMariage(
            ActeMariageRequest(
                numeroActe = "AM-2026-0001",
                epouxId = 1L,
                epouseId = 2L,
                communeId = 1L,
                dateMariage = LocalDate.now().minusDays(10),
                lieuMariage = " Commune de Gombe ",
                regimeMatrimonial = RegimeMatrimonial.COMMUNAUTE_LEGALE,
                officier = " OEC MUKENDI ",
                temoin1 = " T1 ",
                temoin2 = " T2 "
            )
        )

        assertEquals("AM-2026-0001", response.numeroActe)
        argumentCaptor<ActeMariage>().apply {
            verify(acteRepo).save(capture())
            assertEquals("AM-2026-0001", firstValue.numeroActe)
            assertEquals("Commune de Gombe", firstValue.lieuMariage)
            assertEquals("OEC MUKENDI", firstValue.officier)
        }
    }

    @Test
    fun `rechercherActes - delegue au repository et mappe en simple`() {
        val pageReq = PageRequest.of(0, 20)
        val acte = Fixtures.acteMariage(id = 1L)

        whenever(
            acteRepo.rechercheAvanceeMariageRDC(
                provinceId = anyOrNull(),
                entiteId = anyOrNull(),
                communeId = anyOrNull(),
                sexeEpoux = anyOrNull(),
                sexeEpouse = anyOrNull(),
                regimeMatrimonial = anyOrNull(),
                terme = anyOrNull(),
                dateDebut = anyOrNull(),
                dateFin = anyOrNull(),
                pageable = any()
            )
        ).thenReturn(PageImpl(listOf(acte), pageReq, 1))

        val result = service.rechercherActes(ActeMariageSearchCriteria(page = 0, size = 20))

        assertEquals(1, result.totalElements)
        assertEquals("AM-2026-0001", result.content.first().numeroActe)
    }
}


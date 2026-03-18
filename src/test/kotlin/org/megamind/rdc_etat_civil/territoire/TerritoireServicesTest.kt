package org.megamind.rdc_etat_civil.territoire

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.testsupport.Fixtures
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.commune.CommuneService
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteService
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceService
import org.megamind.rdc_etat_civil.territoire.quarier.QuartierRepository
import org.megamind.rdc_etat_civil.territoire.quarier.QuartierService
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

@Tag("unit")
class TerritoireServicesTest {

    @Test
    fun `ProvinceService - findAllProvinces retourne liste`() {
        val repo: ProvinceRepository = mock()
        whenever(repo.findAll()).thenReturn(listOf(Fixtures.province(1L), Fixtures.province(2L)))
        val service = ProvinceService(repo)

        val result = service.findAllProvinces()
        assertEquals(2, result.size)
    }

    @Test
    fun `EntiteService - findByProvince throw si vide`() {
        val repo: EntiteRepository = mock()
        whenever(repo.findByProvinceId(1L)).thenReturn(Optional.empty())
        val service = EntiteService(repo)

        assertThrows(EntityNotFoundException::class.java) {
            service.findByProvince(1L)
        }
    }

    @Test
    fun `CommuneService - findCommunesByEntite throw si vide`() {
        val repo: CommuneRepository = mock()
        whenever(repo.findByEntiteId(1L)).thenReturn(Optional.empty())
        val service = CommuneService(repo)

        assertThrows(EntityNotFoundException::class.java) {
            service.findCommunesByEntite(1L)
        }
    }

    @Test
    fun `QuartierService - findByEntity throw si vide`() {
        val repo: QuartierRepository = mock()
        whenever(repo.findByCommuneId(1L)).thenReturn(Optional.empty())
        val service = QuartierService(repo)

        assertThrows(EntityNotFoundException::class.java) {
            service.findByEntity(1L)
        }
    }
}


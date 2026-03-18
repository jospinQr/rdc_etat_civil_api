package org.megamind.rdc_etat_civil.personne

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.testsupport.Fixtures
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

@Tag("unit")
class PersonneServiceTest {

    private val repo: PersonneRepository = mock()
    private val service = PersonneService(repo)

    @Test
    fun `creerPersonne - normalise champs et sauvegarde`() {
        val request = Fixtures.personneRequest(
            nom = " Kabamba ",
            postnom = " Mulumba ",
            prenom = " Jean "
        )

        whenever(repo.findById(any())).thenReturn(Optional.empty())
        whenever(repo.save(any<Personne>())).thenAnswer { it.arguments[0] as Personne }

        val response = service.creerPersonne(request)

        assertEquals("KABAMBA", response.nom)
        assertEquals("MULUMBA", response.postnom)
        assertEquals("JEAN", response.prenom)
        assertEquals("jean.kabamba@example.com", response.email)

        argumentCaptor<Personne>().apply {
            verify(repo).save(capture())
            assertEquals("KABAMBA", firstValue.nom)
            assertEquals("MULUMBA", firstValue.postnom)
            assertEquals("JEAN", firstValue.prenom)
            assertEquals("jean.kabamba@example.com", firstValue.email)
        }
    }

    @Test
    fun `creerPersonne - pereId inexistant - throw EntityNotFoundException`() {
        val request = Fixtures.personneRequest(pereId = 99L)
        whenever(repo.findById(99L)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) {
            service.creerPersonne(request)
        }
        verify(repo, never()).save(any())
    }

    @Test
    fun `creerPersonne - pere doit etre masculin`() {
        val pere = Fixtures.personne(id = 99L, sexe = Sexe.FEMININ)
        val request = Fixtures.personneRequest(pereId = 99L)
        whenever(repo.findById(99L)).thenReturn(Optional.of(pere))

        assertThrows(BadRequestException::class.java) {
            service.creerPersonne(request)
        }
        verify(repo, never()).save(any())
    }

    @Test
    fun `creerPersonne - mere doit etre feminin`() {
        val mere = Fixtures.personne(id = 88L, sexe = Sexe.MASCULIN)
        val request = Fixtures.personneRequest(mereId = 88L)
        whenever(repo.findById(88L)).thenReturn(Optional.of(mere))

        assertThrows(BadRequestException::class.java) {
            service.creerPersonne(request)
        }
        verify(repo, never()).save(any())
    }

    @Test
    fun `supprimerPersonne - refuse suppression si enfants existent`() {
        val parent = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN)
        whenever(repo.findById(1L)).thenReturn(Optional.of(parent))
        whenever(repo.findByPere(eq(parent), any())).thenReturn(PageImpl(listOf(Fixtures.personne(id = 2L)), PageRequest.of(0, 1), 1))
        whenever(repo.findByMere(eq(parent), any())).thenReturn(PageImpl(emptyList(), PageRequest.of(0, 1), 0))

        assertThrows(IllegalStateException::class.java) {
            service.supprimerPersonne(1L)
        }
        verify(repo, never()).deleteById(any())
    }

    @Test
    fun `obtenirEnfants - utilise findByPere si parent masculin`() {
        val parent = Fixtures.personne(id = 1L, sexe = Sexe.MASCULIN)
        whenever(repo.findById(1L)).thenReturn(Optional.of(parent))
        whenever(repo.findByPere(eq(parent), any())).thenReturn(PageImpl(listOf(Fixtures.personne(id = 2L))))

        val page = service.obtenirEnfants(parentId = 1L, page = 0, size = 20)

        assertEquals(1, page.totalElements)
        verify(repo).findByPere(eq(parent), any())
        verify(repo, never()).findByMere(any(), any())
    }

    @Test
    fun `obtenirEnfants - utilise findByMere si parent feminin`() {
        val parent = Fixtures.personne(id = 1L, sexe = Sexe.FEMININ)
        whenever(repo.findById(1L)).thenReturn(Optional.of(parent))
        whenever(repo.findByMere(eq(parent), any())).thenReturn(PageImpl(listOf(Fixtures.personne(id = 2L))))

        val page = service.obtenirEnfants(parentId = 1L, page = 0, size = 20)

        assertEquals(1, page.totalElements)
        verify(repo).findByMere(eq(parent), any())
        verify(repo, never()).findByPere(any(), any())
    }
}


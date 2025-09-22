package org.megamind.rdc_etat_civil.unit

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.megamind.rdc_etat_civil.deces.ActeDeces
import org.megamind.rdc_etat_civil.deces.ActeDecesRepository
import org.megamind.rdc_etat_civil.deces.ActeDecesService
import org.megamind.rdc_etat_civil.deces.dto.ActeDecesRequest
import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.PersonneRepository
import org.megamind.rdc_etat_civil.personne.PersonneService
import org.megamind.rdc_etat_civil.personne.StatutPersonne
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import java.time.LocalDate
import java.util.*

class ActeDecesServiceTest {

    @Mock
    private lateinit var acteDecesRepository: ActeDecesRepository

    @Mock
    private lateinit var personneRepository: PersonneRepository

    @Mock
    private lateinit var communeRepository: CommuneRepository

    @Mock
    private lateinit var entiteRepository: EntiteRepository

    @Mock
    private lateinit var provinceRepository: ProvinceRepository

    @Mock
    private lateinit var personneService: PersonneService

    private lateinit var acteDecesService: ActeDecesService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        acteDecesService = ActeDecesService(
            acteDecesRepository,
            personneRepository,
            communeRepository,
            entiteRepository,
            provinceRepository,
            personneService
        )
    }

    @Test
    fun `creerActeDeces devrait mettre à jour le statut de la personne en DECEDE`() {
        // Given
        val defuntId = 1L
        val communeId = 1L
        val request = ActeDecesRequest(
            numeroActe = "DEC-2024-001",
            defuntId = defuntId,
            communeId = communeId,
            dateDeces = LocalDate.now().minusDays(1),
            lieuDeces = "Hôpital",
            officier = "Dr. Test"
        )

        val defunt = Personne(
            id = defuntId,
            nom = "TEST",
            postnom = "PERSONNE",
            prenom = "Jean",
            sexe = org.megamind.rdc_etat_civil.personne.Sexe.MASCULIN,
            statut = StatutPersonne.VIVANT
        )

        val commune = Commune(
            id = communeId,
            designation = "Commune Test",
            entite = Entite(
                id = 1L,
                designation = "Entité Test",
                province = Province(id = 1L, designation = "Province Test")
            )
        )

        val acteCree = ActeDeces(
            id = 1L,
            numeroActe = request.numeroActe,
            defunt = defunt,
            commune = commune,
            dateDeces = request.dateDeces,
            lieuDeces = request.lieuDeces,
            officier = request.officier
        )

        // Mock des repositories
        `when`(acteDecesRepository.existsByNumeroActe(request.numeroActe)).thenReturn(false)
        `when`(personneRepository.findById(defuntId)).thenReturn(Optional.of(defunt))
        `when`(acteDecesRepository.existsByDefunt(defunt)).thenReturn(false)
        `when`(communeRepository.findById(communeId)).thenReturn(Optional.of(commune))
        `when`(acteDecesRepository.save(any(ActeDeces::class.java))).thenReturn(acteCree)

        // When
        val result = acteDecesService.creerActeDeces(request)

        // Then
        assertNotNull(result)
        assertEquals(request.numeroActe, result.numeroActe)
        
        // Vérifier que le statut de la personne a été mis à jour
        verify(personneService, times(1)).changerStatut(defuntId, StatutPersonne.DECEDE)
    }

    @Test
    fun `supprimerActeDeces devrait restaurer le statut de la personne en VIVANT`() {
        // Given
        val acteId = 1L
        val defuntId = 1L
        
        val defunt = Personne(
            id = defuntId,
            nom = "TEST",
            postnom = "PERSONNE",
            prenom = "Jean",
            sexe = org.megamind.rdc_etat_civil.personne.Sexe.MASCULIN,
            statut = StatutPersonne.DECEDE
        )

        val commune = Commune(
            id = 1L,
            designation = "Commune Test",
            entite = Entite(
                id = 1L,
                designation = "Entité Test",
                province = Province(id = 1L, designation = "Province Test")
            )
        )

        val acte = ActeDeces(
            id = acteId,
            numeroActe = "DEC-2024-001",
            defunt = defunt,
            commune = commune,
            dateDeces = LocalDate.now().minusDays(1),
            lieuDeces = "Hôpital",
            officier = "Dr. Test"
        )

        `when`(acteDecesRepository.findById(acteId)).thenReturn(Optional.of(acte))

        // When
        acteDecesService.supprimerActeDeces(acteId)

        // Then
        verify(acteDecesRepository, times(1)).deleteById(acteId)
        verify(personneService, times(1)).changerStatut(defuntId, StatutPersonne.VIVANT)
    }

    @Test
    fun `creerActeDeces devrait continuer même si la mise à jour du statut échoue`() {
        // Given
        val defuntId = 1L
        val communeId = 1L
        val request = ActeDecesRequest(
            numeroActe = "DEC-2024-001",
            defuntId = defuntId,
            communeId = communeId,
            dateDeces = LocalDate.now().minusDays(1),
            lieuDeces = "Hôpital",
            officier = "Dr. Test"
        )

        val defunt = Personne(
            id = defuntId,
            nom = "TEST",
            postnom = "PERSONNE",
            prenom = "Jean",
            sexe = org.megamind.rdc_etat_civil.personne.Sexe.MASCULIN,
            statut = StatutPersonne.VIVANT
        )

        val commune = Commune(
            id = communeId,
            designation = "Commune Test",
            entite = Entite(
                id = 1L,
                designation = "Entité Test",
                province = Province(id = 1L, designation = "Province Test")
            )
        )

        val acteCree = ActeDeces(
            id = 1L,
            numeroActe = request.numeroActe,
            defunt = defunt,
            commune = commune,
            dateDeces = request.dateDeces,
            lieuDeces = request.lieuDeces,
            officier = request.officier
        )

        // Mock des repositories
        `when`(acteDecesRepository.existsByNumeroActe(request.numeroActe)).thenReturn(false)
        `when`(personneRepository.findById(defuntId)).thenReturn(Optional.of(defunt))
        `when`(acteDecesRepository.existsByDefunt(defunt)).thenReturn(false)
        `when`(communeRepository.findById(communeId)).thenReturn(Optional.of(commune))
        `when`(acteDecesRepository.save(any(ActeDeces::class.java))).thenReturn(acteCree)

        // Simuler une erreur lors de la mise à jour du statut
        doThrow(RuntimeException("Erreur de mise à jour")).`when`(personneService).changerStatut(defuntId, StatutPersonne.DECEDE)

        // When
        val result = acteDecesService.creerActeDeces(request)

        // Then
        assertNotNull(result)
        assertEquals(request.numeroActe, result.numeroActe)
        
        // Vérifier que la mise à jour du statut a été tentée
        verify(personneService, times(1)).changerStatut(defuntId, StatutPersonne.DECEDE)
    }
}

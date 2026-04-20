package org.megamind.rdc_etat_civil.statistique

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class StatistiqueService(
    private val statistiqueRepository: StatistiqueCustomRepository
) {

    fun obtenirStatistiques(
        dateDebut: LocalDate?,
        dateFin: LocalDate?,
        grouperPar: NiveauGroupement?,
        provinceId: Long? = null,
        entiteId: Long? = null,
        communeId: Long? = null
    ): StatistiqueResponseDto {
        
        // Période par défaut : le mois en cours (ou une année en cours pour avoir de belles évolutions ?)
        // Si le dashboard demande l'évolution, il est préférable d'afficher l'année en cours par défaut.
        // Mais nous respecterons la signature du Controller. Le client peut préciser librement `dateDebut` et `dateFin`.
        val today = LocalDate.now()
        val debut = dateDebut ?: today.withDayOfMonth(1)
        val fin = dateFin ?: YearMonth.from(today).atEndOfMonth()

        val statsGlobales = statistiqueRepository.getGlobalStats(debut, fin, provinceId, entiteId, communeId)
        
        val statsGroupes = if (grouperPar != null) {
            statistiqueRepository.getStatsGrouped(debut, fin, grouperPar, provinceId, entiteId, communeId)
        } else {
            null
        }

        val statsEvolution = statistiqueRepository.getStatsEvolution(debut, fin, provinceId, entiteId, communeId)

        return StatistiqueResponseDto(
            periodeDebut = debut,
            periodeFin = fin,
            niveauGroupement = grouperPar,
            statsGlobales = statsGlobales,
            statsParRegion = statsGroupes,
            statsEvolution = statsEvolution
        )
    }
}

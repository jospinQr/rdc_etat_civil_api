package org.megamind.rdc_etat_civil.statistique

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month

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
        val today = LocalDate.now()
        val debut = dateDebut ?: today.withDayOfYear(1)
        val fin = dateFin ?: today.withDayOfYear(today.lengthOfYear())

        val statsGlobales = statistiqueRepository.getGlobalStats(debut, fin, provinceId, entiteId, communeId)
        val statsGroupes = grouperPar?.let {
            statistiqueRepository.getStatsGrouped(debut, fin, it, provinceId, entiteId, communeId)
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

    fun obtenirDonneesDashboard(
        annee: Int?,
        provinceId: Long? = null,
        entiteId: Long? = null,
        communeId: Long? = null
    ): List<DashboardMoisDto> {
        val year = annee ?: LocalDate.now().year
        val debut = LocalDate.of(year, Month.JANUARY, 1)
        val fin = LocalDate.of(year, Month.DECEMBER, 31)
        val evolution = statistiqueRepository.getStatsEvolution(debut, fin, provinceId, entiteId, communeId)
        val index = evolution.associateBy { it.periode }

        val nomsMois = listOf("Jan", "Fev", "Mar", "Avr", "Mai", "Juin", "Juil", "Aout", "Sep", "Oct", "Nov", "Dec")

        return (1..12).map { month ->
            val key = String.format("%04d-%02d", year, month)
            val stats = index[key]?.stats ?: StatistiqueDto()
            DashboardMoisDto(
                mois = nomsMois[month - 1],
                naissances = stats.nombreTotalNaissance,
                deces = stats.nombreTotalDeces,
                mariages = stats.nombreTotalMariage
            )
        }
    }
}

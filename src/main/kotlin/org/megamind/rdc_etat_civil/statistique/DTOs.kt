package org.megamind.rdc_etat_civil.statistique

import java.time.LocalDate

data class StatistiqueDto(
    val nombreTotalNaissance: Long = 0,
    val naissancesGarcons: Long = 0,
    val naissancesFilles: Long = 0,
    
    val nombreTotalMariage: Long = 0,
    val nombreTotalDeces: Long = 0,
    val decesHommes: Long = 0,
    val decesFemmes: Long = 0
)

data class StatistiqueRegionDto(
    val nomRegion: String,
    val stats: StatistiqueDto
)

data class StatistiqueEvolutionDto(
    val periode: String, // Format: YYYY-MM
    val stats: StatistiqueDto
)

enum class NiveauGroupement {
    PROVINCE, ENTITE, COMMUNE
}

data class StatistiqueResponseDto(
    val periodeDebut: LocalDate,
    val periodeFin: LocalDate,
    val niveauGroupement: NiveauGroupement?,
    val statsGlobales: StatistiqueDto,
    val statsParRegion: List<StatistiqueRegionDto>? = null,
    val statsEvolution: List<StatistiqueEvolutionDto>? = null
)

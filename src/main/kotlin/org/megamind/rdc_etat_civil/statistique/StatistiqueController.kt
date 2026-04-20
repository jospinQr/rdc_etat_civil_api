package org.megamind.rdc_etat_civil.statistique

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/statistiques")
class StatistiqueController(
    private val statistiqueService: StatistiqueService
) {

    @GetMapping
    fun getStatistiques(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateDebut: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFin: LocalDate?,
        @RequestParam(required = false) grouperPar: NiveauGroupement?,
        @RequestParam(required = false) provinceId: Long?,
        @RequestParam(required = false) entiteId: Long?,
        @RequestParam(required = false) communeId: Long?
    ): ResponseEntity<StatistiqueResponseDto> {
        val response = statistiqueService.obtenirStatistiques(dateDebut, dateFin, grouperPar, provinceId, entiteId, communeId)
        return ResponseEntity.ok(response)
    }
}

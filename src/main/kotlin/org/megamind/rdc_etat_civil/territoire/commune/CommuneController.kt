package org.megamind.rdc_etat_civil.territoire.commune


import jakarta.persistence.EntityNotFoundException
import org.megamind.rdc_etat_civil.territoire.commune.dto.CommunAvecIdEntiteDto

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("territoire/commune")
class CommuneController(private val service: CommuneService) {

    @GetMapping("/all")
    fun getAllCommune(): ResponseEntity<List<CommunAvecIdEntiteDto>> {
        val communes = service.findAll()

        // Si vide, on peut décider de lancer une exception personnalisée
        if (communes.isEmpty()) {
            throw EntityNotFoundException("Aucune commune trouvée")
        }

        return ResponseEntity.ok(communes)
    }

    @GetMapping("/entite/{entiteId}")
    fun getCommuneByEntite(@PathVariable entiteId: Long): ResponseEntity<List<Commune>> {
        val communes = service.findCommunesByEntite(entiteId)

        if (communes.isEmpty()) {
            throw EntityNotFoundException("Aucune commune trouvée pour l'entité $entiteId")
        }

        return ResponseEntity.ok(communes)
    }
}

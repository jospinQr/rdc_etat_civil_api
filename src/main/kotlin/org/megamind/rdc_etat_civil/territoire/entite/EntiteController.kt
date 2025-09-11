package org.megamind.rdc_etat_civil.territoire.entite

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("territoire/entite")
class EntiteController(private val service: EntiteService) {


    @GetMapping("/all")
    fun getAllEntite(): ResponseEntity<List<Entite>> {

        val entite = service.findAll()

        if (entite.isEmpty()) {

            throw EntityNotFoundException("Aucune ville ou territoire trouvée")
        }
        return ResponseEntity.ok().body(entite)


    }

    @GetMapping("/province/{provinceId}")
    fun getByProvince(@PathVariable provinceId: Long): ResponseEntity<List<Entite>> {

        val entites = service.findByProvince(provinceId)

        if (entites.isEmpty()) {
            throw EntityNotFoundException("Aucune ville ou territoire trouvée")
        }

        return ResponseEntity.ok(entites)

    }


}
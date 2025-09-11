package org.megamind.rdc_etat_civil.territoire.quarier

import org.megamind.rdc_etat_civil.territoire.quarier.dto.QuartierAvecCommunId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("territoire/quartier")
class QuartierController(private val service: QuartierService) {


    @GetMapping("/all")

    fun getAll(): ResponseEntity<List<QuartierAvecCommunId>> {

        return try {
            ResponseEntity(service.findAll(), HttpStatus.OK)
        } catch (ex: Exception) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/commune/{communeId}")
    fun getQuartierByCommune(@PathVariable communeId: Long): ResponseEntity<List<Quartier>> {

        return try {

            ResponseEntity(service.findByEntity(communeId), HttpStatus.OK)
        } catch (ex: Exception) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        }

    }


}
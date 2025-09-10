package org.megamind.rdc_etat_civil.territoire.entite

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/entite")
class EntiteController(private val service: EntiteService) {


    @GetMapping("/all")
    fun getAllEntite(): ResponseEntity<List<Entite>> {

        return try {
            ResponseEntity(service.findAll(), HttpStatus.OK)

        } catch (ex: Exception) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        }


    }

    @GetMapping("/province/{provinceId}")
    fun getByProvince(@PathVariable provinceId: Long): ResponseEntity<List<Entite>> {


        return try {
            ResponseEntity(service.findByProvince(provinceId), HttpStatus.OK)
        } catch (ex: EntityNotFoundException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (ex: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }


}
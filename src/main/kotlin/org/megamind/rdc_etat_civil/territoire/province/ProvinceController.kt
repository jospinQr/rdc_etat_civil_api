package org.megamind.rdc_etat_civil.territoire.province

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/province")
class ProvinceController(private val service: ProvinceService) {

    @GetMapping("/all")

    fun findAll(): ResponseEntity<List<Province>> {

        return try {
            val provinces = service.getAllProvince()
            ResponseEntity(provinces, HttpStatus.OK)
        } catch (e: EntityNotFoundException) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {

            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }


    }

}
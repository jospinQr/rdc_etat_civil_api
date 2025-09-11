package org.megamind.rdc_etat_civil.territoire.province

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("territoire/province")
class ProvinceController(private val service: ProvinceService) {

    @GetMapping("/all")

    fun findAll(): ResponseEntity<List<Province>> {

        val provinces = service.findAllProvinces()

        if (provinces.isEmpty()) {
            throw EntityNotFoundException("Aucune proovince trouvée")
        }

        return ResponseEntity.ok().body(provinces)


    }

}
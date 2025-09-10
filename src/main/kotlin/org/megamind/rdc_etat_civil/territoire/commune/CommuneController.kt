package org.megamind.rdc_etat_civil.territoire.commune



import org.megamind.rdc_etat_civil.territoire.commune.dto.CommunAvecIdEntiteDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/commune")
class CommuneController(private val service: CommuneService) {


    @GetMapping("/all")
    fun getAllCommune(): ResponseEntity<List<CommunAvecIdEntiteDto>> {


        return try {
            val communes = service.findAll()
            ResponseEntity(communes, HttpStatus.OK)
        } catch (e: Exception) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/entite/{entiteId}")
    fun getCommuneByEntite(@PathVariable entiteId: Long): ResponseEntity<List<Commune>> {


        return try {
            val communes = service.findCommunesByEntite(entiteId)
            ResponseEntity(communes, HttpStatus.OK)
        } catch (e: Exception) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}
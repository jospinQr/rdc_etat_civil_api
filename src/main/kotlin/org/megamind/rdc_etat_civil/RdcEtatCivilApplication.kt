package org.megamind.rdc_etat_civil

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
class RdcEtatCivilApplication

fun main(args: Array<String>) {
    runApplication<RdcEtatCivilApplication>(*args)
}


@RestController

class GrettingController {


    @GetMapping("/hello")
    fun sayHello(): ResponseEntity<Map<String, Any>> {

        return ResponseEntity.ok().body(
            mapOf(
                "Nom" to "Jospin",
                "Age" to 28,
                "Poid" to 62.4
            )

        )

    }


}

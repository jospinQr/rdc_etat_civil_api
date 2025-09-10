package org.megamind.rdc_etat_civil.territoire.commune

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommuneRepository : JpaRepository<Commune, Long> {


    fun findByEntiteId(idEntite: Long): Optional<List<Commune>>
}
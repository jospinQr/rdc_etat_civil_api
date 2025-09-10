package org.megamind.rdc_etat_civil.territoire.quarier

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface QuartierRepository : JpaRepository<Quartier, Long> {


    fun findByCommuneId(idCommune: Long): Optional<List<Quartier>>
}
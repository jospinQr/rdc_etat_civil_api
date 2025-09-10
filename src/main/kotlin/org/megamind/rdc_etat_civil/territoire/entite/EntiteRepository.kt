package org.megamind.rdc_etat_civil.territoire.entite

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EntiteRepository : JpaRepository<Entite, Long> {

    fun findByProvinceId(provinceId : Long): Optional<List<Entite>>

}
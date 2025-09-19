package org.megamind.rdc_etat_civil.territoire.entite

import org.megamind.rdc_etat_civil.territoire.province.Province
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EntiteRepository : JpaRepository<Entite, Long> {

    fun findByProvinceId(provinceId : Long): Optional<List<Entite>>
    
    /**
     * Trouve toutes les entités d'une province donnée
     */
    fun findByProvince(province: Province): List<Entite>

}
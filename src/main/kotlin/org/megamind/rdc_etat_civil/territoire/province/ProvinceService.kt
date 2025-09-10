package org.megamind.rdc_etat_civil.territoire.province

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service


@Service
class ProvinceService(private val repository: ProvinceRepository) {

    fun getAllProvince(): List<Province> {

        return try {
            repository.findAll()
        } catch (e: EntityNotFoundException) {

            throw EntityNotFoundException(e.message)
        }


    }


}
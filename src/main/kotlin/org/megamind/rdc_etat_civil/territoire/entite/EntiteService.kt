package org.megamind.rdc_etat_civil.territoire.entite

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service


@Service
class EntiteService(private val repository: EntiteRepository) {


    fun findAll(): List<Entite> {

        return try {
            repository.findAll()
        } catch (e: EntityNotFoundException) {

            throw EntityNotFoundException(e.message)
        }

    }


    fun findByProvince(provinceId: Long): List<Entite> = repository.findByProvinceId(provinceId).orElseThrow {

        throw EntityNotFoundException("Entite non trouve")
    }


}
package org.megamind.rdc_etat_civil.territoire.quarier

import org.megamind.rdc_etat_civil.territoire.quarier.dto.QuartierAvecCommunId
import org.megamind.rdc_etat_civil.territoire.quarier.dto.toQuartierAvecCommunId
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class QuartierService(private val repository: QuartierRepository) {


    fun findByEntity(entiteId: Long): List<Quartier> =
        repository.findByCommuneId(idCommune = entiteId).orElseThrow {
            throw EntityNotFoundException("Quartier non trouvé")
        }


    fun findAll(): List<QuartierAvecCommunId> = repository.findAll().map { it.toQuartierAvecCommunId() }

}
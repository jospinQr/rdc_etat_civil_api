package org.megamind.rdc_etat_civil.territoire.commune

import jakarta.persistence.EntityNotFoundException
import org.megamind.rdc_etat_civil.territoire.commune.dto.CommunAvecIdEntiteDto
import org.megamind.rdc_etat_civil.territoire.commune.dto.toCommunAvecIdEntiteDto
import org.springframework.stereotype.Service


@Service
class CommuneService(private val repository: CommuneRepository) {

    fun findAll(): List<CommunAvecIdEntiteDto> = repository.findAll().map { it.toCommunAvecIdEntiteDto() }

    fun findCommunesByEntite(idEntity: Long): List<Commune> = repository.findByEntiteId(idEntity).orElseThrow {
        throw EntityNotFoundException("Commune non trouvé")
    }


}
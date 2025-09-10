package org.megamind.rdc_etat_civil.utlisateur


import org.megamind.rdc_etat_civil.utlisat.Utilisateur
import org.springframework.data.jpa.repository.JpaRepository


interface UtilisateurRepository : JpaRepository<Utilisateur, Long> {

    fun findByUsername(userName: String): Utilisateur?

    fun existsByUsername(userName: String): Boolean

}
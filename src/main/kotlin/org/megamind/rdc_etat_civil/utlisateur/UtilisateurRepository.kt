package org.megamind.rdc_etat_civil.utlisateur


import org.megamind.rdc_etat_civil.utlisat.User
import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}
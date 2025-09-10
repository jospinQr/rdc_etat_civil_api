package org.megamind.rdc_etat_civil.utlisateur

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UtilisateurRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("Utilisateur non trouvé")


        return User
            .withUsername(user.username)
            .password(user.password)
            .roles(user.role.name)
            .build()
    }
}
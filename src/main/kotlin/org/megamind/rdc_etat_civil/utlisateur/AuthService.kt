package org.megamind.rdc_etat_civil.utlisateur

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.megamind.rdc_etat_civil.jwt.JwtService
import org.megamind.rdc_etat_civil.territoire.commune.CommuneRepository
import org.megamind.rdc_etat_civil.territoire.entite.EntiteRepository
import org.megamind.rdc_etat_civil.territoire.province.ProvinceRepository
import org.megamind.rdc_etat_civil.utlisat.Role
import org.megamind.rdc_etat_civil.utlisat.User
import org.megamind.rdc_etat_civil.utlisateur.dto.AuthResponse
import org.megamind.rdc_etat_civil.utlisateur.dto.LoginRequest
import org.megamind.rdc_etat_civil.utlisateur.dto.RegisterRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val provinceRepository: ProvinceRepository,
    private val entiteRepository: EntiteRepository,
    private val communeRepository: CommuneRepository
) {


    fun login(@Valid request: LoginRequest): AuthResponse {

        val user =
            userRepository.findByUsername(request.username) ?: throw IllegalArgumentException("Ce nom n'exixte pas")


        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Mot de passe incorrect")
        }

        val token =
            jwtService.generateToken(
                user.id,
                user.username,
                role = user.role.name,
                provinceId = user.province?.id,
                entiteId = user.entite?.id,
                communeId = user.commune?.id
            )

        return AuthResponse(token)
    }


    fun register(@Valid request: RegisterRequest): AuthResponse {

        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("Ce nom d'utilisateur existe déjà")
        }

        when (request.role) {
            Role.ADMIN -> Unit
            Role.CD -> if (request.provinceId == null) throw IllegalArgumentException("La province est obligatoire pour un CD")
            Role.CB -> if (request.entiteId == null) throw IllegalArgumentException("L'entité est obligatoire pour un CB")
            Role.OEC -> if (request.communeId == null) throw IllegalArgumentException("La commune est obligatoire pour un OEC")
        }

        val province = request.provinceId?.let { id ->
            provinceRepository.findById(id).orElseThrow {
                EntityNotFoundException("Province introuvable (id=$id)")
            }
        }

        val entite = request.entiteId?.let { id ->
            entiteRepository.findById(id).orElseThrow {
                EntityNotFoundException("Entité introuvable (id=$id)")
            }
        }

        val commune = request.communeId?.let { id ->
            communeRepository.findById(id).orElseThrow {
                EntityNotFoundException("Commune introuvable (id=$id)")
            }
        }
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = request.role,
            province = province,
            entite = entite,
            commune = commune
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(
            savedUser.id,
            savedUser.username,
            role = savedUser.role.name,
            provinceId = savedUser.province?.id,
            entiteId = savedUser.entite?.id,
            communeId = savedUser.commune?.id
        )

        return AuthResponse(token)
    }



    fun changePassword(userId: Long, currentPassword: String, newPassword: String?, newUsername: String?): AuthResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("Mot de passe actuel incorrect")
        }

        // Vérifier qu'au moins une modification est demandée
        if (newPassword == null && newUsername == null) {
            throw IllegalArgumentException("Veuillez fournir un nouveau mot de passe ou un nouveau nom d'utilisateur")
        }

        // Vérifier si le nouveau nom d'utilisateur est déjà pris
        if (newUsername != null && newUsername != user.username) {
            if (userRepository.findByUsername(newUsername) != null) {
                throw IllegalArgumentException("Ce nom d'utilisateur existe déjà")
            }
        }

        // Mettre à jour les champs
        val updatedUser = user.copy(
            password = if (newPassword != null) passwordEncoder.encode(newPassword) else user.password,
            username = newUsername ?: user.username
        )
        userRepository.save(updatedUser)

        // Générer un nouveau token
        val token = jwtService.generateToken(
            updatedUser.id,
            updatedUser.username,
            role = updatedUser.role.name,
            provinceId = updatedUser.province?.id,
            entiteId = updatedUser.entite?.id,
            communeId = updatedUser.commune?.id
        )
        return AuthResponse(token)
    }
}
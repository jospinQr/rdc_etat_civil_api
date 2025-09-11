package org.megamind.rdc_etat_civil.utlisateur


import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.jwt.JwtService
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.utlisat.Role
import org.megamind.rdc_etat_civil.utlisat.Utilisateur
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UtilisateurRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        if (userRepository.existsByUsername(request.username)) {
            throw BadRequestException("Username déjà utilisé")
        }

        val user = Utilisateur(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = request.role,
            province = request.province,
            entite = request.entite,

            )

        userRepository.save(user)

        val token = jwtService.generateToken(user.username)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByUsername(request.username)
            ?: throw BadRequestException("Nom d'utilisateur ou mot de passe incorrect")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadRequestException("Nom d'utilisateur ou mot de passe incorrect")
        }


        // Vérification du rôle
        if (user.role != request.role) {
            throw BadRequestException("Role incorrecte")
        }

        // Si l'utilisateur n'est pas Admin, on vérifie province et entité
        when (user.role) {
            Role.ADMIN -> { /* pas de restriction */
            }

            Role.CD -> {
                if (user.province != request.province) {
                    throw BadRequestException("Province incorrecte")
                }
            }

            Role.CB -> {
                if (user.province != request.province) {
                    throw BadRequestException("Province incorrecte")
                }
                if (user.entite != request.entite) {
                    throw BadRequestException("Entité incorrecte")
                }
            }

            Role.OEC -> {

                if (user.province != request.province) {
                    throw BadRequestException("Province incorrecte")
                }
                if (user.entite != request.entite) {
                    throw BadRequestException("Ville ou territoire incorrecte")
                }

                if (user.commune != request.commune) {
                    throw BadRequestException("Commune ou chefferie incorrecte")
                }

            }
        }

        val token = jwtService.generateToken(user.username)
        return ResponseEntity.ok(AuthResponse(token))
    }

}

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: Role,
    val province: Province? = null,
    val entite: Entite? = null
)

data class LoginRequest(
    val username: String,
    val password: String,
    val role: Role,
    val province: Province? = null,
    val entite: Entite? = null,
    val commune: Commune? = null,
)

data class AuthResponse(val token: String)

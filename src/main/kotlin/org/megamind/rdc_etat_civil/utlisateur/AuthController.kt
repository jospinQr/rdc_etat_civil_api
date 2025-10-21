package org.megamind.rdc_etat_civil.utlisateur


import org.apache.coyote.BadRequestException
import org.megamind.rdc_etat_civil.jwt.JwtService
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.utlisat.Role
import org.megamind.rdc_etat_civil.utlisat.Utilisateur
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
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
            commune = request.commune

        )

        when (user.role) {
            Role.ADMIN -> { /* pas de restriction */
            }

            Role.CD -> {
                if (request.province == null) {
                    throw BadRequestException("Province incorrecte")
                }
            }

            Role.CB -> {
                if (request.province == null) {
                    throw BadRequestException("Province incorrecte")
                }
                if (user.entite == null) {
                    throw BadRequestException("Entité incorrecte")
                }
            }

            Role.OEC -> {

                if (request.province == null) {
                    throw BadRequestException("Province incorrecte")
                }
                if (request.entite == null) {
                    throw BadRequestException("Ville ou territoire incorrecte")
                }

                if (user.commune == null) {
                    throw BadRequestException("Commune ou chefferie incorrecte")
                }

            }
        }


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

        val token = jwtService.generateToken(user.username)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<UserInfoResponse> {
        if (authorizationHeader.isNullOrBlank() || !authorizationHeader.startsWith("Bearer ")) {
            throw BadRequestException("Token manquant ou invalide")
        }

        val token = authorizationHeader.removePrefix("Bearer ").trim()
        val username = jwtService.extractUsername(token)
            ?: throw BadRequestException("Token invalide")

        val user = userRepository.findByUsername(username)
            ?: throw BadRequestException("Utilisateur introuvable")

        val body = UserInfoResponse(
            username = user.username,
            role = user.role,
            provinceId = user.province?.id,
            entiteId = user.entite?.id,
            communeId = user.commune?.id,
        )

        return ResponseEntity.ok(body)
    }

}

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: Role,
    val province: Province? = null,
    val entite: Entite? = null,
    val commune: Commune? = null
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class AuthResponse(val token: String)

data class UserInfoResponse(
    val username: String,
    val role: Role,
    val provinceId: Long?,
    val entiteId: Long?,
    val communeId: Long?,
)

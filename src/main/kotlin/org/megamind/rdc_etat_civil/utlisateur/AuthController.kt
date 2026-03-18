package org.megamind.rdc_etat_civil.utlisateur


import org.megamind.rdc_etat_civil.jwt.JwtService
import org.megamind.rdc_etat_civil.utlisateur.dto.AuthResponse
import org.megamind.rdc_etat_civil.utlisateur.dto.ChangePasswordRequest
import org.megamind.rdc_etat_civil.utlisateur.dto.LoginRequest
import org.megamind.rdc_etat_civil.utlisateur.dto.RegisterRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/auth")
class AuthController(
    private val service: AuthService,
    private val jwtService: JwtService,

    ) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {

        val authResponse = service.login(loginRequest)
        return ResponseEntity.ok(authResponse)

    }

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {

        val authResponse = service.register(registerRequest)
        return ResponseEntity.ok(authResponse)

    }

    @PutMapping("/change-password")
    fun changePassword(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<AuthResponse> {
        val token = authHeader.removePrefix("Bearer ").trim()
        val userId = jwtService.extractUserId(token)
            ?: throw IllegalArgumentException("Token invalide")

        val authResponse = service.changePassword(userId, request.currentPassword, request.newPassword, request.newUsername)
        return ResponseEntity.ok(authResponse)
    }

}

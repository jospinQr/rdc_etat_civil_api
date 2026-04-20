package org.megamind.rdc_etat_civil.jwt


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


@Service
class JwtService(
    @Value("\${spring.jwt.secret}")
    private val secret: String,

    @Value("\${spring.jwt.expiration}")
    private val expiration: Long

) {


    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }


    fun generateToken(
        userId: Long,
        username: String,
        role: String,
        provinceId: Long?,
        entiteId: Long?,
        communeId: Long?,
    ): String {
        val now = Date()

        val claims = mutableMapOf<String, Any>(
            "role" to role, "name" to username
        )
        if (provinceId != null) {
            claims["provinceId"] = provinceId
        }
        if (entiteId != null) {
            claims["entiteId"] = entiteId
        }
        if (communeId != null) {
            claims["communeId"] = communeId
        }

        return Jwts.builder().claims(claims).subject(userId.toString()).issuedAt(now)
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key, SignatureAlgorithm.HS256).compact()
    }

    // 🔹 Extrait l'ID utilisateur
    fun extractUserId(token: String): Long? = extractAllClaims(token)?.subject?.toLongOrNull()

    // 🔹 Extrait le nom d'utilisateur
    fun extractUsername(token: String): String? = extractAllClaims(token)?.get("name", String::class.java)

    // 🔹 Extrait le rôle utilisateur
    fun extractRole(token: String): String? = extractAllClaims(token)?.get("role", String::class.java)


    // 🔹 Méthode interne pour parser les claims
    private fun extractAllClaims(token: String): Claims? {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (e: Exception) {
            null
        }
    }

    // 🔹 Vérifie si le token est valide pour un utilisateur donné
    fun isTokenValid(token: String, username: String): Boolean {
        val extractedUsername = extractUsername(token)
        return extractedUsername == username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractAllClaims(token)?.expiration
        return expiration?.before(Date()) ?: true
    }
}

// 🔹 Data class pour encapsuler les informations du user
data class UserInfo(
    val username: String?,
    val role: String?,
    val provinceId: Long?,
    val entiteId: Long?,
    val communeId: Long?,
)
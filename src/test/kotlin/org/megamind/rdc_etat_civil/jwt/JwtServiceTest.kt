package org.megamind.rdc_etat_civil.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class JwtServiceTest {

    private val jwt = JwtService()

    @Test
    fun `generateToken - extrait userId username role et valide`() {
        val token = jwt.generateToken(
            userId = 42L,
            username = "admin",
            role = "ADMIN",
            provinceId = 1L,
            entiteId = 2L,
            communeId = 3L
        )

        assertNotNull(token)
        assertEquals(42L, jwt.extractUserId(token))
        assertEquals("admin", jwt.extractUsername(token))
        assertEquals("ADMIN", jwt.extractRole(token))
        assertTrue(jwt.isTokenValid(token, "admin"))
        assertFalse(jwt.isTokenValid(token, "someone-else"))
    }

    @Test
    fun `extract - token invalide retourne null et invalid`() {
        val token = "not-a-jwt"
        assertNull(jwt.extractUserId(token))
        assertNull(jwt.extractUsername(token))
        assertNull(jwt.extractRole(token))
        assertFalse(jwt.isTokenValid(token, "admin"))
    }
}


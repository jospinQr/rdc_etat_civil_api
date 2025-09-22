package org.megamind.rdc_etat_civil.common

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Utilitaires pour les tests
 */
object TestUtils {

    /**
     * Exécute une requête GET et vérifie le statut
     */
    fun MockMvc.performGet(url: String, expectedStatus: Int = 200) {
        perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().`is`(expectedStatus))
    }

    /**
     * Exécute une requête POST avec un body JSON
     */
    fun MockMvc.performPost(url: String, body: Any, expectedStatus: Int = 201) {
        val jsonBody = ObjectMapper().writeValueAsString(body)
        perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            .andExpect(status().`is`(expectedStatus))
    }

    /**
     * Exécute une requête PUT avec un body JSON
     */
    fun MockMvc.performPut(url: String, body: Any, expectedStatus: Int = 200) {
        val jsonBody = ObjectMapper().writeValueAsString(body)
        perform(
            MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            .andExpect(status().`is`(expectedStatus))
    }

    /**
     * Exécute une requête DELETE
     */
    fun MockMvc.performDelete(url: String, expectedStatus: Int = 204) {
        perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().`is`(expectedStatus))
    }

    /**
     * Vérifie qu'une réponse contient un champ spécifique
     * Note: Cette méthode doit être utilisée dans une chaîne MockMvc
     */
    fun expectJsonPath(path: String, value: Any) = MockMvcResultMatchers.jsonPath(path).value(value)

    /**
     * Convertit un objet en JSON
     */
    fun toJson(obj: Any): String {
        return ObjectMapper().writeValueAsString(obj)
    }

    /**
     * Convertit un JSON en objet
     */
    inline fun <reified T> fromJson(json: String): T {
        return ObjectMapper().readValue(json, T::class.java)
    }
}

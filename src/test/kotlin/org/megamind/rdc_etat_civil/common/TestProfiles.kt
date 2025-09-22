package org.megamind.rdc_etat_civil.common

/**
 * Constantes pour les profils de test
 */
object TestProfiles {
    const val UNIT_TEST = "unit-test"
    const val INTEGRATION_TEST = "integration-test"
    const val API_TEST = "api-test"
    const val TEST = "test"
}

/**
 * Annotations pour faciliter l'utilisation des profils
 */
annotation class UnitTest
annotation class IntegrationTest
annotation class ApiTest


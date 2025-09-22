package org.megamind.rdc_etat_civil.common

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Profile

/**
 * Configuration de base de données de test simplifiée
 * Utilise H2 en mémoire pour tous les tests via application-test.yaml
 */
@TestConfiguration
@Profile("test")
class TestDatabaseConfiguration {
    // Configuration simplifiée - H2 sera utilisé automatiquement
    // via application-test.yaml
}
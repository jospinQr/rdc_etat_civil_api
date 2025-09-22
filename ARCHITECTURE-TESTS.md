# Architecture des Tests - RDC État Civil

## Vue d'ensemble

Ce document décrit l'architecture de tests mise en place pour le projet RDC État Civil. L'architecture suit la pyramide de tests avec trois niveaux principaux :

1. **Tests Unitaires** - Tests rapides et isolés des composants individuels
2. **Tests d'Intégrité** - Tests des interactions entre composants et avec la base de données
3. **Tests d'API** - Tests end-to-end des endpoints REST

## Structure des Dossiers

```
src/test/kotlin/org/megamind/rdc_etat_civil/
├── unit/                          # Tests unitaires
│   ├── PersonneServiceTest.kt     # Tests du service Personne
│   ├── PersonneControllerTest.kt  # Tests du controller Personne
│   └── ActeDecesServiceTest.kt    # Tests du service ActeDeces
├── integration/                   # Tests d'intégrité
│   ├── PersonneEntityIntegrationTest.kt        # Tests JPA Personne
│   ├── PersonneServiceIntegrationTest.kt       # Tests service avec DB
│   └── ActeDecesEntityIntegrationTest.kt       # Tests JPA ActeDeces
├── api/                          # Tests d'API endpoints
│   ├── PersonneApiTest.kt        # Tests API Personne
│   └── ActeDecesApiTest.kt       # Tests API ActeDeces
└── common/                       # Utilitaires et configuration
    ├── TestConfiguration.kt      # Configuration de test
    ├── TestDatabaseConfiguration.kt  # Configuration DB de test
    ├── ApiTestConfiguration.kt   # Configuration sécurité API
    ├── TestUtils.kt             # Utilitaires de test
    └── builders/                # Builders pour créer des objets de test
        ├── PersonneTestBuilder.kt
        └── ActeDecesTestBuilder.kt
```

## Types de Tests

### 1. Tests Unitaires (`unit/`)

**Objectif** : Tester les composants individuels de manière isolée.

**Technologies** :
- JUnit 5
- MockK pour le mocking
- MockMvc pour les tests de controllers

**Caractéristiques** :
- Tests rapides (< 1ms par test)
- Aucune dépendance externe
- Utilisation de mocks pour les dépendances
- Couverture complète des cas de succès et d'erreur

**Exemples** :
- `PersonneServiceTest` : Teste la logique métier du service Personne
- `PersonneControllerTest` : Teste les endpoints REST du controller

### 2. Tests d'Intégrité (`integration/`)

**Objectif** : Tester les interactions entre composants et la persistance.

**Technologies** :
- `@DataJpaTest` pour les tests JPA
- `@SpringBootTest` pour les tests de services
- H2 en mémoire pour les tests
- TestEntityManager pour la gestion des entités

**Caractéristiques** :
- Tests de persistance avec base de données
- Tests des requêtes JPA
- Tests des transactions
- Validation des contraintes de base de données

**Exemples** :
- `PersonneEntityIntegrationTest` : Teste la persistance JPA
- `PersonneServiceIntegrationTest` : Teste le service avec base de données

### 3. Tests d'API (`api/`)

**Objectif** : Tester les endpoints REST de bout en bout.

**Technologies** :
- `@SpringBootTest` avec `TestRestTemplate`
- Configuration de sécurité simplifiée pour les tests
- Tests des réponses HTTP complètes

**Caractéristiques** :
- Tests end-to-end des API REST
- Validation des codes de statut HTTP
- Tests de sérialisation/désérialisation JSON
- Tests de validation des données

**Exemples** :
- `PersonneApiTest` : Teste tous les endpoints de l'API Personne
- `ActeDecesApiTest` : Teste tous les endpoints de l'API ActeDeces

## Configuration et Utilitaires

### Configuration de Test

- **`TestConfiguration.kt`** : Configuration générale des beans de test
- **`TestDatabaseConfiguration.kt`** : Configuration de la base de données de test avec Testcontainers
- **`ApiTestConfiguration.kt`** : Configuration de sécurité simplifiée pour les tests d'API

### Builders de Test

Les builders permettent de créer facilement des objets de test avec des données par défaut :

```kotlin
// Utilisation simple
val personne = PersonneTestBuilder.createDefault()

// Utilisation avec personnalisation
val personne = PersonneTestBuilder.create()
    .withNom("Dupont")
    .withPrenom("Jean")
    .build()
```

### Utilitaires

- **`TestUtils.kt`** : Fonctions utilitaires pour les tests MockMvc et la sérialisation JSON

## Profils de Test

### Profil `test`
- Utilise H2 en mémoire
- Configuration JPA simplifiée
- Logging activé pour le débogage

### Profil `integration-test`
- Utilise Testcontainers avec MySQL
- Configuration de production simulée
- Tests plus lents mais plus réalistes

## Exécution des Tests

### Tous les tests
```bash
./gradlew test
```

### Tests unitaires uniquement
```bash
./gradlew test --tests "*unit*"
```

### Tests d'intégrité uniquement
```bash
./gradlew test --tests "*integration*"
```

### Tests d'API uniquement
```bash
./gradlew test --tests "*api*"
```

### Tests avec rapport de couverture
```bash
./gradlew test jacocoTestReport
```

## Bonnes Pratiques

### 1. Nommage des Tests
- Utiliser des noms descriptifs avec `@DisplayName`
- Grouper les tests avec `@Nested`
- Utiliser la convention Given-When-Then

### 2. Isolation des Tests
- Chaque test doit être indépendant
- Nettoyer les données entre les tests
- Utiliser `@Transactional` pour les tests d'API

### 3. Données de Test
- Utiliser des builders pour créer des données cohérentes
- Éviter les données hardcodées
- Créer des données réalistes mais anonymes

### 4. Assertions
- Utiliser des assertions spécifiques (assertEquals, assertNotNull, etc.)
- Vérifier les cas d'erreur et d'exception
- Tester les conditions limites

## Métriques et Qualité

### Couverture de Code
- Objectif : > 80% de couverture
- Exclure les classes de configuration et DTOs
- Focus sur la logique métier

### Performance des Tests
- Tests unitaires : < 1ms par test
- Tests d'intégrité : < 100ms par test
- Tests d'API : < 1s par test

### Maintenance
- Réviser les tests lors des modifications de code
- Supprimer les tests obsolètes
- Mettre à jour les builders lors de l'ajout de nouveaux champs

## Outils Recommandés

### IntelliJ IDEA
- Exécution de tests individuels
- Debugging des tests
- Couverture de code intégrée

### Gradle
- Exécution parallèle des tests
- Rapports de test détaillés
- Intégration avec JaCoCo

### CI/CD
- Exécution automatique des tests
- Rapports de couverture
- Alertes en cas d'échec

Cette architecture de tests garantit une couverture complète du code, une maintenance facilitée et une confiance élevée dans la qualité du système.


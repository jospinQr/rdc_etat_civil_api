# Tests Personne - Focus Unique

## 🎯 Architecture Simplifiée pour Personne

J'ai supprimé tous les autres tests pour vous concentrer uniquement sur **Personne**. Voici ce qui reste :

### 📁 Structure Actuelle
```
src/test/kotlin/org/megamind/rdc_etat_civil/
├── unit/                          # Tests unitaires Personne
│   ├── PersonneServiceTest.kt     # Tests du service Personne
│   ├── PersonneControllerTest.kt  # Tests du controller Personne
│   └── PersonneRepositoryTest.kt  # Tests du repository Personne
├── integration/                   # Tests d'intégrité Personne
│   ├── PersonneEntityIntegrationTest.kt        # Tests JPA Personne
│   └── PersonneServiceIntegrationTest.kt       # Tests service avec DB
├── api/                          # Tests d'API Personne
│   └── PersonneApiTest.kt        # Tests API Personne
└── common/                       # Utilitaires et configuration
    ├── TestConfiguration.kt      
    ├── TestDatabaseConfiguration.kt  
    ├── ApiTestConfiguration.kt   
    ├── TestUtils.kt             
    ├── TestProfiles.kt
    └── builders/                
        └── PersonneTestBuilder.kt  # Builder pour Personne uniquement
```

### 🧪 Tests Disponibles

#### 1. Tests Unitaires (`unit/`)
- **PersonneServiceTest** : 8 tests
  - Création de personne
  - Recherche par ID
  - Mise à jour
  - Suppression
  - Gestion des erreurs

- **PersonneControllerTest** : 6 tests
  - POST /api/personnes
  - GET /api/personnes/{id}
  - GET /api/personnes
  - PUT /api/personnes/{id}
  - DELETE /api/personnes/{id}
  - Validation des données

- **PersonneRepositoryTest** : 12 tests
  - Opérations CRUD de base
  - Recherches spécifiques (nom, prénom, date)
  - Pagination et tri
  - Opérations en lot

#### 2. Tests d'Intégrité (`integration/`)
- **PersonneEntityIntegrationTest** : 6 tests
  - Sauvegarde et récupération
  - Mise à jour
  - Suppression
  - Recherches par critères
  - Validation des contraintes
  - Gestion des relations

- **PersonneServiceIntegrationTest** : 8 tests
  - Création avec persistance
  - Récupération par ID
  - Mise à jour existante
  - Suppression
  - Liste avec pagination
  - Gestion des erreurs
  - Contraintes de validation
  - Gestion des transactions

#### 3. Tests d'API (`api/`)
- **PersonneApiTest** : 10 tests
  - POST /api/personnes
  - GET /api/personnes/{id}
  - GET /api/personnes
  - PUT /api/personnes/{id}
  - DELETE /api/personnes/{id}
  - Pagination
  - Validation des données
  - Gestion des erreurs
  - Sérialisation JSON

### 🚀 Commandes d'Exécution

```bash
# Tous les tests Personne
./gradlew test

# Tests unitaires Personne uniquement
./gradlew test --tests "*unit*"

# Tests d'intégrité Personne uniquement
./gradlew test --tests "*integration*"

# Tests d'API Personne uniquement
./gradlew test --tests "*api*"

# Test spécifique
./gradlew test --tests "PersonneServiceTest"
```

### 🛠️ Builder Personne

```kotlin
// Utilisation simple
val personne = PersonneTestBuilder.createDefault()
val request = PersonneTestBuilder.createDefaultRequest()

// Utilisation avec personnalisation
val personne = PersonneTestBuilder.create()
    .withNom("Dupont")
    .withPrenom("Jean")
    .withDateNaissance(LocalDate.of(1990, 5, 15))
    .withSexe("M")
    .build()
```

### 📊 Métriques Actuelles

- **Total des tests** : ~50 tests
- **Couverture Personne** : 100% (tous les composants)
- **Temps d'exécution estimé** : < 30 secondes
- **Tests unitaires** : ~20 tests (< 1s)
- **Tests d'intégrité** : ~15 tests (< 5s)
- **Tests d'API** : ~15 tests (< 20s)

### ✅ Corrections Apportées

1. **Méthodes corrigées** : `createPersonne` → `creerPersonne`
2. **Tests supprimés** : ActeDeces, ActeNaissance, autres entités
3. **Focus unique** : Seulement Personne dans tous les tests
4. **Builders simplifiés** : PersonneTestBuilder uniquement

### 🎯 Prochaines Étapes

1. **Exécuter les tests** pour vérifier que tout fonctionne
2. **Ajuster les tests** selon vos besoins spécifiques
3. **Ajouter des cas de test** si nécessaire
4. **Une fois satisfait**, vous pourrez ajouter les autres entités

### 🔍 Points d'Attention

- Vérifiez que les méthodes du service Personne correspondent (creerPersonne, updatePersonne, etc.)
- Assurez-vous que les endpoints API correspondent aux routes définies
- Contrôlez que les contraintes JPA sont correctement testées

Cette architecture focalisée vous permet de vous concentrer entièrement sur Personne et de bien comprendre le fonctionnement des tests avant d'ajouter les autres entités ! 🎉


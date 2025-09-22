# Guide des Tests - RDC État Civil

## 🚀 Démarrage Rapide

### Exécuter tous les tests
```bash
./gradlew test
```

### Exécuter les tests par catégorie
```bash
# Tests unitaires uniquement
./gradlew test --tests "*unit*"

# Tests d'intégrité uniquement  
./gradlew test --tests "*integration*"

# Tests d'API uniquement
./gradlew test --tests "*api*"
```

### Exécuter un test spécifique
```bash
./gradlew test --tests "PersonneServiceTest"
```

## 📁 Structure des Tests

### Tests Unitaires (`unit/`)
- **Objectif** : Tester les composants individuels
- **Technologie** : MockK, MockMvc
- **Durée** : < 1ms par test
- **Dépendances** : Aucune

### Tests d'Intégrité (`integration/`)
- **Objectif** : Tester les interactions avec la base de données
- **Technologie** : @DataJpaTest, H2
- **Durée** : < 100ms par test
- **Dépendances** : Base de données en mémoire

### Tests d'API (`api/`)
- **Objectif** : Tester les endpoints REST end-to-end
- **Technologie** : @SpringBootTest, TestRestTemplate
- **Durée** : < 1s par test
- **Dépendances** : Application Spring complète

## 🛠️ Utilitaires et Builders

### Builders de Test
```kotlin
// Personne
val personne = PersonneTestBuilder.createDefault()
val personneCustom = PersonneTestBuilder.create()
    .withNom("Dupont")
    .withPrenom("Jean")
    .build()

// Acte de Décès
val acte = ActeDecesTestBuilder.createDefault()
val acteCustom = ActeDecesTestBuilder.create()
    .withNumeroActe("DEC2024001")
    .withNomDefunt("Dupont")
    .build()
```

### Utilitaires
```kotlin
// Conversion JSON
val json = toJson(objet)
val objet = fromJson<MonObjet>(json)

// Tests MockMvc
mockMvc.performGet("/api/personnes/1")
mockMvc.performPost("/api/personnes", request)
```

## 🔧 Configuration

### Profils de Test
- **`test`** : H2 en mémoire, configuration simplifiée
- **`integration-test`** : Testcontainers MySQL, configuration réaliste

### Base de Données de Test
- H2 en mémoire pour les tests unitaires et d'intégrité
- MySQL avec Testcontainers pour les tests d'API complets

## 📊 Couverture de Code

### Objectifs
- **Couverture globale** : > 80%
- **Logique métier** : > 90%
- **Controllers** : > 85%
- **Services** : > 90%

### Générer le rapport
```bash
./gradlew test jacocoTestReport
```

Le rapport sera disponible dans `build/reports/jacoco/test/html/index.html`

## 🧪 Exemples de Tests

### Test Unitaire - Service
```kotlin
@Test
fun `should create person successfully`() {
    // Given
    val request = PersonneTestBuilder.createDefaultRequest()
    every { repository.save(any()) } returns savedPersonne

    // When
    val result = service.createPersonne(request)

    // Then
    assertNotNull(result)
    assertEquals(request.nom, result.nom)
    verify { repository.save(any()) }
}
```

### Test d'Intégrité - JPA
```kotlin
@Test
fun `should save and retrieve person`() {
    // Given
    val personne = PersonneTestBuilder.createDefault()

    // When
    val saved = repository.save(personne)
    val retrieved = repository.findById(saved.id!!)

    // Then
    assertTrue(retrieved.isPresent)
    assertEquals(personne.nom, retrieved.get().nom)
}
```

### Test d'API - Endpoint
```kotlin
@Test
fun `POST should create person`() {
    // Given
    val request = PersonneTestBuilder.createDefaultRequest()

    // When
    val response = restTemplate.postForEntity("/api/personnes", request, Map::class.java)

    // Then
    assertEquals(HttpStatus.CREATED, response.statusCode)
    assertNotNull(response.body)
}
```

## 🔍 Débogage

### Activer les logs SQL
Dans `application-test.yaml` :
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Tests en mode debug
```bash
./gradlew test --debug-jvm
```

## 📝 Bonnes Pratiques

### 1. Nommage
- Utiliser des noms descriptifs
- Grouper avec `@Nested`
- Utiliser `@DisplayName` pour la lisibilité

### 2. Structure
- Given-When-Then
- Un test = un comportement
- Données de test cohérentes

### 3. Assertions
- Assertions spécifiques
- Vérifier les cas d'erreur
- Tester les conditions limites

### 4. Maintenance
- Réviser lors des modifications
- Supprimer les tests obsolètes
- Maintenir les builders à jour

## 🚨 Résolution de Problèmes

### Tests qui échouent
1. Vérifier les logs d'erreur
2. S'assurer que la base de données est propre
3. Vérifier les mocks et stubs
4. Contrôler les dépendances

### Performance lente
1. Utiliser des tests unitaires pour la logique simple
2. Éviter les tests d'intégrité pour les tests rapides
3. Paralléliser l'exécution des tests
4. Utiliser des bases de données en mémoire

### Couverture insuffisante
1. Identifier les branches non testées
2. Ajouter des tests pour les cas d'erreur
3. Tester les validations
4. Couvrir les conditions limites

## 📚 Ressources

- [Documentation JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Guide MockK](https://mockk.io/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Architecture complète](ARCHITECTURE-TESTS.md)


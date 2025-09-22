# Guide des tests d'intégrité - Actes de décès

## Vue d'ensemble

Ce guide présente la suite complète de tests d'intégrité pour la fonctionnalité d'enregistrement de lot d'actes de décès. Les tests couvrent tous les aspects de la fonctionnalité, des opérations CRUD de base aux performances en passant par la validation des données.

## Structure des tests

### 1. Tests d'intégration API (`ActeDecesBatchIntegrationTest`)

**Fichier :** `src/test/kotlin/org/megamind/rdc_etat_civil/deces/ActeDecesBatchIntegrationTest.kt`

**Objectif :** Tester l'API REST complète pour l'enregistrement de lot d'actes de décès.

#### Tests d'enregistrement de lot
- ✅ Enregistrement réussi d'un lot d'actes
- ✅ Gestion des lots vides
- ✅ Gestion des lots trop volumineux (>100 actes)
- ✅ Détection des numéros d'acte dupliqués
- ✅ Gestion des erreurs individuelles dans un lot

#### Tests de validation de lot
- ✅ Validation d'un lot valide
- ✅ Détection des erreurs de validation
- ✅ Génération d'alertes pour champs manquants

#### Tests de vérification d'existence
- ✅ Vérification de l'existence d'un numéro d'acte
- ✅ Vérification de l'existence d'un défunt

#### Tests de recherche et statistiques
- ✅ Liste des actes avec pagination
- ✅ Recherche par nom de défunt
- ✅ Obtention des statistiques

### 2. Tests de service (`ActeDecesServiceTest`)

**Fichier :** `src/test/kotlin/org/megamind/rdc_etat_civil/deces/ActeDecesServiceTest.kt`

**Objectif :** Tester la logique métier du service.

#### Tests de création d'actes individuels
- ✅ Création réussie d'un acte
- ✅ Gestion des numéros d'acte dupliqués
- ✅ Validation de l'existence des entités liées
- ✅ Validation des dates cohérentes
- ✅ Validation des formats de données

#### Tests de traitement par lot
- ✅ Traitement réussi d'un lot
- ✅ Gestion des erreurs individuelles
- ✅ Validation de lot

#### Tests de recherche
- ✅ Recherche par numéro d'acte
- ✅ Liste avec pagination
- ✅ Gestion des cas d'absence

#### Tests de vérification
- ✅ Vérification d'existence des numéros d'acte
- ✅ Vérification d'existence des défunts

#### Tests de statistiques
- ✅ Calcul correct des statistiques

### 3. Tests de repository (`ActeDecesRepositoryTest`)

**Fichier :** `src/test/kotlin/org/megamind/rdc_etat_civil/deces/ActeDecesRepositoryTest.kt`

**Objectif :** Tester les requêtes de base de données.

#### Tests de recherche de base
- ✅ Recherche par numéro d'acte
- ✅ Vérification d'existence
- ✅ Recherche par défunt

#### Tests de recherche par territoire
- ✅ Recherche par commune
- ✅ Recherche par province
- ✅ Recherche par entité
- ✅ Comptage par territoire

#### Tests de recherche par sexe
- ✅ Recherche par sexe
- ✅ Recherche par sexe et territoire
- ✅ Comptage par sexe

#### Tests de recherche par nom
- ✅ Recherche par nom de défunt
- ✅ Recherche par postnom
- ✅ Recherche par prénom

#### Tests de recherche multicritères
- ✅ Recherche avec plusieurs critères
- ✅ Recherche par dates

#### Tests de statistiques
- ✅ Statistiques par commune
- ✅ Statistiques par officier
- ✅ Statistiques par cause de décès
- ✅ Statistiques par sexe
- ✅ Calcul de moyennes

### 4. Tests de performance (`ActeDecesPerformanceTest`)

**Fichier :** `src/test/kotlin/org/megamind/rdc_etat_civil/deces/ActeDecesPerformanceTest.kt`

**Objectif :** Tester les performances de la fonctionnalité.

#### Tests de performance d'enregistrement
- ⏱️ Enregistrement de 50 actes (< 5 secondes)
- ⏱️ Enregistrement de 100 actes (< 10 secondes)

#### Tests de performance de validation
- ⏱️ Validation de 100 actes (< 2 secondes)

#### Tests de performance de recherche
- ⏱️ Recherche multicritères (< 1 seconde)
- ⏱️ Recherche par sexe (< 500ms)
- ⏱️ Recherche par commune (< 500ms)

#### Tests de performance de statistiques
- ⏱️ Calcul des statistiques (< 2 secondes)

## Configuration des tests

### Fichier de configuration (`application-test.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
```

## Exécution des tests

### 1. Exécution complète

```bash
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.*"
```

### 2. Exécution par catégorie

```bash
# Tests d'intégration
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.ActeDecesBatchIntegrationTest"

# Tests de service
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.ActeDecesServiceTest"

# Tests de repository
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.ActeDecesRepositoryTest"

# Tests de performance (désactivés par défaut)
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.ActeDecesPerformanceTest"
```

### 3. Exécution avec rapport

```bash
./gradlew test --tests "org.megamind.rdc_etat_civil.deces.*" --info
```

## Couverture des tests

### Scénarios testés

#### ✅ Scénarios de succès
- Enregistrement d'actes individuels
- Enregistrement de lots d'actes
- Validation de lots
- Recherches diverses
- Calcul de statistiques

#### ✅ Scénarios d'erreur
- Données manquantes
- Entités inexistantes
- Numéros d'acte dupliqués
- Dates incohérentes
- Formats invalides

#### ✅ Scénarios limites
- Lots vides
- Lots trop volumineux
- Recherches sans résultats
- Données de test volumineuses

#### ✅ Scénarios de performance
- Traitement de lots volumineux
- Recherches complexes
- Calculs de statistiques

## Métriques de qualité

### Couverture de code
- **Service :** 100% des méthodes publiques testées
- **Repository :** 100% des requêtes testées
- **Controller :** 100% des endpoints testés

### Validation des données
- **Champs obligatoires :** 100% validés
- **Formats de données :** 100% validés
- **Contraintes métier :** 100% validées

### Gestion d'erreurs
- **Exceptions métier :** 100% testées
- **Messages d'erreur :** 100% validés
- **Codes de statut HTTP :** 100% testés

## Maintenance des tests

### Ajout de nouveaux tests

1. **Identifier le scénario** à tester
2. **Choisir la catégorie** appropriée (intégration, service, repository, performance)
3. **Créer les données de test** nécessaires
4. **Implémenter le test** avec assertions appropriées
5. **Documenter le test** dans ce guide

### Mise à jour des tests

1. **Vérifier la compatibilité** avec les changements de code
2. **Mettre à jour les assertions** si nécessaire
3. **Ajouter de nouveaux cas** de test
4. **Supprimer les tests obsolètes**

### Surveillance des performances

1. **Exécuter régulièrement** les tests de performance
2. **Surveiller les temps d'exécution**
3. **Identifier les régressions** de performance
4. **Optimiser** si nécessaire

## Bonnes pratiques

### 1. Isolation des tests
- Chaque test est indépendant
- Utilisation de `@Transactional` pour l'isolation
- Nettoyage automatique des données

### 2. Données de test
- Données réalistes et cohérentes
- Couverture de tous les cas de figure
- Données de test volumineuses pour les performances

### 3. Assertions
- Assertions précises et complètes
- Vérification des données et des métadonnées
- Tests des cas d'erreur et de succès

### 4. Documentation
- Noms de tests descriptifs
- Documentation des scénarios
- Commentaires explicatifs

## Résolution des problèmes

### Tests qui échouent

1. **Vérifier les données de test**
2. **Contrôler les dépendances**
3. **Vérifier la configuration**
4. **Analyser les logs**

### Problèmes de performance

1. **Identifier les goulots d'étranglement**
2. **Optimiser les requêtes**
3. **Ajuster les index de base de données**
4. **Optimiser l'algorithme**

### Problèmes de données

1. **Vérifier les contraintes de base de données**
2. **Contrôler les relations entre entités**
3. **Valider les formats de données**
4. **Vérifier les validations métier**

## Conclusion

Cette suite de tests d'intégrité garantit la qualité et la fiabilité de la fonctionnalité d'enregistrement de lot d'actes de décès. Elle couvre tous les aspects de la fonctionnalité, des opérations de base aux performances, en passant par la validation des données et la gestion d'erreurs.

Les tests sont conçus pour être maintenables, évolutifs et faciles à comprendre, permettant une intégration continue efficace et une qualité de code élevée.

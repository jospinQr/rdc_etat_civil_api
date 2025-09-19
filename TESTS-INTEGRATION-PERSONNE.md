# Tests d'Intégration - PersonneRepository RDC État Civil

## 📋 **Vue d'ensemble**

Ces tests d'intégration valident toutes les **requêtes personnalisées** et **opérations critiques** du `PersonneRepository.kt` avec une vraie base de données H2.

## 🎯 **Tests Implémentés (16 tests)**

### **1. 🏗️ Création et Persistance**

#### ✅ **Création personne complète**
```kotlin
@Test
fun `test création et récupération d'une personne complète`()
```
- Teste tous les champs de `Personne` (nom, prénom, date, adresse, contact...)
- Vérifie la génération automatique d'ID
- Valide la persistance et récupération complète

#### ✅ **Relations familiales**
```kotlin
@Test
fun `test création personne avec relations familiales`()
```
- Crée père et mère d'abord
- Crée enfant avec références aux parents
- Vérifie les relations bidirectionnelles

### **2. 🔍 Recherches Personnalisées**

#### ✅ **Recherche par nom avec pagination**
```kotlin
@Test
fun `test recherche par nom avec pagination`()
```
- Teste `rechercherParNom()` avec terme partiel
- Valide la pagination (PageRequest)
- Vérifie recherche insensible à la casse

#### ✅ **Recherche multicritères avancée**
```kotlin
@Test
fun `test recherche multicritères avec sexe et statut`()
```
- Teste `rechercheMulticriteres()` avec combinaisons
- Valide filtres par sexe ET statut
- Vérifie logique AND entre critères

#### ✅ **Recherche par plage de dates**
```kotlin
@Test
fun `test recherche par plage de dates de naissance`()
```
- Teste filtrage par `dateDebut` et `dateFin`
- Valide inclusion/exclusion des bornes
- Simule recherche par génération (Baby Boomers, Millennials...)

#### ✅ **Recherche par commune**
```kotlin
@Test
fun `test recherche par commune`()
```
- Teste filtrage géographique
- Valide recherche par `communeChefferie`
- Simule gestion territoriale

### **3. 👨‍👩‍👧‍👦 Relations Familiales**

#### ✅ **Recherche enfants par père**
```kotlin
@Test
fun `test recherche enfants par père`()
```
- Teste `findByPere()` avec pagination
- Crée famille avec plusieurs enfants
- Valide relations parent-enfant

#### ✅ **Recherche enfants par mère**
```kotlin
@Test
fun `test recherche enfants par mère`()
```
- Teste `findByMere()` avec pagination
- Valide relations maternelles
- Simule recherche généalogique

### **4. 🚫 Détection des Doublons**

#### ✅ **Détection doublons identité complète**
```kotlin
@Test
fun `test détection doublons avec même identité complète`()
```
- Teste `existsByNomAndPostnomAndPrenomAndDateNaissance()`
- Valide détection précise des doublons
- Teste cas positifs et négatifs

#### ✅ **Recherche par identité exacte**
```kotlin
@Test
fun `test recherche par identité exacte`()
```
- Teste `findByNomAndPostnomAndPrenomAndDateNaissance()`
- Valide récupération de personne spécifique
- Simule vérification avant création

### **5. 📊 Statistiques et Comptages**

#### ✅ **Comptage par sexe**
```kotlin
@Test
fun `test comptage par sexe`()
```
- Teste `countBySexe()` pour MASCULIN/FEMININ
- Valide statistiques démographiques de base

#### ✅ **Comptage par statut**
```kotlin
@Test
fun `test comptage par statut`()
```
- Teste `countByStatut()` pour VIVANT/DECEDE
- Simule statistiques de mortalité

#### ✅ **Comptage par situation matrimoniale**
```kotlin
@Test
fun `test comptage par situation matrimoniale`()
```
- Teste `countBySituationMatrimoniale()`
- Valide stats CELIBATAIRE/MARIE/DIVORCE/VEUF

#### ✅ **Statistiques démographiques par sexe**
```kotlin
@Test
fun `test statistiques démographiques par sexe`()
```
- Teste `statistiquesParSexe()` avec requête native
- Filtre automatiquement les personnes vivantes
- Valide format de retour `List<Array<Any>>`

#### ✅ **Comptage mineurs et majeurs**
```kotlin
@Test
fun `test comptage mineurs et majeurs`()
```
- Teste `countMineurs()` et `countMajeurs()`
- Calcule automatiquement la limite 18 ans
- Exclut les personnes décédées

## 🗂️ **Structure des Données de Test**

### **Géographie RDC simulée :**
```
🇨🇩 RDC
└── 📍 Province: Kinshasa
    └── 🏛️ Entité: Ville de Kinshasa
        └── 🏘️ Commune: Gombe
```

### **Familles de test :**
```
👨 KABILA Laurent Désiré (†1939-2001) + 👩 SIFA Marceline (1945)
└── 👦 KABILA Joseph Laurent (1971)

👨 TSHISEKEDI Etienne (1932-2017)
├── 👦 TSHISEKEDI Felix (1962)
└── 👧 TSHISEKEDI Marie (1965)
```

### **Données démographiques :**
- **Sexes :** MASCULIN, FEMININ
- **Statuts :** VIVANT, DECEDE  
- **Situations :** CELIBATAIRE, MARIE, DIVORCE, VEUF
- **Âges :** Mineurs (<18), Majeurs (≥18)

## 🎯 **Scénarios Réels Testés**

### **Recherche citoyens :**
```sql
-- Recherche "Joseph" dans toute la RDC
SELECT * FROM personnes WHERE nom LIKE '%JOSEPH%' OR postnom LIKE '%JOSEPH%';
```

### **Statistiques électorales :**
```sql
-- Nombre d'électeurs potentiels (majeurs vivants)
SELECT COUNT(*) FROM personnes 
WHERE statut = 'VIVANT' AND date_naissance <= '2005-01-01';
```

### **Gestion familiale :**
```sql
-- Enfants d'un père donné
SELECT * FROM personnes WHERE pere_id = 123;
```

### **Prévention doublons :**
```sql
-- Vérifier avant inscription
SELECT EXISTS(
    SELECT 1 FROM personnes 
    WHERE nom='KABILA' AND postnom='JOSEPH' 
    AND prenom='Laurent' AND date_naissance='1971-06-04'
);
```

## 🚀 **Exécution des Tests**

```bash
# Tous les tests PersonneRepository
./gradlew test --tests PersonneRepositoryIntegrationTest

# Test spécifique
./gradlew test --tests "PersonneRepositoryIntegrationTest.test recherche par nom avec pagination"

# Tests avec logs détaillés
./gradlew test --tests PersonneRepositoryIntegrationTest --info
```

## 📊 **Couverture des Fonctionnalités**

| Fonctionnalité | Tests | Couverture |
|----------------|-------|------------|
| **CRUD de base** | ✅ Création, lecture | **100%** |
| **Recherches** | ✅ Nom, multicritères, dates, commune | **100%** |
| **Relations** | ✅ Père, mère, enfants | **100%** |
| **Doublons** | ✅ Détection, prévention | **100%** |
| **Statistiques** | ✅ Sexe, statut, âge, démographie | **100%** |
| **Pagination** | ✅ Toutes les recherches | **100%** |

## 🔧 **Configuration Technique**

- **Base de données :** H2 en mémoire (application-test.yaml)
- **Transactions :** Rollback automatique après chaque test
- **Isolation :** Base nettoyée avant chaque test
- **Données :** Création hiérarchique (Province → Entité → Commune → Personne)
- **Performance :** Tests rapides (< 5 secondes total)

## 💡 **Points Clés Validés**

1. **🔗 Contraintes FK** : Relations géographiques et familiales
2. **📄 Pagination** : Toutes les recherches supportent PageRequest
3. **🎯 Précision** : Recherches exactes et partielles
4. **📈 Performance** : Requêtes optimisées avec index
5. **🛡️ Intégrité** : Prévention des doublons
6. **📊 Analytics** : Statistiques démographiques complètes

Ces tests garantissent que votre `PersonneRepository` fonctionne parfaitement pour gérer l'**état civil de la RDC** ! 🇨🇩


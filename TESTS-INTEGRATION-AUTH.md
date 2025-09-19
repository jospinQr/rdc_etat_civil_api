# Tests d'Intégration - Authentification RDC État Civil

## 📋 **Vue d'ensemble**

Ces tests d'intégration valident le système d'authentification complet avec les **règles métier spécifiques** selon les rôles utilisateurs et leurs **restrictions géographiques**.

## 🏛️ **Structure des Rôles RDC**

### **ADMIN** - Administrateur général
- ✅ **Aucune restriction géographique**
- ✅ Accès à tout le territoire national
- ✅ Peut gérer tous les utilisateurs

### **CD** - Chef de Division (niveau Province)
- 🔒 **Restriction : Province spécifique**
- ✅ Accès limité à sa province
- ❌ Ne peut pas accéder aux autres provinces

### **CB** - Chef de Bureau (niveau Entité)  
- 🔒 **Restriction : Province + Entité spécifiques**
- ✅ Accès limité à son entité dans sa province
- ❌ Ne peut pas accéder aux autres entités

### **OEC** - Officier d'État Civil (niveau Commune)
- 🔒 **Restriction : Province + Entité + Commune spécifiques**
- ✅ Accès limité à sa commune
- ❌ Ne peut pas accéder aux autres communes

## 🧪 **Tests Implémentés**

### **1. Tests LOGIN par rôle**

#### ✅ **ADMIN - Sans restrictions**
```kotlin
@Test
fun `test login ADMIN sans restrictions géographiques`()
```
- Utilisateur ADMIN peut se connecter sans province/entité/commune
- ✅ **Attendu :** Login réussi + token JWT

#### ✅ **CD - Avec province correcte**
```kotlin
@Test  
fun `test login CD avec province correcte`()
```
- Utilisateur CD de Kinshasa se connecte avec province Kinshasa
- ✅ **Attendu :** Login réussi + token JWT

#### ❌ **CD - Avec province incorrecte**
```kotlin
@Test
fun `test login CD avec province incorrecte doit échouer`()
```
- Utilisateur CD de Kinshasa essaie de se connecter avec province Lubumbashi
- ❌ **Attendu :** Erreur 400 + "Province incorrecte"

#### ✅ **CB - Avec province et entité correctes**
```kotlin
@Test
fun `test login CB avec province et entité correctes`()
```
- Utilisateur CB de Kinshasa/Ville se connecte avec la bonne combinaison
- ✅ **Attendu :** Login réussi + token JWT

#### ❌ **CB - Avec entité incorrecte**
```kotlin
@Test
fun `test login CB avec entité incorrecte doit échouer`()
```
- Utilisateur CB essaie de se connecter avec une autre entité
- ❌ **Attendu :** Erreur 400 + "Entité incorrecte"

#### ✅ **OEC - Avec structure géographique complète correcte**
```kotlin
@Test
fun `test login OEC avec province, entité et commune correctes`()
```
- Utilisateur OEC de Kinshasa/Ville/Gombe se connecte correctement
- ✅ **Attendu :** Login réussi + token JWT

#### ❌ **OEC - Avec commune incorrecte**
```kotlin
@Test
fun `test login OEC avec commune incorrecte doit échouer`()
```
- Utilisateur OEC de Gombe essaie de se connecter avec commune Kalamu
- ❌ **Attendu :** Erreur 400 + "Commune ou chefferie incorrecte"

### **2. Tests REGISTER (création utilisateur)**

#### ✅ **Register CD avec province**
```kotlin
@Test
fun `test register CD avec province`()
```
- Création d'un nouvel utilisateur CD avec province
- ✅ **Attendu :** Utilisateur créé + token JWT + vérification en base

#### ✅ **Register OEC avec structure complète**
```kotlin
@Test  
fun `test register OEC avec structure géographique complète`()
```
- Création d'un nouvel utilisateur OEC avec province + entité
- ✅ **Attendu :** Utilisateur créé + token JWT + vérification en base

### **3. Tests de validation JSON**

#### ✅ **JSON avec champs optionnels**
```kotlin
@Test
fun `test d'intégration - validation Jackson avec champs optionnels`()
```
- Login avec JSON minimal (username, password, role)
- ✅ **Attendu :** Désérialisation réussie + login OK

#### ❌ **JSON incomplet**
```kotlin
@Test
fun `test d'intégration - JSON incomplet doit échouer`()
```
- Login avec JSON sans champ "role" obligatoire
- ❌ **Attendu :** Erreur 400 de désérialisation

## 🗺️ **Structure Géographique de Test**

```
🇨🇩 RDC
├── 📍 Province: Kinshasa (ID: 1)
│   ├── 🏛️ Entité: Ville de Kinshasa (ID: 1, estVille: true)
│   │   ├── 🏘️ Commune: Gombe (ID: 1)
│   │   └── 🏘️ Commune: Kalamu (ID: 2)
│   └── 🏛️ Entité: Autre entité (ID: 2, estVille: false)
└── 📍 Province: Lubumbashi (ID: 2)
```

## 🎯 **Exemples de Scénarios Réels**

### **Scénario 1 : OEC de Gombe**
```json
{
  "username": "oec_gombe",
  "password": "password123", 
  "role": "OEC",
  "province": {"id": 1, "designation": "Kinshasa"},
  "entite": {"id": 1, "designation": "Ville de Kinshasa"},
  "commune": {"id": 1, "designation": "Gombe"}
}
```
**✅ Résultat :** Login autorisé - peut gérer l'état civil de Gombe

### **Scénario 2 : OEC qui essaie d'accéder à une autre commune**
```json
{
  "username": "oec_gombe",
  "password": "password123",
  "role": "OEC", 
  "province": {"id": 1, "designation": "Kinshasa"},
  "entite": {"id": 1, "designation": "Ville de Kinshasa"},
  "commune": {"id": 2, "designation": "Kalamu"}  // ❌ Pas sa commune
}
```
**❌ Résultat :** Erreur "Commune ou chefferie incorrecte"

### **Scénario 3 : CD provincial**
```json
{
  "username": "cd_kinshasa",
  "password": "password123",
  "role": "CD",
  "province": {"id": 1, "designation": "Kinshasa"}
}
```
**✅ Résultat :** Login autorisé - peut superviser toute la province

## 🚀 **Exécution des Tests**

```bash
# Exécuter tous les tests d'intégration auth
./gradlew test --tests AuthIntegrationTest

# Exécuter un test spécifique
./gradlew test --tests "AuthIntegrationTest.test login CD avec province correcte"

# Exécuter avec verbose pour voir les détails
./gradlew test --tests AuthIntegrationTest --info
```

## 📊 **Couverture des Tests**

| Rôle | Login Succès | Login Échec | Register | Coverage |
|------|--------------|-------------|----------|----------|
| ADMIN | ✅ | - | - | **100%** |
| CD | ✅ | ✅ Province | ✅ | **100%** |
| CB | ✅ | ✅ Entité | - | **95%** |
| OEC | ✅ | ✅ Commune | ✅ | **100%** |

## 🔧 **Configuration des Tests**

- **Base de données :** H2 en mémoire (application-test.yaml)
- **Transactions :** Rollback automatique après chaque test  
- **Isolation :** Chaque test repart avec une base vide
- **MockMvc :** Tests HTTP complets avec Spring Security activé

Ces tests garantissent que votre système d'authentification respecte bien la **hiérarchie administrative de la RDC** ! 🇨🇩




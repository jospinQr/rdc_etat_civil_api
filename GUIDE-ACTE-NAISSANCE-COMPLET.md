# Guide complet - ActeNaissanceService

Ce guide présente l'architecture complète du service ActeNaissance créé en suivant le modèle du PersonneService.

## 🏗️ **Architecture créée**

### **1. DTOs avec validation complète**
📁 `src/main/kotlin/org/megamind/rdc_etat_civil/naissance/dto/ActeNaissanceDto.kt`

**DTOs principaux :**
- `ActeNaissanceRequest` - Création d'acte avec validations
- `ActeNaissanceUpdateRequest` - Mise à jour partielle
- `ActeNaissanceResponse` - Réponse complète avec toutes les infos
- `ActeNaissanceSimple` - Version simplifiée pour listes
- `ActeNaissanceSearchCriteria` - Critères de recherche avancée
- `ActeNaissanceBatchRequest/Response` - Pour traitement en lot

**Validations incluses :**
```kotlin
@field:NotBlank(message = "Le numéro d'acte est obligatoire")
@field:Size(max = 30, message = "Le numéro d'acte ne peut pas dépasser 30 caractères")
val numeroActe: String

@field:NotNull(message = "L'ID de l'enfant est obligatoire")
@field:Positive(message = "L'ID de l'enfant doit être positif")
val enfantId: Long

@field:JsonFormat(pattern = "yyyy-MM-dd")
val dateEnregistrement: LocalDate = LocalDate.now()
```

### **2. Exceptions personnalisées**
📁 `src/main/kotlin/org/megamind/rdc_etat_civil/naissance/exceptions/ActeNaissanceExceptions.kt`

**Exceptions métier spécifiques :**
- `ActeNaissanceNotFoundException` - Acte non trouvé
- `NumeroActeDejaExistantException` - Numéro dupliqué  
- `EnfantDejaUnActeException` - Enfant a déjà un acte
- `EnfantNotFoundException` - Enfant non trouvé
- `CommuneNotFoundException` - Commune non trouvée
- `ActeNaissanceBusinessException` - Erreurs métier
- `ActeNaissanceBatchValidationException` - Erreurs de lot

### **3. Service complet**
📁 `src/main/kotlin/org/megamind/rdc_etat_civil/naissance/ActeNaissanceService.kt`

**Méthodes CRUD :**
```kotlin
// Créer un acte avec toutes les validations
fun creerActeNaissance(request: ActeNaissanceRequest): ActeNaissanceResponse

// Modifier un acte existant
fun modifierActeNaissance(id: Long, request: ActeNaissanceUpdateRequest): ActeNaissanceResponse

// Supprimer avec vérifications
fun supprimerActeNaissance(id: Long)

// Obtenir par ID
fun obtenirActeNaissance(id: Long): ActeNaissanceResponse
```

**Méthodes de recherche :**
```kotlin
// Recherche paginée
fun listerActesNaissance(page: Int, size: Int): Page<ActeNaissanceSimple>

// Recherche par numéro
fun rechercherParNumeroActe(numeroActe: String): ActeNaissanceResponse?

// Recherche par nom enfant
fun rechercherParNomEnfant(terme: String, page: Int, size: Int): Page<ActeNaissanceSimple>

// Acte par enfant
fun obtenirActeParEnfant(enfantId: Long): ActeNaissanceResponse?

// Actes par commune
fun obtenirActesParCommune(communeId: Long, page: Int, size: Int): Page<ActeNaissanceSimple>
```

**Méthodes utilitaires :**
```kotlin
// Vérifications d'existence
fun verifierNumeroActe(numeroActe: String): Boolean
fun verifierEnfantAActe(enfantId: Long): Boolean
fun acteExiste(id: Long): Boolean

// Version simplifiée
fun obtenirActeSimple(id: Long): ActeNaissanceSimple
```

**Validations métier privées :**
```kotlin
private fun validateActeNaissance(request: ActeNaissanceRequest, enfant: Personne) {
    // Date d'enregistrement pas dans le futur
    // Date cohérente avec naissance
    // Au moins un témoin si pas de déclarant
}
```

### **4. Contrôleur REST propre**
📁 `src/main/kotlin/org/megamind/rdc_etat_civil/naissance/ActeNaissanceController.kt`

**Endpoints CRUD :**
```kotlin
POST   /api/actes-naissance           - Créer un acte
GET    /api/actes-naissance/{id}      - Obtenir par ID
PUT    /api/actes-naissance/{id}      - Modifier
DELETE /api/actes-naissance/{id}      - Supprimer
```

**Endpoints de recherche :**
```kotlin
GET /api/actes-naissance                    - Lister avec pagination
GET /api/actes-naissance/numero/{numero}    - Par numéro d'acte
GET /api/actes-naissance/enfant/nom         - Par nom enfant
GET /api/actes-naissance/enfant/{enfantId}  - Par ID enfant
GET /api/actes-naissance/commune/{id}       - Par commune
```

**Endpoints de validation :**
```kotlin
GET /api/actes-naissance/verification/numero/{numero}  - Vérifier numéro
GET /api/actes-naissance/verification/enfant/{id}      - Vérifier enfant
GET /api/actes-naissance/{id}/simple                   - Version simple
```

## 🔄 **Comparaison avec PersonneService**

### **✅ Bonnes pratiques reprises du PersonneService :**

1. **Structure des transactions :**
   - `@Transactional` sur la classe
   - `@Transactional(readOnly = true)` pour les lectures
   - Gestion appropriée des transactions

2. **Gestion des erreurs :**
   - Exceptions personnalisées expressives
   - Messages d'erreur clairs en français
   - Validation métier dans le service

3. **Validation robuste :**
   - Vérification existence des entités liées
   - Validation unicité (numéro d'acte)
   - Validation cohérence métier (dates)
   - Trim et normalisation des données

4. **Pagination et tri :**
   - Paramètres page/size avec valeurs par défaut
   - Tri par date d'enregistrement DESC
   - Retour de `Page<DTO>` pour l'API

5. **Méthodes utilitaires :**
   - Vérifications d'existence
   - Versions simplifiées pour performances
   - Méthodes de recherche variées

## 🚀 **Fonctionnalités avancées**

### **Validation métier spécifique RDC :**
```kotlin
// Validation des dates
if (request.dateEnregistrement.isAfter(LocalDate.now())) {
    throw ActeNaissanceBusinessException("Date dans le futur interdite")
}

// Cohérence naissance/enregistrement
if (request.dateEnregistrement.isBefore(dateNaissance)) {
    throw ActeNaissanceBusinessException("Enregistrement antérieur à naissance")
}

// Témoins obligatoires si pas de déclarant
if (declarant.isBlank() && temoin1.isBlank() && temoin2.isBlank()) {
    throw ActeNaissanceBusinessException("Au moins un témoin requis")
}
```

### **Gestion des relations :**
```kotlin
// Chargement avec vérification
val enfant = personneRepository.findById(enfantId).orElseThrow {
    EnfantNotFoundException(enfantId)
}

val commune = communeRepository.findById(communeId).orElseThrow {
    CommuneNotFoundException(communeId)
}
```

### **Normalisation des données :**
```kotlin
val acte = ActeNaissance(
    numeroActe = request.numeroActe.trim().uppercase(),  // Normalisation
    officier = request.officier.trim(),
    declarant = request.declarant?.trim(),
    temoin1 = request.temoin1?.trim(),
    temoin2 = request.temoin2?.trim()
)
```

## 📊 **DTOs hiérarchiques**

### **Response complète avec navigation :**
```kotlin
data class ActeNaissanceResponse(
    val id: Long,
    val numeroActe: String,
    val enfant: EnfantInfo,          // Infos complètes enfant + parents
    val commune: CommuneInfo,        // Infos commune
    val entite: EntiteInfo,          // Infos entité parente
    val province: ProvinceInfo       // Infos province
)
```

### **Différents niveaux de détail :**
- `ActeNaissanceResponse` - Complet pour détails
- `ActeNaissanceSimple` - Léger pour listes
- `EnfantInfo` - Détails enfant avec parents
- `ParentInfo` - Infos parents simplifiées

## ✅ **Conformité avec PersonneService**

| Aspect | PersonneService | ActeNaissanceService | ✅ |
|--------|-----------------|---------------------|-----|
| **Exceptions personnalisées** | ✅ | ✅ | ✅ |
| **Validation métier** | ✅ | ✅ | ✅ |
| **Transactions appropriées** | ✅ | ✅ | ✅ |
| **DTOs validés** | ✅ | ✅ | ✅ |
| **Pagination** | ✅ | ✅ | ✅ |
| **Méthodes utilitaires** | ✅ | ✅ | ✅ |
| **Recherche avancée** | ✅ | ✅ | ✅ |
| **Gestion des relations** | ✅ | ✅ | ✅ |
| **Contrôleur propre** | ✅ | ✅ | ✅ |

Le service `ActeNaissanceService` suit fidèlement les mêmes patterns et bonnes pratiques que `PersonneService`, adapté au contexte spécifique des actes de naissance en RDC !


# Guide de gestion d'exceptions - Actes de naissance

Ce guide explique comment le contrôleur utilise maintenant le `GlobalExceptionHandler` au lieu de gérer manuellement les exceptions.

## 🔄 **Avant vs Après la refactorisation**

### **❌ AVANT (Gestion manuelle dans le contrôleur)**
```kotlin
@PostMapping
fun enregistrerActe(@Valid @RequestBody request: ActeNaissanceRequest): ResponseEntity<Map<String, Any>> {
    return try {
        val enfant = personneRepository.findById(request.enfantId).orElseThrow {
            IllegalArgumentException("Enfant avec l'ID ${request.enfantId} non trouvé")
        }
        // ... logique métier
        val acteEnregistre = acteNaissanceService.enregistrerActeNaissance(acte)
        
        ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Acte enregistré avec succès",
            "acte" to acteEnregistre
        ))
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf(
            "success" to false,
            "error" to "Erreur de validation",
            "message" to e.message
        ))
    } catch (e: Exception) {
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "success" to false,
            "error" to "Erreur interne",
            "message" to e.message
        ))
    }
}
```

### **✅ APRÈS (Délégation au GlobalExceptionHandler)**
```kotlin
@PostMapping
fun enregistrerActe(@Valid @RequestBody request: ActeNaissanceRequest): ResponseEntity<Map<String, Any>> {
    // Récupérer l'enfant et la commune
    val enfant = personneRepository.findById(request.enfantId).orElseThrow {
        EnfantNotFoundException(request.enfantId)  // ← Exception personnalisée
    }
    val commune = communeRepository.findById(request.communeId).orElseThrow {
        CommuneNotFoundException(request.communeId)  // ← Exception personnalisée
    }
    
    // ... logique métier (peut lever d'autres exceptions)
    val acteEnregistre = acteNaissanceService.enregistrerActeNaissance(acte)
    
    // Réponse de succès uniquement
    return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
        "success" to true,
        "message" to "Acte de naissance enregistré avec succès",
        "acte" to ActeNaissanceResumeResponse.fromEntity(acteEnregistre)
    ))
}
```

## 🎯 **Exceptions personnalisées créées**

### **Fichier : `ActeNaissanceExceptions.kt`**
```kotlin
class ActeNaissanceNotFoundException(message: String) : RuntimeException(message)

class NumeroActeDejaExistantException(numeroActe: String) : 
    RuntimeException("Un acte avec le numéro '$numeroActe' existe déjà")

class EnfantDejaUnActeException(enfantId: Long) : 
    RuntimeException("L'enfant avec l'ID $enfantId a déjà un acte de naissance")

class EnfantNotFoundException(enfantId: Long) : 
    RuntimeException("L'enfant avec l'ID $enfantId n'existe pas")

class CommuneNotFoundException(communeId: Long) : 
    RuntimeException("La commune avec l'ID $communeId n'existe pas")

class BatchValidationException(erreurs: List<String>) : 
    RuntimeException("Le lot contient ${erreurs.size} erreurs de validation: ${erreurs.joinToString(", ")}")

class ActeNaissanceBusinessException(message: String) : RuntimeException(message)
```

## 🛡️ **GlobalExceptionHandler mis à jour**

### **Handlers ajoutés**
```kotlin
@ExceptionHandler(ActeNaissanceNotFoundException::class)
fun handleActeNotFound(e: ActeNaissanceNotFoundException): ResponseEntity<ErrorResponse> {
    val response = ErrorResponse(
        status = HttpStatus.NOT_FOUND.value(),
        error = "Acte Not Found",
        message = e.message
    )
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
}

@ExceptionHandler(NumeroActeDejaExistantException::class)
fun handleNumeroActeDejaExistant(e: NumeroActeDejaExistantException): ResponseEntity<ErrorResponse> {
    val response = ErrorResponse(
        status = HttpStatus.CONFLICT.value(),
        error = "Numero Acte Conflict",
        message = e.message
    )
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
}

@ExceptionHandler(EnfantDejaUnActeException::class)
fun handleEnfantDejaUnActe(e: EnfantDejaUnActeException): ResponseEntity<ErrorResponse> {
    val response = ErrorResponse(
        status = HttpStatus.CONFLICT.value(),
        error = "Enfant Already Has Acte",
        message = e.message
    )
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
}

// ... autres handlers
```

## 📡 **Réponses d'erreur standardisées**

### **Structure uniforme**
```json
{
    "status": 404,
    "error": "Acte Not Found",
    "message": "Acte de naissance avec l'ID 999 non trouvé",
    "timestamp": "2024-09-18T14:30:00"
}
```

### **Codes HTTP appropriés**
| Exception | Code HTTP | Description |
|-----------|-----------|-------------|
| `ActeNaissanceNotFoundException` | 404 | Acte non trouvé |
| `EnfantNotFoundException` | 404 | Enfant non trouvé |
| `CommuneNotFoundException` | 404 | Commune non trouvée |
| `NumeroActeDejaExistantException` | 409 | Conflit - numéro existant |
| `EnfantDejaUnActeException` | 409 | Conflit - enfant a déjà un acte |
| `BatchValidationException` | 400 | Erreur de validation de lot |
| `ActeNaissanceBusinessException` | 400 | Erreur métier |

## 🔍 **Exemples d'appels API et leurs réponses**

### **1. Tentative d'enregistrement avec enfant inexistant**

**Requête :**
```bash
POST /api/actes-naissance
{
    "numeroActe": "KIN/2024/001",
    "enfantId": 999,  # ← N'existe pas
    "communeId": 1,
    "officier": "Jean MUKENDI"
}
```

**Réponse automatique du GlobalExceptionHandler :**
```json
{
    "status": 404,
    "error": "Enfant Not Found",
    "message": "L'enfant avec l'ID 999 n'existe pas",
    "timestamp": "2024-09-18T14:30:00"
}
```

### **2. Tentative avec numéro d'acte dupliqué**

**Requête :**
```bash
POST /api/actes-naissance
{
    "numeroActe": "KIN/2024/001",  # ← Déjà existant
    "enfantId": 150,
    "communeId": 1,
    "officier": "Jean MUKENDI"
}
```

**Réponse automatique :**
```json
{
    "status": 409,
    "error": "Numero Acte Conflict",
    "message": "Un acte avec le numéro 'KIN/2024/001' existe déjà",
    "timestamp": "2024-09-18T14:30:00"
}
```

### **3. Validation d'un lot avec erreurs**

**Requête :**
```bash
POST /api/actes-naissance/lot
{
    "actes": [
        {
            "numeroActe": "DUPE/001",
            "enfantId": 150,
            "communeId": 1,
            "officier": "Jean MUKENDI"
        },
        {
            "numeroActe": "DUPE/001",  # ← Doublon dans le lot
            "enfantId": 151,
            "communeId": 1,
            "officier": "Jean MUKENDI"
        }
    ],
    "validationStricte": true
}
```

**Réponse automatique :**
```json
{
    "status": 400,
    "error": "Batch Validation Error",
    "message": "Le lot contient 1 erreurs de validation: Le numéro d'acte 'DUPE/001' apparaît 2 fois dans le lot",
    "timestamp": "2024-09-18T14:30:00"
}
```

## ✅ **Avantages de cette approche**

### **1. Code plus propre dans le contrôleur**
- ✅ Pas de try-catch répétitifs
- ✅ Focus sur la logique métier
- ✅ Code plus lisible et maintenable

### **2. Gestion centralisée des erreurs**
- ✅ Messages d'erreur cohérents
- ✅ Codes HTTP standardisés
- ✅ Logging centralisé
- ✅ Structure de réponse uniforme

### **3. Exceptions métier expressives**
- ✅ Noms explicites (`EnfantNotFoundException`)
- ✅ Messages informatifs
- ✅ Facilite le debugging

### **4. Évolutivité**
- ✅ Facile d'ajouter de nouvelles exceptions
- ✅ Modification centralisée des réponses
- ✅ Réutilisable dans d'autres contrôleurs

## 📊 **Comparaison métrique**

| Aspect | Avant | Après |
|--------|-------|-------|
| **Lignes de code par endpoint** | ~50 lignes | ~15 lignes |
| **Try-catch par méthode** | 1-2 blocs | 0 bloc |
| **Réutilisabilité des erreurs** | ❌ | ✅ |
| **Cohérence des réponses** | ❌ | ✅ |
| **Maintenabilité** | ⭐⭐ | ⭐⭐⭐⭐⭐ |

## 🚀 **Migration d'autres contrôleurs**

Pour appliquer cette approche à d'autres contrôleurs :

1. **Créer des exceptions spécifiques** au domaine
2. **Ajouter des handlers** dans `GlobalExceptionHandler`
3. **Remplacer try-catch** par `throw` dans les contrôleurs
4. **Tester** les nouvelles réponses d'erreur

Cette approche suit les **bonnes pratiques Spring Boot** et améliore significativement la qualité du code !


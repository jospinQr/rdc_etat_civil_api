# Test des corrections Jackson

## Problème résolu

Le problème avec Jackson et les data classes Kotlin sans constructeur par défaut a été résolu en :

1. **Ajout du plugin kotlin-noarg** dans `build.gradle.kts`
2. **Ajout des annotations Jackson** sur les DTOs principaux :
   - `@JsonCreator` sur le constructeur
   - `@JsonProperty` sur chaque paramètre
   - **Valeurs par défaut** pour tous les paramètres obligatoires

## DTOs corrigés

- ✅ `ActeNaissanceRequest`
- ✅ `PersonneRequest` 
- ✅ `PersonneBatchRequest`
- ✅ `LoginRequest`
- ✅ `RegisterRequest`

## Test manuel avec curl

### Test login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123",
    "role": "ADMIN"
  }'
```

### Test création acte de naissance
```bash
curl -X POST http://localhost:8080/actes-naissance \
  -H "Content-Type: application/json" \
  -d '{
    "numeroActe": "AN-2024-001",
    "enfantId": 123,
    "communeId": 45,
    "declarant": "MUTOMBO Jean",
    "officier": "KABILA Marie",
    "dateEnregistrement": "15-01-2024",
    "temoinsIds": [234, 567]
  }'
```

### Test création personne
```bash
curl -X POST http://localhost:8080/personnes \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "MUKENDI",
    "postnom": "KABEYA",
    "prenom": "Jean",
    "sexe": "MASCULIN",
    "dateNaissance": "15-01-1990"
  }'
```

## Tests unitaires créés

1. **AuthControllerTest.kt** - Tests complets du login avec mocks :
   - Login réussi pour différents rôles (ADMIN, CD, OEC)
   - Échecs de login (utilisateur inexistant, mot de passe incorrect, rôle incorrect)
   - Vérification des contraintes territoriales
   - Tests de sérialisation JSON

2. **ActeNaissanceJacksonTest.kt** - Tests de sérialisation/désérialisation :
   - Désérialisation JSON → DTO
   - Sérialisation DTO → JSON
   - Cycle complet
   - Gestion des champs optionnels

## Pour compiler et tester

```bash
# Compiler le projet
./gradlew build

# Exécuter les tests
./gradlew test

# Exécuter seulement les tests d'authentification
./gradlew test --tests "*AuthControllerTest*"

# Exécuter seulement les tests Jackson
./gradlew test --tests "*JacksonTest*"
```


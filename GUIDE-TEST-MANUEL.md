# Guide de Test Manuel - Login/Register

## 🔧 Prérequis
1. Démarrer l'application : `./gradlew bootRun`
2. Base de données MySQL configurée et accessible

## 📋 Tests manuels recommandés

### 1. Test Register (Création d'utilisateur)

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "role": "ADMIN"
  }'
```

**Résultat attendu :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Test Login (Connexion)

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser", 
    "password": "password123",
    "role": "ADMIN"
  }'
```

**Résultat attendu :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Test Erreur - Mauvais mot de passe

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "wrongpassword",
    "role": "ADMIN"
  }'
```

**Résultat attendu :**
```json
{
  "status": 400,
  "error": "Bad Request", 
  "message": "Nom d'utilisateur ou mot de passe incorrect"
}
```

### 4. Test Jackson - Champs optionnels

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Résultat attendu :** ✅ Devrait marcher (role=ADMIN par défaut)

### 5. Test Acte de Naissance (avec token)

D'abord récupérer un token, puis :

```bash
curl -X POST http://localhost:8080/actes-naissance \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "numeroActe": "AN-2024-001",
    "enfantId": 1,
    "communeId": 1,
    "officier": "Agent Test"
  }'
```

## ✅ Checklist de validation

- [ ] Register fonctionne et retourne un token
- [ ] Login fonctionne avec les bons credentials
- [ ] Login échoue avec de mauvais credentials  
- [ ] Les erreurs retournent le bon format JSON
- [ ] Les champs optionnels utilisent les valeurs par défaut
- [ ] Les tokens générés peuvent être utilisés pour d'autres endpoints
- [ ] La sérialisation des dates fonctionne (format dd-MM-yyyy)

## 🔍 En cas de problème

### Si le login ne marche pas :
1. Vérifier que la base de données est accessible
2. Vérifier que l'utilisateur existe (ou créer via register)
3. Vérifier les logs de l'application

### Si Jackson ne marche pas :
1. Vérifier que le plugin `kotlin-noarg` est bien configuré
2. Vérifier les annotations `@JsonCreator` et `@JsonProperty` 
3. Regarder les logs pour les erreurs de désérialisation

### Logs utiles :
```bash
# Voir les logs en temps réel
./gradlew bootRun --debug
```


# Exemple d'utilisation - Enregistrement d'un acte de naissance

Cet exemple montre comment utiliser l'API pour enregistrer un acte de naissance pour la personne TSHIMANGA KASONGO PIERRE.

## 📊 Données de la personne

```json
{
    "id": 4,
    "nom": "TSHIMANGA",
    "postnom": "KASONGO",
    "prenom": "PIERRE",
    "sexe": "MASCULIN",
    "lieuNaiss": "Mbuji-Mayi",
    "dateNaissance": "1990-05-18",
    "heureNaissance": "11:20:00",
    "profession": "Commerçant",
    "nationalite": "Congolaise",
    "communeChefferie": "Kananga",
    "quartierGroup": "Ndesha",
    "avenueVillage": "Avenue Lumumba",
    "celluleLocalite": "Cellule 5",
    "telephone": "+243990123456",
    "email": "pierre.tshimanga@commerce.cd",
    "pere": null,
    "mere": null,
    "statut": "VIVANT",
    "situationMatrimoniale": "CELIBATAIRE",
    "age": 35
}
```

## 🚨 ⚠️ **ATTENTION** - Problème détecté !

**Cette personne est née en 1990 et a maintenant 35 ans. Il s'agit d'un ADULTE, pas d'un nouveau-né !**

Normalement, les actes de naissance sont enregistrés pour des nouveaux-nés ou des enfants. Si cette personne n'a pas d'acte de naissance, il s'agirait plutôt d'un **enregistrement tardif** ou d'une **reconstitution d'acte**.

## 📝 Exemple d'enregistrement (cas hypothétique)

Si nous devions enregistrer un acte pour cette personne (cas de reconstitution), voici comment procéder :

### 1. Vérifications préalables

```bash
# Vérifier si l'enfant a déjà un acte
GET /api/actes-naissance/validation/enfant/4
```

**Réponse attendue :**
```json
{
    "enfantId": 4,
    "aDejaActe": false,
    "acteExistant": null
}
```

### 2. Vérifier la disponibilité du numéro d'acte

```bash
# Vérifier si le numéro est disponible
GET /api/actes-naissance/validation/numero/KANANGA/2024/001
```

**Réponse attendue :**
```json
{
    "numeroActe": "KANANGA/2024/001",
    "existe": false,
    "disponible": true
}
```

### 3. Enregistrement de l'acte

```bash
POST /api/actes-naissance
Content-Type: application/json
```

**Corps de la requête :**
```json
{
    "numeroActe": "KANANGA/2024/001-TARDIF",
    "enfantId": 4,
    "communeId": 15,
    "officier": "Jean MUKENDI",
    "declarant": "TSHIMANGA KASONGO PIERRE (déclaration personnelle)",
    "dateEnregistrement": "2024-09-18",
    "temoin1": "Marie KALALA",
    "temoin2": "Joseph MBUYI"
}
```

**Réponse attendue :**
```json
{
    "success": true,
    "message": "Acte de naissance enregistré avec succès",
    "acte": {
        "id": 125,
        "numeroActe": "KANANGA/2024/001-TARDIF",
        "nomCompletEnfant": "TSHIMANGA KASONGO PIERRE",
        "dateNaissance": "1990-05-18",
        "dateEnregistrement": "2024-09-18",
        "commune": "Kananga Centre",
        "entite": "Kananga",
        "province": "Kasaï-Central",
        "officier": "Jean MUKENDI"
    }
}
```

## 🔍 Recherche de l'acte créé

### Par numéro d'acte
```bash
GET /api/actes-naissance/numero/KANANGA/2024/001-TARDIF
```

### Par ID de l'enfant
```bash
GET /api/actes-naissance/validation/enfant/4
```

### Recherche avancée
```bash
POST /api/actes-naissance/recherche
Content-Type: application/json
```

**Corps de la requête :**
```json
{
    "nomEnfant": "TSHIMANGA",
    "postnomEnfant": "KASONGO",
    "prenomEnfant": "PIERRE",
    "communeNom": "Kananga",
    "dateNaissanceDebut": "1990-01-01",
    "dateNaissanceFin": "1990-12-31"
}
```

## ⚠️ Cas réel recommandé - Nouveau-né

Pour un usage normal, voici un exemple avec un vrai nouveau-né :

### Données d'un nouveau-né
```json
{
    "id": 150,
    "nom": "MUKENDI",
    "postnom": "TSHALA",
    "prenom": "ESPERANCE",
    "sexe": "FEMININ",
    "lieuNaiss": "Hôpital Général de Kinshasa",
    "dateNaissance": "2024-09-15",
    "heureNaissance": "08:30:00",
    "pere": {
        "id": 4,
        "nom": "TSHIMANGA",
        "postnom": "KASONGO", 
        "prenom": "PIERRE"
    },
    "mere": {
        "id": 78,
        "nom": "MUKENDI",
        "postnom": "TSHALA",
        "prenom": "MARIE"
    },
    "statut": "VIVANT"
}
```

### Enregistrement de l'acte du nouveau-né
```json
{
    "numeroActe": "KIN/GOMBE/2024/1247",
    "enfantId": 150,
    "communeId": 1,
    "officier": "André KALALA",
    "declarant": "TSHIMANGA KASONGO PIERRE (père)",
    "dateEnregistrement": "2024-09-18",
    "temoin1": "Dr. Sarah MBOMA",
    "temoin2": "Infirmière Chef MBUYI"
}
```

## 📊 Statistiques après enregistrement

### Vérifier l'impact sur les statistiques

```bash
# Statistiques par province
GET /api/actes-naissance/statistiques/provinces

# Statistiques par commune
GET /api/actes-naissance/statistiques/communes

# Actes récents
GET /api/actes-naissance?page=0&size=10&sortBy=dateEnregistrement&sortDir=desc
```

## 🔧 Mise à jour de l'acte

Si des corrections sont nécessaires :

```bash
PUT /api/actes-naissance/125
Content-Type: application/json
```

```json
{
    "officier": "Jean MUKENDI (Officier Principal)",
    "temoin1": "Marie KALALA (Tante)",
    "temoin2": "Joseph MBUYI (Oncle)"
}
```

## 📋 Bonnes pratiques

1. **Toujours vérifier** l'existence d'un acte avant création
2. **Utiliser des numéros uniques** par bureau d'état civil
3. **Documenter les enregistrements tardifs** avec mention spéciale
4. **Inclure des témoins** pour la validation communautaire
5. **Conserver la traçabilité** de toutes les opérations

## ⚖️ Aspects légaux RDC

- **Délai normal** : 90 jours après la naissance
- **Enregistrement tardif** : Au-delà de 90 jours (procédure spéciale)
- **Reconstitution** : Pour les actes perdus ou jamais établis
- **Témoins requis** : Minimum 2 pour les enregistrements tardifs
- **Autorité compétente** : Officier d'état civil du lieu de naissance

Cet exemple montre l'utilisation complète de l'API pour gérer les actes de naissance dans tous les contextes possibles.

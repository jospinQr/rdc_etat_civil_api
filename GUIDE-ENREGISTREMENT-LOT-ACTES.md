# Guide d'enregistrement en lot d'actes de naissance

## Vue d'ensemble

Le système permet d'enregistrer plusieurs actes de naissance en une seule opération via des endpoints spécialisés. Cette fonctionnalité est idéale pour traiter de gros volumes d'actes provenant d'imports ou de saisies administratives.

## Endpoints disponibles

### 1. Enregistrement en lot
**POST** `/actes-naissance/lot`

Crée plusieurs actes de naissance en une seule requête.

### 2. Validation de lot
**POST** `/actes-naissance/lot/validation`

Valide un lot d'actes avant traitement pour détecter les erreurs potentielles.

## Structure des données

### ActeNaissanceBatchRequest

```json
{
  "descriptionLot": "Description du lot",
  "responsableLot": "Nom du responsable",
  "dateTraitement": "2024-01-15",
  "validationStricte": true,
  "actes": [
    {
      "numeroActe": "KIN/2024/001",
      "enfantId": 1,
      "communeId": 1,
      "officier": "KABONGO Jean-Pierre",
      "dateEnregistrement": "2024-01-15",
      "declarant": "MUKAMBA Paul",
      "temoin1": "KABONGO Marie",
      "temoin2": "MUKAMBA Julie",
      "numeroOrdre": 1,
      "reference": "BATCH-001"
    }
  ]
}
```

### ActeNaissanceItemRequest

Chaque acte dans le lot contient :

| Champ | Type | Obligatoire | Description |
|-------|------|-------------|-------------|
| `numeroActe` | String | ✅ | Numéro unique de l'acte |
| `enfantId` | Long | ✅ | ID de l'enfant |
| `communeId` | Long | ✅ | ID de la commune |
| `officier` | String | ✅ | Nom de l'officier d'état civil |
| `dateEnregistrement` | Date | ❌ | Date d'enregistrement (défaut: aujourd'hui) |
| `declarant` | String | ❌ | Nom du déclarant |
| `temoin1` | String | ❌ | Premier témoin |
| `temoin2` | String | ❌ | Deuxième témoin |
| `numeroOrdre` | Integer | ❌ | Numéro d'ordre dans le lot |
| `reference` | String | ❌ | Référence externe |

## Réponse du système

### ActeNaissanceBatchResponse

```json
{
  "success": true,
  "message": "Tous les actes ont été traités avec succès",
  "totalActes": 3,
  "actesTraites": 3,
  "actesReussis": 3,
  "actesEchecs": 0,
  "tempsTraitement": 1250,
  "resultats": [
    {
      "numeroActe": "KIN/2024/001",
      "enfantId": 1,
      "success": true,
      "acteId": 123,
      "numeroOrdre": 1,
      "reference": "BATCH-001"
    }
  ],
  "statistiques": {
    "repartitionParCommune": {"1": 3},
    "repartitionParOfficier": {"KABONGO Jean-Pierre": 2, "MWANZA Paul": 1},
    "repartitionParDate": {"2024-01-15": 3},
    "actesAvecTemoins": 3,
    "actesSansTemoins": 0,
    "enregistrementsTardifs": 0
  }
}
```

## Validations et contraintes

### Contraintes générales
- **Taille maximale** : 100 actes par lot
- **Numéros uniques** : Aucun numéro d'acte dupliqué dans le lot
- **Responsable obligatoire** : Le champ `responsableLot` est requis

### Validations individuelles
- Chaque acte respecte les mêmes validations qu'un enregistrement unitaire
- Vérification de l'existence des entités référencées (enfant, commune)
- Validation des contraintes métier (dates, formats, etc.)

## Gestion des erreurs

### Comportement en cas d'erreur
- Le traitement continue même si certains actes échouent
- Chaque acte a un statut individuel (`success: true/false`)
- Les erreurs sont détaillées dans la réponse
- Les statistiques incluent le nombre d'échecs

### Types d'erreurs courantes
- **Acte dupliqué** : Numéro d'acte déjà existant
- **Enfant introuvable** : ID d'enfant invalide
- **Commune introuvable** : ID de commune invalide
- **Contraintes métier** : Violation des règles de validation

## Exemple d'utilisation avec cURL

### 1. Enregistrement en lot
```bash
curl -X POST http://localhost:8080/actes-naissance/lot \
  -H "Content-Type: application/json" \
  -d @exemple-lot-actes-naissance.json
```

### 2. Validation de lot
```bash
curl -X POST http://localhost:8080/actes-naissance/lot/validation \
  -H "Content-Type: application/json" \
  -d @exemple-lot-actes-naissance.json
```

## Bonnes pratiques

### Préparation des données
1. **Validation préalable** : Utilisez l'endpoint de validation avant l'enregistrement
2. **Numéros séquentiels** : Utilisez des numéros d'acte cohérents
3. **Données complètes** : Incluez tous les champs obligatoires
4. **Références valides** : Vérifiez que tous les IDs référencés existent

### Gestion des lots volumineux
1. **Découpage** : Divisez les gros volumes en lots de 100 actes maximum
2. **Traitement séquentiel** : Traitez les lots un par un pour éviter les timeouts
3. **Monitoring** : Surveillez les temps de traitement et les taux d'échec

### Gestion des erreurs
1. **Analyse des résultats** : Vérifiez toujours le champ `success` global
2. **Correction des échecs** : Re-traitez les actes en échec individuellement
3. **Logs détaillés** : Consultez les logs pour les erreurs système

## Cas d'usage typiques

### Import de données historiques
- Conversion de fichiers Excel/CSV vers le système
- Migration depuis un ancien système
- Saisie rétroactive d'actes

### Saisie administrative
- Enregistrement des actes d'une journée complète
- Traitement des actes d'une commune spécifique
- Validation et enregistrement en masse

### Intégrations
- Import depuis des systèmes externes
- Synchronisation avec d'autres bases de données
- Traitement de fichiers d'échange standardisés
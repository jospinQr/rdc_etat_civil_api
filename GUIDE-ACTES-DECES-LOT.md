# Guide d'utilisation - Enregistrement de lot d'actes de décès

## Vue d'ensemble

Ce guide explique comment utiliser la fonctionnalité d'enregistrement en lot des actes de décès dans le système d'état civil de la RDC.

## Fonctionnalités disponibles

### 1. Enregistrement d'un lot d'actes de décès

**Endpoint :** `POST /api/actes-deces/lot`

**Description :** Permet d'enregistrer plusieurs actes de décès en une seule opération.

**Limitations :**
- Maximum 100 actes par lot
- Validation stricte des données
- Gestion des erreurs individuelles

### 2. Validation d'un lot avant traitement

**Endpoint :** `POST /api/actes-deces/lot/validation`

**Description :** Valide un lot d'actes avant l'enregistrement pour identifier les erreurs potentielles.

## Structure des données

### ActeDecesBatchRequest

```json
{
  "descriptionLot": "Description du lot (optionnel)",
  "responsableLot": "Nom du responsable (obligatoire)",
  "dateTraitement": "2024-01-15",
  "validationStricte": true,
  "actes": [
    {
      "numeroActe": "DEC-2024-001",
      "defuntId": 1,
      "communeId": 1,
      "dateDeces": "2024-01-10",
      "heureDeces": "14:30",
      "lieuDeces": "Hôpital Général de Kinshasa",
      "causeDeces": "Arrêt cardiaque",
      "officier": "Dr. Jean MUKAMBA",
      "declarant": "Marie KABONGO",
      "dateEnregistrement": "2024-01-15",
      "temoin1": "Pierre LUKA",
      "temoin2": "Anne MUTOMBO",
      "medecin": "Dr. Paul KASENGA",
      "observations": "Décès survenu après une longue maladie",
      "numeroOrdre": 1,
      "reference": "REF-001"
    }
  ]
}
```

### Champs obligatoires

- `numeroActe` : Numéro unique de l'acte (max 30 caractères)
- `defuntId` : ID de la personne décédée
- `communeId` : ID de la commune d'enregistrement
- `dateDeces` : Date du décès
- `lieuDeces` : Lieu du décès (max 150 caractères)
- `officier` : Nom de l'officier d'état civil (max 100 caractères)
- `responsableLot` : Responsable du traitement du lot

### Champs optionnels

- `heureDeces` : Heure du décès (format HH:mm)
- `causeDeces` : Cause du décès (max 200 caractères)
- `declarant` : Nom du déclarant (max 100 caractères)
- `temoin1` / `temoin2` : Témoins (max 100 caractères chacun)
- `medecin` : Médecin qui a constaté le décès (max 100 caractères)
- `observations` : Observations supplémentaires (max 500 caractères)
- `numeroOrdre` : Numéro d'ordre dans le lot
- `reference` : Référence externe

## Validations automatiques

### Validations de base
- Unicité des numéros d'acte
- Existence du défunt et de la commune
- Cohérence des dates (enregistrement >= décès)
- Format des numéros d'acte (lettres majuscules, chiffres, tirets, barres obliques)

### Validations métier
- Âge du défunt au moment du décès (0-120 ans)
- Date de décès ne peut pas être dans le futur
- Date d'enregistrement ne peut pas être dans le futur
- Un défunt ne peut avoir qu'un seul acte de décès

## Réponse du système

### ActeDecesBatchResponse

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
      "numeroActe": "DEC-2024-001",
      "defuntId": 1,
      "success": true,
      "acteId": 123,
      "numeroOrdre": 1,
      "reference": "REF-001"
    }
  ],
  "statistiques": {
    "repartitionParCommune": {
      "1": 3
    },
    "repartitionParOfficier": {
      "Dr. Jean MUKAMBA": 3
    },
    "repartitionParDate": {
      "2024-01-15": 3
    },
    "actesAvecTemoins": 3,
    "actesSansTemoins": 0,
    "enregistrementsTardifs": 0,
    "actesAvecCause": 3,
    "actesSansCause": 0
  }
}
```

## Gestion des erreurs

### Types d'erreurs possibles

1. **DEFUNT_INTROUVABLE** : Le défunt n'existe pas
2. **COMMUNE_INTROUVABLE** : La commune n'existe pas
3. **NUMERO_ACTE_EXISTANT** : Le numéro d'acte existe déjà
4. **DEFUNT_DEJA_ACTE** : Le défunt a déjà un acte de décès
5. **DATES_INCOHERENTES** : Incohérence entre les dates

### Alertes

1. **TEMOINS_MANQUANTS** : Témoins non spécifiés
2. **DECLARANT_MANQUANT** : Déclarant non spécifié
3. **CAUSE_MANQUANTE** : Cause de décès non spécifiée
4. **DATE_FUTURE** : Date dans le futur

## Exemples d'utilisation

### 1. Enregistrement simple d'un lot

```bash
curl -X POST http://localhost:8080/api/actes-deces/lot \
  -H "Content-Type: application/json" \
  -d @exemple-lot-actes-deces.json
```

### 2. Validation d'un lot

```bash
curl -X POST http://localhost:8080/api/actes-deces/lot/validation \
  -H "Content-Type: application/json" \
  -d @exemple-lot-actes-deces.json
```

### 3. Vérification d'un numéro d'acte

```bash
curl -X GET http://localhost:8080/api/actes-deces/verification/numero/DEC-2024-001
```

### 4. Vérification d'un défunt

```bash
curl -X GET http://localhost:8080/api/actes-deces/verification/defunt/1
```

## Bonnes pratiques

1. **Validation préalable** : Toujours valider un lot avant l'enregistrement
2. **Gestion des erreurs** : Vérifier la réponse pour identifier les échecs
3. **Numérotation** : Utiliser une numérotation cohérente pour les actes
4. **Métadonnées** : Utiliser les champs `numeroOrdre` et `reference` pour le suivi
5. **Taille des lots** : Limiter à 50-100 actes maximum pour de meilleures performances

## Statistiques et rapports

Le système génère automatiquement des statistiques sur :
- Répartition par commune
- Répartition par officier
- Répartition par date
- Nombre d'actes avec/sans témoins
- Nombre d'actes avec/sans cause de décès
- Enregistrements tardifs

## Support et maintenance

Pour toute question ou problème :
1. Vérifier les logs d'erreur
2. Utiliser l'endpoint de validation
3. Consulter la documentation des codes d'erreur
4. Contacter l'équipe technique

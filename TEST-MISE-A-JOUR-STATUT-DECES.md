# Test de Mise à Jour Automatique du Statut lors de la Création d'Acte de Décès

## Objectif
Vérifier que le statut d'une personne est automatiquement mis à jour en "DÉCÉDÉ" lors de la création d'un acte de décès.

## Étapes de Test

### 1. Créer une personne vivante
```bash
POST /personnes
Content-Type: application/json

{
  "nom": "TEST",
  "postnom": "STATUT",
  "prenom": "Jean",
  "sexe": "MASCULIN",
  "dateNaissance": "1990-01-01",
  "lieuNaiss": "Kinshasa"
}
```

**Résultat attendu :** La personne est créée avec le statut "VIVANT"

### 2. Vérifier le statut initial
```bash
GET /personnes/{id}
```

**Résultat attendu :** `"statut": "VIVANT"`

### 3. Créer un acte de décès
```bash
POST /actes-deces
Content-Type: application/json

{
  "numeroActe": "DEC-TEST-001",
  "defuntId": {id_de_la_personne},
  "communeId": 1,
  "dateDeces": "2024-01-15",
  "lieuDeces": "Hôpital Test",
  "officier": "Dr. Test"
}
```

**Résultat attendu :** L'acte de décès est créé avec succès

### 4. Vérifier que le statut a été mis à jour
```bash
GET /personnes/{id}
```

**Résultat attendu :** `"statut": "DECEDE"`

### 5. Supprimer l'acte de décès
```bash
DELETE /actes-deces/{id_acte}
```

**Résultat attendu :** L'acte est supprimé avec succès

### 6. Vérifier que le statut a été restauré
```bash
GET /personnes/{id}
```

**Résultat attendu :** `"statut": "VIVANT"`

## Tests Automatiques

Les tests unitaires et d'intégration ont été créés dans :
- `src/test/kotlin/org/megamind/rdc_etat_civil/unit/ActeDecesServiceTest.kt`
- `src/test/kotlin/org/megamind/rdc_etat_civil/integration/ActeDecesIntegrationTest.kt`

## Exécution des Tests

```bash
# Tests unitaires
./gradlew test --tests ActeDecesServiceTest

# Tests d'intégration
./gradlew test --tests ActeDecesIntegrationTest

# Tous les tests
./gradlew test
```

## Comportement Implémenté

1. **Création d'acte de décès** : Le statut de la personne est automatiquement mis à jour en "DÉCÉDÉ"
2. **Suppression d'acte de décès** : Le statut de la personne est restauré en "VIVANT"
3. **Gestion d'erreurs** : Si la mise à jour du statut échoue, l'opération principale (création/suppression d'acte) continue
4. **Traitement par lot** : Le comportement s'applique aussi aux créations en lot d'actes de décès

## Avantages

- **Cohérence des données** : Le statut de la personne reflète toujours la réalité
- **Statistiques fiables** : Les comptages par statut sont corrects
- **Recherches précises** : Les filtres par statut fonctionnent correctement
- **Maintenance automatique** : Pas besoin de mettre à jour manuellement le statut

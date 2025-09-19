# Guide de génération de PDF pour les actes de naissance

## Vue d'ensemble

Le système permet de générer des PDF officiels pour les actes de naissance en utilisant iText. Ces PDF respectent le format officiel de la République Démocratique du Congo.

## Endpoints disponibles

### 1. Génération par ID d'acte
**GET** `/actes-naissance/{id}/pdf`

Génère un PDF pour un acte spécifique via son ID.

### 2. Génération par numéro d'acte
**GET** `/actes-naissance/numero/{numeroActe}/pdf`

Génère un PDF pour un acte via son numéro d'acte.

### 3. Génération par ID d'enfant
**GET** `/actes-naissance/enfant/{enfantId}/pdf`

Génère un PDF pour l'acte de naissance d'un enfant spécifique.

## Exemples d'utilisation

### Avec cURL

```bash
# Génération par ID d'acte
curl -X GET http://localhost:8080/actes-naissance/123/pdf \
  -H "Accept: application/pdf" \
  --output acte_naissance.pdf

# Génération par numéro d'acte (attention aux slashes encodés)
curl -X GET "http://localhost:8080/actes-naissance/numero/KIN%2F2024%2F001/pdf" \
  -H "Accept: application/pdf" \
  --output acte_naissance_KIN_2024_001.pdf

# Génération par ID d'enfant
curl -X GET http://localhost:8080/actes-naissance/enfant/456/pdf \
  -H "Accept: application/pdf" \
  --output acte_naissance_enfant_456.pdf
```

### Avec JavaScript/Fetch

```javascript
// Génération par ID d'acte
async function downloadPdf(acteId) {
    try {
        const response = await fetch(`/actes-naissance/${acteId}/pdf`);
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `acte_naissance_${acteId}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } else {
            console.error('Erreur lors de la génération du PDF');
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}
```

## Structure du PDF généré

### En-tête
- **République Démocratique du Congo**
- **Informations territoriales** (Province, Entité, Commune)
- **Bureau d'état-civil**
- **Numéro d'acte**
- **Drapeau RDC** (si l'image est disponible)

### Contenu principal
- **Titre** : "ACTE DE NAISSANCE" (en gras et souligné)
- **Date et heure** de l'enregistrement
- **Officier d'état civil**
- **Déclarant** et ses informations
- **Informations de l'enfant** :
  - Nom complet
  - Date et lieu de naissance
  - Sexe
- **Informations des parents** :
  - Père (nom, date de naissance, nationalité, profession)
  - Mère (nom, date de naissance, nationalité, profession)
- **Témoins** (si présents)
- **Clauses légales** de validation

### Signatures
- **Déclarant**
- **Officier de l'État civil**

## Formatage et style

### Police et taille
- **Police principale** : Helvetica, 11pt
- **Titre** : Helvetica, 16pt, gras et souligné
- **En-tête** : Helvetica, 10pt

### Arrière-plan
- **Image de fond** : Filigrane avec opacité réduite (15%)
- **Image** : `/images/background.png` (optionnel)
- **Drapeau** : `/images/flag-rdc.png` (optionnel)

### Mise en page
- **Format** : A4 (210 x 297 mm)
- **Marges** : 50pt sur tous les côtés
- **Alignement** : Justifié pour le texte, centré pour le titre

## Gestion des erreurs

### Erreurs courantes

#### 404 - Acte non trouvé
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Aucun acte trouvé avec le numéro: KIN/2024/999",
  "path": "/actes-naissance/numero/KIN/2024/999/pdf"
}
```

#### 400 - Paramètres invalides
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "L'ID doit être un nombre positif",
  "path": "/actes-naissance/-1/pdf"
}
```

### Codes de statut HTTP
- **200 OK** : PDF généré avec succès
- **400 Bad Request** : Paramètres invalides
- **404 Not Found** : Acte non trouvé
- **500 Internal Server Error** : Erreur de génération PDF

## Optimisations et bonnes pratiques

### Performance
- **Cache** : Les PDF sont générés à la demande (pas de cache)
- **Mémoire** : Utilisation de ByteArrayOutputStream pour éviter les fichiers temporaires
- **Taille** : Les PDF générés sont optimisés pour la taille

### Sécurité
- **Validation** : Tous les paramètres d'entrée sont validés
- **Autorisation** : Respect des règles de sécurité existantes
- **Contenu** : Échappement des caractères spéciaux dans le texte

### Maintenance
- **Images** : Placez les images dans `src/main/resources/images/`
- **Polices** : Utilisation des polices système standard
- **Configuration** : Aucune configuration supplémentaire requise

## Personnalisation

### Ajout d'images
1. Placez vos images dans `src/main/resources/images/`
2. Modifiez le service `ActeNaissancePdfService`
3. Mettez à jour les chemins d'images

### Modification du contenu
1. Éditez la méthode `addActeContent()` dans `ActeNaissancePdfService`
2. Ajustez le formatage selon vos besoins
3. Testez avec différents types d'actes

### Ajout de champs
1. Étendez `ActeNaissanceCompletDto`
2. Mettez à jour la méthode de conversion
3. Modifiez la génération PDF pour inclure les nouveaux champs

## Exemple de réponse HTTP

```
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="acte_naissance_KIN_2024_001.pdf"
Content-Length: 45678

%PDF-1.4
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj
...
```

## Support et dépannage

### Problèmes courants

#### PDF vide ou corrompu
- Vérifiez que l'acte existe dans la base de données
- Vérifiez les permissions sur le répertoire des ressources
- Consultez les logs pour les erreurs de génération

#### Images manquantes
- Vérifiez que les images existent dans `src/main/resources/images/`
- Vérifiez les chemins d'accès aux images
- Le système fonctionne sans images (placeholders)

#### Erreurs de mémoire
- Augmentez la mémoire JVM si nécessaire
- Vérifiez la taille des PDF générés
- Considérez l'implémentation d'un cache si nécessaire

### Logs utiles
```bash
# Activer les logs de debug pour iText
logging.level.org.megamind.rdc_etat_civil.naissance.pdf=DEBUG

# Activer les logs de performance
logging.level.org.springframework.web=DEBUG
```

# Exemples de requêtes pour la génération de PDF

## 1. Génération par ID d'acte

### Requête
```http
GET /actes-naissance/123/pdf
Accept: application/pdf
```

### Réponse
```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="acte_naissance_KIN_2024_001.pdf"
Content-Length: 45678

[Contenu binaire du PDF]
```

### Avec cURL
```bash
curl -X GET http://localhost:8080/actes-naissance/123/pdf \
  -H "Accept: application/pdf" \
  --output acte_naissance.pdf
```

## 2. Génération par numéro d'acte

### Requête
```http
GET /actes-naissance/numero/KIN%2F2024%2F001/pdf
Accept: application/pdf
```

### Réponse
```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="acte_naissance_KIN_2024_001.pdf"
Content-Length: 45678

[Contenu binaire du PDF]
```

### Avec cURL
```bash
# Encodage URL nécessaire pour les slashes
curl -X GET "http://localhost:8080/actes-naissance/numero/KIN%2F2024%2F001/pdf" \
  -H "Accept: application/pdf" \
  --output acte_naissance_KIN_2024_001.pdf
```

## 3. Génération par ID d'enfant

### Requête
```http
GET /actes-naissance/enfant/456/pdf
Accept: application/pdf
```

### Réponse
```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="acte_naissance_enfant_456.pdf"
Content-Length: 45678

[Contenu binaire du PDF]
```

### Avec cURL
```bash
curl -X GET http://localhost:8080/actes-naissance/enfant/456/pdf \
  -H "Accept: application/pdf" \
  --output acte_naissance_enfant_456.pdf
```

## 4. Exemple avec JavaScript

```javascript
// Fonction pour télécharger un PDF
async function downloadActePdf(acteId) {
    try {
        const response = await fetch(`/actes-naissance/${acteId}/pdf`, {
            method: 'GET',
            headers: {
                'Accept': 'application/pdf'
            }
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            
            // Créer un lien de téléchargement
            const a = document.createElement('a');
            a.href = url;
            a.download = `acte_naissance_${acteId}.pdf`;
            
            // Déclencher le téléchargement
            document.body.appendChild(a);
            a.click();
            
            // Nettoyer
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } else {
            console.error('Erreur lors de la génération du PDF:', response.statusText);
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

// Utilisation
downloadActePdf(123);
```

## 5. Exemple avec Postman

### Configuration
1. **Méthode** : GET
2. **URL** : `http://localhost:8080/actes-naissance/123/pdf`
3. **Headers** :
   - `Accept: application/pdf`
4. **Response** :
   - Sauvegarder et télécharger le fichier
   - Vérifier le Content-Type: `application/pdf`

## 6. Exemple avec Python

```python
import requests

def download_acte_pdf(acte_id, base_url="http://localhost:8080"):
    """
    Télécharge le PDF d'un acte de naissance
    """
    url = f"{base_url}/actes-naissance/{acte_id}/pdf"
    headers = {"Accept": "application/pdf"}
    
    try:
        response = requests.get(url, headers=headers)
        
        if response.status_code == 200:
            filename = f"acte_naissance_{acte_id}.pdf"
            with open(filename, 'wb') as f:
                f.write(response.content)
            print(f"PDF sauvegardé: {filename}")
        else:
            print(f"Erreur {response.status_code}: {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"Erreur de requête: {e}")

# Utilisation
download_acte_pdf(123)
```

## 7. Gestion des erreurs

### Erreur 404 - Acte non trouvé
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Aucun acte trouvé avec le numéro: KIN/2024/999",
  "path": "/actes-naissance/numero/KIN/2024/999/pdf"
}
```

### Erreur 400 - Paramètre invalide
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "L'ID doit être un nombre positif",
  "path": "/actes-naissance/-1/pdf"
}
```

### Erreur 500 - Erreur de génération
```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Erreur lors de la génération du PDF",
  "path": "/actes-naissance/123/pdf"
}
```

## 8. Tests d'intégration

Les tests d'intégration vérifient :
- ✅ Génération de PDF par ID d'acte
- ✅ Génération de PDF par numéro d'acte (avec encodage URL)
- ✅ Génération de PDF par ID d'enfant
- ✅ Headers HTTP corrects (Content-Type, Content-Disposition)
- ✅ Gestion des erreurs (404, 400, 500)

## 9. Bonnes pratiques

### Performance
- Les PDF sont générés à la demande (pas de cache)
- Utilisation de ByteArrayOutputStream pour éviter les fichiers temporaires
- Optimisation de la taille des images

### Sécurité
- Validation de tous les paramètres d'entrée
- Respect des règles d'autorisation existantes
- Échappement des caractères spéciaux

### Maintenance
- Images optionnelles (le système fonctionne sans)
- Logs détaillés pour le débogage
- Tests d'intégration complets

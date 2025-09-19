# Images pour la génération de PDF

Ce dossier contient les images utilisées pour la génération des PDF d'actes de naissance.

## Images requises

### 1. Arrière-plan (optionnel)
- **Fichier** : `background.png`
- **Usage** : Filigrane d'arrière-plan pour le PDF
- **Format** : PNG avec transparence
- **Dimensions** : Recommandé 210x297mm (format A4)
- **Opacité** : L'image sera automatiquement rendue à 15% d'opacité

### 2. Drapeau RDC (optionnel)
- **Fichier** : `flag-rdc.png`
- **Usage** : Drapeau de la République Démocratique du Congo dans l'en-tête
- **Format** : PNG
- **Dimensions** : Recommandé 80x60px
- **Position** : En-tête à droite

## Gestion des images manquantes

Si les images ne sont pas présentes :
- Le système continue de fonctionner normalement
- Un placeholder textuel sera affiché à la place du drapeau
- Aucun arrière-plan ne sera appliqué

## Recommandations

### Qualité des images
- Utilisez des images haute résolution pour une meilleure qualité d'impression
- Optimisez les images pour réduire la taille des PDF générés
- Utilisez le format PNG pour les images avec transparence

### Droits d'usage
- Assurez-vous d'avoir les droits d'usage pour toutes les images
- Respectez les droits d'auteur et les marques déposées
- Utilisez des images officielles du gouvernement quand possible

## Exemple de structure

```
src/main/resources/images/
├── background.png      # Arrière-plan du PDF
├── flag-rdc.png       # Drapeau RDC
└── README.md          # Ce fichier
```

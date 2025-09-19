# Guide complet - ActeNaissanceService

Ce guide présente toutes les méthodes disponibles dans `ActeNaissanceService` pour gérer les actes de naissance dans le système d'état civil de la RDC.

## 🛠️ Méthodes CRUD (Create, Read, Update, Delete)

### Créer un acte de naissance

```kotlin
@Autowired
private lateinit var acteNaissanceService: ActeNaissanceService

// Créer un nouvel acte
val nouvelActe = ActeNaissance(
    numeroActe = "KIN/2024/001",
    enfant = enfant, // Personne existante
    commune = commune, // Commune existante
    officier = "Jean KABILA",
    declarant = "Marie TSHALA",
    dateEnregistrement = LocalDate.now(),
    temoin1 = "Pierre MUKENDI",
    temoin2 = "Sophie KASONGO"
)

try {
    val acteEnregistre = acteNaissanceService.enregistrerActeNaissance(nouvelActe)
    println("Acte enregistré avec l'ID: ${acteEnregistre.id}")
} catch (e: IllegalArgumentException) {
    println("Erreur de validation: ${e.message}")
}
```

### Lire un acte

```kotlin
// Par ID - avec gestion d'erreur
try {
    val acte = acteNaissanceService.obtenirActeParId(1L)
    println("Acte trouvé: ${acte.numeroActe}")
} catch (e: NoSuchElementException) {
    println("Acte non trouvé")
}

// Par ID - sans exception
val acte = acteNaissanceService.trouverActeParId(1L)
if (acte != null) {
    println("Acte trouvé: ${acte.numeroActe}")
} else {
    println("Acte non trouvé")
}

// Par numéro d'acte
val acte = acteNaissanceService.rechercherParNumeroActe("KIN/2024/001")
```

### Mettre à jour un acte

```kotlin
// Récupérer l'acte existant
val acteExistant = acteNaissanceService.obtenirActeParId(1L)

// Modifier les données
val acteModifie = acteExistant.copy(
    officier = "Nouveau Officier",
    declarant = "Nouveau Déclarant"
)

try {
    val acteAJour = acteNaissanceService.mettreAJourActeNaissance(1L, acteModifie)
    println("Acte mis à jour avec succès")
} catch (e: IllegalArgumentException) {
    println("Erreur de validation: ${e.message}")
}
```

### Supprimer un acte

```kotlin
try {
    acteNaissanceService.supprimerActeNaissance(1L)
    println("Acte supprimé avec succès")
} catch (e: NoSuchElementException) {
    println("Acte non trouvé")
}
```

## 🔍 Méthodes de recherche

### Recherches de base

```kotlin
val pageable = PageRequest.of(0, 20, Sort.by("dateEnregistrement").descending())

// Tous les actes
val tousLesActes = acteNaissanceService.listerTousLesActes(pageable)

// Par nom d'enfant
val actesParNom = acteNaissanceService.rechercherParNomEnfant("MUKENDI", pageable)

// Par officier
val actesParOfficier = acteNaissanceService.rechercherParOfficier("KABILA", pageable)

// Par déclarant
val actesParDeclarant = acteNaissanceService.rechercherParDeclarant("TSHALA", pageable)
```

### Recherches géographiques

```kotlin
// Par commune (objet)
val commune = communeRepository.findById(1L).get()
val actesCommune = acteNaissanceService.rechercherParCommune(commune, pageable)

// Par nom de commune
val actesKinshasa = acteNaissanceService.rechercherParNomCommune("Gombe", pageable)

// Par province
val actesProvince = acteNaissanceService.obtenirActesParProvinceId(1L, pageable)

// Par entité
val actesEntite = acteNaissanceService.obtenirActesParEntiteId(1L, pageable)

// Par type d'entité (villes ou territoires)
val actesVilles = acteNaissanceService.obtenirActesParTypeEntite(true, pageable) // villes
val actesTerritoires = acteNaissanceService.obtenirActesParTypeEntite(false, pageable) // territoires
```

### Recherches familiales

```kotlin
// Enfants d'un père
val pere = personneRepository.findById(1L).get()
val enfantsDuPere = acteNaissanceService.rechercherParPere(pere, pageable)

// Enfants d'une mère
val mere = personneRepository.findById(2L).get()
val enfantsDeLaMere = acteNaissanceService.rechercherParMere(mere, pageable)

// Enfants d'un couple
val enfantsDuCouple = acteNaissanceService.rechercherParPereEtMere(pere, mere, pageable)
```

### Recherches temporelles

```kotlin
// Par période d'enregistrement
val debut = LocalDate.of(2024, 1, 1)
val fin = LocalDate.of(2024, 12, 31)
val actesPeriode = acteNaissanceService.rechercherParPeriodeEnregistrement(debut, fin, pageable)

// Par période de naissance
val actesNaissance = acteNaissanceService.rechercherParPeriodeNaissanceEnfant(debut, fin, pageable)

// Actes d'aujourd'hui
val actesAujourdhui = acteNaissanceService.rechercherActesAujourdhui(pageable)

// Actes récents (depuis 7 jours)
val depuisDate = LocalDate.now().minusDays(7)
val actesRecents = acteNaissanceService.rechercherActesRecents(depuisDate, pageable)
```

### Recherches spécialisées

```kotlin
// Actes avec témoins
val actesAvecTemoins = acteNaissanceService.rechercherActesAvecTemoins(pageable)

// Actes sans témoins (attention particulière)
val actesSansTemoins = acteNaissanceService.rechercherActesSansTemoins(pageable)

// Actes avec enregistrement tardif (>90 jours)
val actesTardifs = acteNaissanceService.rechercherActesEnregistrementTardif(pageable)
```

## ✅ Méthodes de validation

### Vérifications d'existence

```kotlin
// Vérifier si un numéro d'acte existe
val numeroExiste = acteNaissanceService.numeroActeExiste("KIN/2024/001")
if (numeroExiste) {
    println("Ce numéro d'acte est déjà utilisé")
}

// Vérifier si un enfant a déjà un acte
val enfant = personneRepository.findById(1L).get()
val enfantAActe = acteNaissanceService.enfantADejaUnActe(enfant)
if (enfantAActe) {
    println("Cet enfant a déjà un acte de naissance")
}
```

### Détection de doublons

```kotlin
// Rechercher des doublons potentiels
val doublons = acteNaissanceService.rechercherDoublonsPotentiels(
    nom = "MUKENDI",
    postnom = "TSHALA",
    dateNaissance = LocalDate.of(2024, 1, 15),
    excludeId = 0L // ID à exclure si on modifie un acte existant
)

if (doublons.isNotEmpty()) {
    println("Attention: ${doublons.size} doublons potentiels trouvés")
    doublons.forEach { doublon ->
        println("- Acte ${doublon.numeroActe} pour ${doublon.enfant.nom}")
    }
}
```

## 📊 Méthodes statistiques

### Comptages

```kotlin
// Total d'actes par province
val nombreActesProvince = acteNaissanceService.compterActesParProvince(1L)

// Total d'actes par entité
val nombreActesEntite = acteNaissanceService.compterActesParEntite(1L)

// Total d'actes par type d'entité
val nombreActesVilles = acteNaissanceService.compterActesParTypeEntite(true)
val nombreActesTerritoires = acteNaissanceService.compterActesParTypeEntite(false)
```

### Statistiques détaillées

```kotlin
// Statistiques par province
val statsProvinces = acteNaissanceService.obtenirStatistiquesParProvince()
statsProvinces.forEach { (province, count) ->
    println("$province: $count actes")
}

// Statistiques par entité
val statsEntites = acteNaissanceService.obtenirStatistiquesParEntite()

// Statistiques par type d'entité
val statsTypes = acteNaissanceService.obtenirStatistiquesParTypeEntite()
```

### Résumés complets

```kotlin
// Résumé de toutes les provinces
val resumeProvinces = acteNaissanceService.obtenirResumeProvincies()
resumeProvinces.forEach { (province, count) ->
    println("Province ${province.designation}: $count actes")
}

// Résumé de toutes les entités
val resumeEntites = acteNaissanceService.obtenirResumeEntites()

// Résumé des entités d'une province
val province = provinceRepository.findById(1L).get()
val resumeEntitesProvince = acteNaissanceService.obtenirResumeEntitesDansProvince(province)
```

## 🚨 Gestion d'erreurs

### Types d'exceptions

```kotlin
try {
    // Opération sur un acte
    acteNaissanceService.enregistrerActeNaissance(acte)
} catch (e: IllegalArgumentException) {
    // Erreurs de validation métier
    when {
        e.message?.contains("numéro d'acte") == true -> println("Problème avec le numéro d'acte")
        e.message?.contains("officier") == true -> println("Problème avec l'officier")
        e.message?.contains("date") == true -> println("Problème avec les dates")
        else -> println("Erreur de validation: ${e.message}")
    }
} catch (e: NoSuchElementException) {
    // Entité non trouvée
    println("Élément non trouvé: ${e.message}")
} catch (e: Exception) {
    // Autres erreurs
    println("Erreur inattendue: ${e.message}")
}
```

### Bonnes pratiques

1. **Toujours valider** avant d'enregistrer
2. **Vérifier l'existence** des entités liées
3. **Détecter les doublons** avant création
4. **Utiliser la pagination** pour les grandes listes
5. **Capturer les exceptions** appropriées
6. **Logger les opérations** importantes

## 📝 Exemples complets

### Workflow d'enregistrement complet

```kotlin
fun enregistrerNouvelActe(
    numeroActe: String,
    enfantId: Long,
    communeId: Long,
    officier: String,
    declarant: String?
): Result<ActeNaissance> {
    return try {
        // 1. Vérifier l'unicité du numéro
        if (acteNaissanceService.numeroActeExiste(numeroActe)) {
            return Result.failure(Exception("Numéro d'acte déjà existant"))
        }
        
        // 2. Récupérer l'enfant
        val enfant = personneRepository.findByIdOrNull(enfantId)
            ?: return Result.failure(Exception("Enfant non trouvé"))
        
        // 3. Vérifier que l'enfant n'a pas déjà un acte
        if (acteNaissanceService.enfantADejaUnActe(enfant)) {
            return Result.failure(Exception("L'enfant a déjà un acte de naissance"))
        }
        
        // 4. Récupérer la commune
        val commune = communeRepository.findByIdOrNull(communeId)
            ?: return Result.failure(Exception("Commune non trouvée"))
        
        // 5. Créer l'acte
        val acte = ActeNaissance(
            numeroActe = numeroActe,
            enfant = enfant,
            commune = commune,
            officier = officier,
            declarant = declarant,
            dateEnregistrement = LocalDate.now()
        )
        
        // 6. Enregistrer
        val acteEnregistre = acteNaissanceService.enregistrerActeNaissance(acte)
        Result.success(acteEnregistre)
        
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

Ce service complet offre maintenant toutes les fonctionnalités nécessaires pour gérer efficacement les actes de naissance dans le contexte de la RDC !

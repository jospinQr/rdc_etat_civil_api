# Guide d'utilisation - Récupération des actes de naissance par province

Ce guide explique comment récupérer tous les actes de naissance des enfants d'une province dans votre application RDC État Civil.

## 🗂️ Structure des données

```
Province → Entité → Commune → ActeNaissance
```

- **Province** : Province de la RDC (ex: Kinshasa, Haut-Katanga, etc.)
- **Entité** : Subdivision administrative (ville/territoire)
- **Commune** : Commune ou secteur
- **ActeNaissance** : Acte de naissance enregistré

## 📋 Méthodes disponibles dans ActeNaissanceRepository

### 1. Recherche par Province

```kotlin
// Par objet Province
fun findByProvince(province: Province, pageable: Pageable): Page<ActeNaissance>

// Par ID de province
fun findByProvinceId(provinceId: Long, pageable: Pageable): Page<ActeNaissance>

// Par nom de province (recherche partielle)
fun findByProvinceNomContaining(nomProvince: String, pageable: Pageable): Page<ActeNaissance>
```

### 2. Comptage par Province

```kotlin
// Nombre d'actes par province
fun countByProvince(province: Province): Long
fun countByProvinceId(provinceId: Long): Long

// Statistiques par province
fun statistiquesParProvince(): List<Array<Any>>
```

## 🚀 Exemples d'utilisation

### Via le Service

```kotlin
@Autowired
private lateinit var acteNaissanceService: ActeNaissanceService

// Récupérer tous les actes de Kinshasa (ID = 1)
val pageable = PageRequest.of(0, 20, Sort.by("dateEnregistrement").descending())
val actesKinshasa = acteNaissanceService.obtenirActesParProvinceId(1L, pageable)

// Rechercher par nom de province
val actesHautKatanga = acteNaissanceService.obtenirActesParNomProvince("Haut-Katanga", pageable)

// Compter les actes d'une province
val nombreActes = acteNaissanceService.compterActesParProvince(1L)

// Obtenir les statistiques de toutes les provinces
val statistiques = acteNaissanceService.obtenirStatistiquesParProvince()
```

### Via l'API REST

```bash
# Récupérer les actes de la province ID=1 (page 0, 20 résultats)
GET /api/actes-naissance/province/1?page=0&size=20

# Rechercher par nom de province
GET /api/actes-naissance/province/recherche?nom=Kinshasa&page=0&size=20

# Compter les actes d'une province
GET /api/actes-naissance/province/1/count

# Statistiques par provinces
GET /api/actes-naissance/statistiques/provinces

# Recherche avancée avec filtrage par province
GET /api/actes-naissance/recherche-avancee?province=Kinshasa&nomEnfant=Mukendi

# Résumé de toutes les provinces
GET /api/actes-naissance/resume-provinces
```

## 📊 Exemples de réponses

### Actes par province
```json
{
  "content": [
    {
      "id": 1,
      "numeroActe": "KIN/2024/001",
      "dateEnregistrement": "2024-01-15",
      "officier": "Jean KABILA",
      "enfant": {
        "nom": "MUKENDI",
        "postnom": "TSHIALA",
        "prenom": "Pascal",
        "sexe": "MASCULIN",
        "dateNaissance": "2024-01-10"
      },
      "commune": {
        "id": 1,
        "nom": "Gombe"
      },
      "province": {
        "id": 1,
        "nom": "Kinshasa"
      }
    }
  ],
  "totalElements": 1250,
  "totalPages": 63,
  "number": 0,
  "size": 20
}
```

### Statistiques par province
```json
[
  {
    "province": "Kinshasa",
    "nombreActes": 5420
  },
  {
    "province": "Haut-Katanga",
    "nombreActes": 3210
  },
  {
    "province": "Nord-Kivu",
    "nombreActes": 2890
  }
]
```

## 🎯 Cas d'usage typiques

### 1. Rapport provincial mensuel
```kotlin
fun genererRapportProvincial(provinceId: Long, mois: Int, annee: Int): RapportProvincial {
    val debut = LocalDate.of(annee, mois, 1)
    val fin = debut.withDayOfMonth(debut.lengthOfMonth())
    
    val actes = acteNaissanceRepository.findByProvinceId(provinceId, pageable)
        .filter { it.dateEnregistrement.isAfter(debut) && it.dateEnregistrement.isBefore(fin) }
    
    return RapportProvincial(
        province = provinceRepository.findById(provinceId).get(),
        periode = "$mois/$annee",
        nombreActes = actes.size,
        actes = actes
    )
}
```

### 2. Dashboard administratif
```kotlin
fun obtenirDashboardProvinces(): DashboardProvinces {
    val statistiques = acteNaissanceService.obtenirStatistiquesParProvince()
    val total = statistiques.sumOf { it.second }
    
    return DashboardProvinces(
        totalActesNational = total,
        nombreProvinces = statistiques.size,
        provinceLeader = statistiques.firstOrNull(),
        repartition = statistiques.map { (province, count) ->
            ProvinceStats(province, count, (count * 100.0 / total))
        }
    )
}
```

### 3. Recherche citoyenne
```kotlin
// Rechercher tous les enfants nés dans une province
fun rechercherEnfantsParProvince(
    nomProvince: String,
    criteres: CriteresRecherche,
    pageable: Pageable
): Page<ActeNaissance> {
    return acteNaissanceRepository.findByProvinceNomContaining(nomProvince, pageable)
        .filter { acte ->
            criteres.correspond(acte.enfant)
        }
}
```

## ⚠️ Bonnes pratiques

1. **Pagination obligatoire** : Toujours utiliser la pagination pour éviter les surcharges
2. **Tri par défaut** : Trier par `dateEnregistrement DESC` pour les actes récents
3. **Cache** : Mettre en cache les statistiques par province (mise à jour quotidienne)
4. **Filtrage** : Combiner les recherches par province avec d'autres critères
5. **Performance** : Utiliser les `JOIN FETCH` pour éviter les N+1 queries

## 🔐 Sécurité et accès

- **Niveau provincial** : Accès limité aux agents de la province
- **Niveau national** : Accès complet pour les administrateurs centraux
- **Logs d'audit** : Tracer tous les accès aux données provinciales
- **Anonymisation** : Possibilité d'anonymiser pour les statistiques publiques

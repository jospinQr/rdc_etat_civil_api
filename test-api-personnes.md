# Test API Personnes - RDC État Civil

## 🎯 Exemples de requêtes pour tester l'API

### 1. **Créer le père (Jean-Baptiste MUKAMBA)**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "MUKAMBA",
  "postnom": "MBUYI",
  "prenom": "Jean-Baptiste",
  "sexe": "MASCULIN",
  "dateNaissance": "1975-03-15",
  "heureNaissance": "14:30:00",
  "lieuNaiss": "Lubumbashi",
  "profession": "Enseignant",
  "nationalite": "Congolaise",
  "communeChefferie": "Lubumbashi",
  "quartierGroup": "Katuba",
  "avenueVillage": "Avenue Kasai",
  "celluleLocalite": "Cellule 12",
  "telephone": "+243971234567",
  "email": "jean.mukamba@gmail.com",
  "statut": "VIVANT",
  "situationMatrimoniale": "MARIE"
}
```

### 2. **Créer la mère (Marie-Claire KABAMBA)**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "KABAMBA",
  "postnom": "NGALULA",
  "prenom": "Marie-Claire",
  "sexe": "FEMININ",
  "dateNaissance": "1980-07-22",
  "heureNaissance": "09:15:00",
  "lieuNaiss": "Kinshasa",
  "profession": "Infirmière",
  "nationalite": "Congolaise",
  "communeChefferie": "Lubumbashi",
  "quartierGroup": "Katuba",
  "avenueVillage": "Avenue Kasai",
  "celluleLocalite": "Cellule 12",
  "telephone": "+243982345678",
  "email": "marie.kabamba@yahoo.fr",
  "statut": "VIVANT",
  "situationMatrimoniale": "MARIE"
}
```

### 3. **Créer l'enfant (Grace MUKAMBA) avec parents**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "MUKAMBA",
  "postnom": "MBUYI",
  "prenom": "Grace",
  "sexe": "FEMININ",
  "dateNaissance": "2005-12-10",
  "heureNaissance": "06:45:00",
  "lieuNaiss": "Lubumbashi",
  "profession": "Étudiante",
  "nationalite": "Congolaise",
  "communeChefferie": "Lubumbashi",
  "quartierGroup": "Katuba",
  "avenueVillage": "Avenue Kasai",
  "celluleLocalite": "Cellule 12",
  "telephone": "+243993456789",
  "email": "grace.mukamba@student.unilu.ac.cd",
  "pereId": 1,
  "mereId": 2,
  "statut": "VIVANT",
  "situationMatrimoniale": "CELIBATAIRE"
}
```

### 4. **Créer une personne de Kananga**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "TSHIMANGA",
  "postnom": "KASONGO",
  "prenom": "Pierre",
  "sexe": "MASCULIN",
  "dateNaissance": "1990-05-18",
  "heureNaissance": "11:20:00",
  "lieuNaiss": "Mbuji-Mayi",
  "profession": "Commerçant",
  "nationalite": "Congolaise",
  "communeChefferie": "Kananga",
  "quartierGroup": "Ndesha",
  "avenueVillage": "Avenue Lumumba",
  "celluleLocalite": "Cellule 5",
  "telephone": "+243990123456",
  "email": "pierre.tshimanga@commerce.cd",
  "statut": "VIVANT",
  "situationMatrimoniale": "CELIBATAIRE"
}
```

### 5. **Créer une personne âgée**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "ILUNGA",
  "postnom": "MWEMA",
  "prenom": "Elisabeth",
  "sexe": "FEMININ",
  "dateNaissance": "1955-01-30",
  "heureNaissance": "16:00:00",
  "lieuNaiss": "Kolwezi",
  "profession": "Retraitée",
  "nationalite": "Congolaise",
  "communeChefferie": "Kolwezi",
  "quartierGroup": "Dilala",
  "avenueVillage": "Avenue Mobutu",
  "celluleLocalite": "Cellule 8",
  "telephone": "+243977888999",
  "statut": "VIVANT",
  "situationMatrimoniale": "VEUF"
}
```

### 6. **Créer un mineur**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "MPIANA",
  "postnom": "KATENDE",
  "prenom": "Jonathan",
  "sexe": "MASCULIN",
  "dateNaissance": "2010-08-05",
  "heureNaissance": "08:30:00",
  "lieuNaiss": "Goma",
  "nationalite": "Congolaise",
  "communeChefferie": "Goma",
  "quartierGroup": "Himbi",
  "avenueVillage": "Avenue des Volcans",
  "celluleLocalite": "Cellule 3",
  "statut": "VIVANT",
  "situationMatrimoniale": "CELIBATAIRE"
}
```

### 7. **Créer un médecin de Bukavu**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "KAZADI",
  "postnom": "MULAMBA",
  "prenom": "Joseph",
  "sexe": "MASCULIN",
  "dateNaissance": "1985-11-12",
  "heureNaissance": "13:45:00",
  "lieuNaiss": "Bukavu",
  "profession": "Médecin",
  "nationalite": "Congolaise",
  "communeChefferie": "Bukavu",
  "quartierGroup": "Ibanda",
  "avenueVillage": "Avenue Patrice Lumumba",
  "celluleLocalite": "Cellule 1",
  "telephone": "+243998765432",
  "email": "dr.kazadi@hopital-bukavu.cd",
  "statut": "VIVANT",
  "situationMatrimoniale": "MARIE"
}
```

### 8. **Créer une journaliste de Kinshasa**
```bash
POST http://localhost:8080/api/v1/personnes
Content-Type: application/json

{
  "nom": "NGANDU",
  "postnom": "KALONJI",
  "prenom": "Antoinette",
  "sexe": "FEMININ",
  "dateNaissance": "1992-04-25",
  "heureNaissance": "10:15:00",
  "lieuNaiss": "Kinshasa",
  "profession": "Journaliste",
  "nationalite": "Congolaise",
  "communeChefferie": "Kinshasa",
  "quartierGroup": "Gombe",
  "avenueVillage": "Boulevard du 30 Juin",
  "celluleLocalite": "Cellule Centre",
  "telephone": "+243812345678",
  "email": "antoinette.ngandu@radiookapi.net",
  "statut": "VIVANT",
  "situationMatrimoniale": "CELIBATAIRE"
}
```

## 🔍 **Tests de recherche après création**

### Recherche par nom :
```bash
GET http://localhost:8080/api/v1/personnes/rechercher?terme=MUKAMBA&page=0&size=10
```

### Recherche multicritères :
```bash
GET http://localhost:8080/api/v1/personnes/recherche-multicriteres?sexe=MASCULIN&ageMin=30&ageMax=50&commune=Lubumbashi
```

### Obtenir les enfants d'une personne :
```bash
GET http://localhost:8080/api/v1/personnes/1/enfants
```

### Statistiques :
```bash
GET http://localhost:8080/api/v1/personnes/statistiques/generales
GET http://localhost:8080/api/v1/personnes/statistiques/par-commune
GET http://localhost:8080/api/v1/personnes/statistiques/par-sexe
```

## 🚀 **CRÉATION EN LOT avec PersonneBatchRequest**

### **Une seule requête pour créer toutes les personnes :**
```bash
POST http://localhost:8080/api/v1/personnes/batch
Content-Type: application/json

{
  "personnes": [
    {
      "nom": "MUKAMBA",
      "postnom": "MBUYI",
      "prenom": "Jean-Baptiste",
      "sexe": "MASCULIN",
      "dateNaissance": "1975-03-15",
      "heureNaissance": "14:30:00",
      "lieuNaiss": "Lubumbashi",
      "profession": "Enseignant",
      "nationalite": "Congolaise",
      "communeChefferie": "Lubumbashi",
      "quartierGroup": "Katuba",
      "avenueVillage": "Avenue Kasai",
      "celluleLocalite": "Cellule 12",
      "telephone": "+243971234567",
      "email": "jean.mukamba@gmail.com",
      "statut": "VIVANT",
      "situationMatrimoniale": "MARIE"
    },
    {
      "nom": "KABAMBA",
      "postnom": "NGALULA",
      "prenom": "Marie-Claire",
      "sexe": "FEMININ",
      "dateNaissance": "1980-07-22",
      "heureNaissance": "09:15:00",
      "lieuNaiss": "Kinshasa",
      "profession": "Infirmière",
      "nationalite": "Congolaise",
      "communeChefferie": "Lubumbashi",
      "quartierGroup": "Katuba",
      "avenueVillage": "Avenue Kasai",
      "celluleLocalite": "Cellule 12",
      "telephone": "+243982345678",
      "email": "marie.kabamba@yahoo.fr",
      "statut": "VIVANT",
      "situationMatrimoniale": "MARIE"
    },
    {
      "nom": "TSHIMANGA",
      "postnom": "KASONGO",
      "prenom": "Pierre",
      "sexe": "MASCULIN",
      "dateNaissance": "1990-05-18",
      "heureNaissance": "11:20:00",
      "lieuNaiss": "Mbuji-Mayi",
      "profession": "Commerçant",
      "nationalite": "Congolaise",
      "communeChefferie": "Kananga",
      "quartierGroup": "Ndesha",
      "avenueVillage": "Avenue Lumumba",
      "celluleLocalite": "Cellule 5",
      "telephone": "+243990123456",
      "email": "pierre.tshimanga@commerce.cd",
      "statut": "VIVANT",
      "situationMatrimoniale": "CELIBATAIRE"
    }
  ]
}
```

### **Réponse attendue :**
```json
{
  "totalDemandees": 8,
  "totalCreees": 8,
  "totalEchecs": 0,
  "personnesCreees": [
    {
      "id": 1,
      "nom": "MUKAMBA",
      "postnom": "MBUYI",
      "prenom": "Jean-Baptiste",
      "age": 49,
      ...
    }
  ],
  "echecs": []
}
```

## 📊 **Cas de test couverts**

✅ **Relations familiales** : Père, mère, enfant  
✅ **Différentes provinces** : Lubumbashi, Kinshasa, Kananga, Goma, Bukavu, Kolwezi  
✅ **Tranches d'âge** : Mineur (13 ans), Adultes, Personne âgée (69 ans)  
✅ **Professions variées** : Enseignant, Infirmière, Médecin, Commerçant, Journaliste  
✅ **Situations matrimoniales** : Célibataire, Marié, Veuf  
✅ **Contacts** : Téléphones (+243), Emails  
✅ **Adresses RDC** : Communes, quartiers, avenues typiques  
✅ **API Batch** : Création de plusieurs personnes en une seule requête

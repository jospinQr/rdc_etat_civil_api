package org.megamind.rdc_etat_civil.mariage

import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ActeMariageRepository : JpaRepository<ActeMariage, Long> {

    // ====== RECHERCHES DE BASE ======

    fun findByNumeroActe(numeroActe: String): ActeMariage?

    fun existsByNumeroActe(numeroActe: String): Boolean

    fun findByEpoux(epoux: Personne): List<ActeMariage>

    fun findByEpouse(epouse: Personne): List<ActeMariage>

    fun findByCommune(commune: Commune, pageable: Pageable): Page<ActeMariage>

    fun countByCommune(commune: Commune): Long

    // ====== RECHERCHES PAR TERRITOIRE ======

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
    """)
    fun findByProvinceId(provinceId: Long, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
    """)
    fun countByProvinceId(provinceId: Long): Long

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
    """)
    fun findByEntiteId(entiteId: Long, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
    """)
    fun countByEntiteId(entiteId: Long): Long

    // ====== RECHERCHES PAR SEXE ======

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE a.epoux.sexe = :sexe
    """)
    fun findBySexeEpoux(sexe: Sexe, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE a.epouse.sexe = :sexe
    """)
    fun findBySexeEpouse(sexe: Sexe, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.epoux.sexe = :sexe
    """)
    fun countBySexeEpoux(sexe: Sexe): Long

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.epouse.sexe = :sexe
    """)
    fun countBySexeEpouse(sexe: Sexe): Long

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.epoux.sexe = :sexe AND p.id = :provinceId
    """)
    fun findBySexeEpouxAndProvinceId(sexe: Sexe, provinceId: Long, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.epouse.sexe = :sexe AND p.id = :provinceId
    """)
    fun findBySexeEpouseAndProvinceId(sexe: Sexe, provinceId: Long, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.epoux.sexe = :sexe AND p.id = :provinceId
    """)
    fun countBySexeEpouxAndProvinceId(sexe: Sexe, provinceId: Long): Long

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.epouse.sexe = :sexe AND p.id = :provinceId
    """)
    fun countBySexeEpouseAndProvinceId(sexe: Sexe, provinceId: Long): Long

    // ====== RECHERCHES PAR RÉGIME MATRIMONIAL ======

    fun findByRegimeMatrimonial(regimeMatrimonial: RegimeMatrimonial, pageable: Pageable): Page<ActeMariage>

    fun countByRegimeMatrimonial(regimeMatrimonial: RegimeMatrimonial): Long

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.regimeMatrimonial = :regimeMatrimonial AND p.id = :provinceId
    """)
    fun findByRegimeMatrimonialAndProvinceId(regimeMatrimonial: RegimeMatrimonial, provinceId: Long, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.regimeMatrimonial = :regimeMatrimonial AND p.id = :provinceId
    """)
    fun countByRegimeMatrimonialAndProvinceId(regimeMatrimonial: RegimeMatrimonial, provinceId: Long): Long

    // ====== RECHERCHES PAR NOM ======

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE LOWER(a.epoux.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epoux.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epoux.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epouse.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epouse.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epouse.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
    """)
    fun rechercherParNomEpouxOuEpouse(terme: String, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE LOWER(a.epoux.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epoux.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epoux.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
    """)
    fun rechercherParNomEpoux(terme: String, pageable: Pageable): Page<ActeMariage>

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE LOWER(a.epouse.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epouse.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.epouse.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
    """)
    fun rechercherParNomEpouse(terme: String, pageable: Pageable): Page<ActeMariage>

    // ====== RECHERCHES PAR PÉRIODE ======

    fun findByDateMariageBetween(dateDebut: LocalDate, dateFin: LocalDate, pageable: Pageable): Page<ActeMariage>

    fun countByDateMariageBetween(dateDebut: LocalDate, dateFin: LocalDate): Long

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        AND a.dateMariage >= :dateDebut AND a.dateMariage <= :dateFin
    """)
    fun findByProvinceIdAndDateMariageBetween(
        provinceId: Long, 
        dateDebut: LocalDate, 
        dateFin: LocalDate, 
        pageable: Pageable
    ): Page<ActeMariage>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        AND a.dateMariage >= :dateDebut AND a.dateMariage <= :dateFin
    """)
    fun countByProvinceIdAndDateMariageBetween(provinceId: Long, dateDebut: LocalDate, dateFin: LocalDate): Long

    // ====== MÉTHODES SPÉCIFIQUES RDC - NIVEAU PAYS ======

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a
    """)
    fun countTotalActesMariagePays(): Long

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.dateMariage >= :dateDebut AND a.dateMariage <= :dateFin
    """)
    fun countActesMariagePaysParPeriode(dateDebut: LocalDate, dateFin: LocalDate): Long

    @Query("""
        SELECT p.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        GROUP BY p.id, p.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariagePaysParProvince(): List<Array<Any>>

    @Query("""
        SELECT e.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        GROUP BY e.id, e.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariagePaysParEntite(): List<Array<Any>>

    @Query("""
        SELECT c.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        GROUP BY c.id, c.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariagePaysParCommune(): List<Array<Any>>

    @Query("""
        SELECT a.regimeMatrimonial, COUNT(a) 
        FROM ActeMariage a 
        GROUP BY a.regimeMatrimonial
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariagePaysParRegime(): List<Array<Any>>

    // ====== MÉTHODES SPÉCIFIQUES RDC - NIVEAU PROVINCE ======

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        AND (:dateDebut IS NULL OR a.dateMariage >= :dateDebut)
        AND (:dateFin IS NULL OR a.dateMariage <= :dateFin)
    """)
    fun findByProvinceIdAvecPeriode(
        provinceId: Long, 
        dateDebut: LocalDate?, 
        dateFin: LocalDate?, 
        pageable: Pageable
    ): Page<ActeMariage>

    @Query("""
        SELECT e.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        GROUP BY e.id, e.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageProvinceParEntite(provinceId: Long): List<Array<Any>>

    @Query("""
        SELECT c.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        GROUP BY c.id, c.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageProvinceParCommune(provinceId: Long): List<Array<Any>>

    @Query("""
        SELECT a.regimeMatrimonial, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        GROUP BY a.regimeMatrimonial
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageProvinceParRegime(provinceId: Long): List<Array<Any>>

    @Query("""
        SELECT a.epoux.sexe, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        GROUP BY a.epoux.sexe
    """)
    fun statistiquesMariageProvinceParSexeEpoux(provinceId: Long): List<Array<Any>>

    @Query("""
        SELECT a.epouse.sexe, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        GROUP BY a.epouse.sexe
    """)
    fun statistiquesMariageProvinceParSexeEpouse(provinceId: Long): List<Array<Any>>

    // ====== MÉTHODES SPÉCIFIQUES RDC - NIVEAU ENTITÉ/VILLE ======

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
        AND (:dateDebut IS NULL OR a.dateMariage >= :dateDebut)
        AND (:dateFin IS NULL OR a.dateMariage <= :dateFin)
    """)
    fun findByEntiteIdAvecPeriode(
        entiteId: Long, 
        dateDebut: LocalDate?, 
        dateFin: LocalDate?, 
        pageable: Pageable
    ): Page<ActeMariage>

    @Query("""
        SELECT c.designation, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
        GROUP BY c.id, c.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageEntiteParCommune(entiteId: Long): List<Array<Any>>

    @Query("""
        SELECT a.regimeMatrimonial, COUNT(a) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
        GROUP BY a.regimeMatrimonial
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageEntiteParRegime(entiteId: Long): List<Array<Any>>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
        AND a.dateMariage >= :dateDebut AND a.dateMariage <= :dateFin
    """)
    fun countActesMariageEntiteParPeriode(entiteId: Long, dateDebut: LocalDate, dateFin: LocalDate): Long

    // ====== MÉTHODES SPÉCIFIQUES RDC - NIVEAU COMMUNE ======

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE a.commune.id = :communeId
        AND (:dateDebut IS NULL OR a.dateMariage >= :dateDebut)
        AND (:dateFin IS NULL OR a.dateMariage <= :dateFin)
    """)
    fun findByCommuneIdAvecPeriode(
        communeId: Long, 
        dateDebut: LocalDate?, 
        dateFin: LocalDate?, 
        pageable: Pageable
    ): Page<ActeMariage>

    @Query("""
        SELECT a.regimeMatrimonial, COUNT(a) 
        FROM ActeMariage a 
        WHERE a.commune.id = :communeId
        GROUP BY a.regimeMatrimonial
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageCommuneParRegime(communeId: Long): List<Array<Any>>

    @Query("""
        SELECT a.officier, COUNT(a) 
        FROM ActeMariage a 
        WHERE a.commune.id = :communeId
        GROUP BY a.officier 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesMariageCommuneParOfficier(communeId: Long): List<Array<Any>>

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.commune.id = :communeId
        AND a.dateMariage >= :dateDebut AND a.dateMariage <= :dateFin
    """)
    fun countActesMariageCommuneParPeriode(communeId: Long, dateDebut: LocalDate, dateFin: LocalDate): Long

    // ====== MÉTHODES DE RECHERCHE AVANCÉE RDC ======

    @Query("""
        SELECT a FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE (:provinceId IS NULL OR p.id = :provinceId)
        AND (:entiteId IS NULL OR e.id = :entiteId)
        AND (:communeId IS NULL OR c.id = :communeId)
        AND (:sexeEpoux IS NULL OR a.epoux.sexe = :sexeEpoux)
        AND (:sexeEpouse IS NULL OR a.epouse.sexe = :sexeEpouse)
        AND (:regimeMatrimonial IS NULL OR a.regimeMatrimonial = :regimeMatrimonial)
        AND (:terme IS NULL OR LOWER(a.epoux.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
            OR LOWER(a.epoux.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
            OR LOWER(a.epoux.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
            OR LOWER(a.epouse.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
            OR LOWER(a.epouse.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
            OR LOWER(a.epouse.prenom) LIKE LOWER(CONCAT('%', :terme, '%')))
        AND (:dateDebut IS NULL OR a.dateMariage >= :dateDebut)
        AND (:dateFin IS NULL OR a.dateMariage <= :dateFin)
    """)
    fun rechercheAvanceeMariageRDC(
        @Param("provinceId") provinceId: Long?,
        @Param("entiteId") entiteId: Long?,
        @Param("communeId") communeId: Long?,
        @Param("sexeEpoux") sexeEpoux: Sexe?,
        @Param("sexeEpouse") sexeEpouse: Sexe?,
        @Param("regimeMatrimonial") regimeMatrimonial: RegimeMatrimonial?,
        @Param("terme") terme: String?,
        @Param("dateDebut") dateDebut: LocalDate?,
        @Param("dateFin") dateFin: LocalDate?,
        pageable: Pageable
    ): Page<ActeMariage>

    // ====== MÉTHODES DE STATISTIQUES TEMPORELLES RDC ======

    @Query("""
        SELECT YEAR(a.dateMariage), MONTH(a.dateMariage), COUNT(a)
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
        AND a.dateMariage >= :dateDebut
        GROUP BY YEAR(a.dateMariage), MONTH(a.dateMariage)
        ORDER BY YEAR(a.dateMariage), MONTH(a.dateMariage)
    """)
    fun statistiquesMariageProvinceParMois(provinceId: Long, dateDebut: LocalDate): List<Array<Any>>

    @Query("""
        SELECT YEAR(a.dateMariage), MONTH(a.dateMariage), COUNT(a)
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
        AND a.dateMariage >= :dateDebut
        GROUP BY YEAR(a.dateMariage), MONTH(a.dateMariage)
        ORDER BY YEAR(a.dateMariage), MONTH(a.dateMariage)
    """)
    fun statistiquesMariageEntiteParMois(entiteId: Long, dateDebut: LocalDate): List<Array<Any>>

    @Query("""
        SELECT YEAR(a.dateMariage), MONTH(a.dateMariage), COUNT(a)
        FROM ActeMariage a 
        WHERE a.commune.id = :communeId
        AND a.dateMariage >= :dateDebut
        GROUP BY YEAR(a.dateMariage), MONTH(a.dateMariage)
        ORDER BY YEAR(a.dateMariage), MONTH(a.dateMariage)
    """)
    fun statistiquesMariageCommuneParMois(communeId: Long, dateDebut: LocalDate): List<Array<Any>>

    // ====== MÉTHODES DE VÉRIFICATION D'EXISTENCE RDC ======

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
    """)
    fun existeActesMariagePourProvince(provinceId: Long): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
    """)
    fun existeActesMariagePourEntite(entiteId: Long): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM ActeMariage a 
        WHERE a.commune.id = :communeId
    """)
    fun existeActesMariagePourCommune(communeId: Long): Boolean

    // ====== MÉTHODES DE VÉRIFICATION DE MARIAGE ======

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM ActeMariage a 
        WHERE a.epoux.id = :personneId OR a.epouse.id = :personneId
    """)
    fun estPersonneMariee(personneId: Long): Boolean

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE a.epoux.id = :personneId OR a.epouse.id = :personneId
    """)
    fun trouverMariagesParPersonne(personneId: Long): List<ActeMariage>

    @Query("""
        SELECT a FROM ActeMariage a 
        WHERE a.epoux.id = :personneId OR a.epouse.id = :personneId
        ORDER BY a.dateMariage DESC
    """)
    fun trouverDernierMariageParPersonne(personneId: Long): ActeMariage?

    // ====== MÉTHODES DE STATISTIQUES SPÉCIALISÉES ======

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.temoin1 IS NOT NULL AND a.temoin1 != ''
        OR a.temoin2 IS NOT NULL AND a.temoin2 != ''
    """)
    fun countMariagesAvecTemoins(): Long

    @Query("""
        SELECT COUNT(a) FROM ActeMariage a 
        WHERE a.temoin1 IS NULL OR a.temoin1 = ''
        AND a.temoin2 IS NULL OR a.temoin2 = ''
    """)
    fun countMariagesSansTemoins(): Long

    @Query("""
        SELECT AVG(YEAR(a.dateMariage) - YEAR(a.epoux.dateNaissance)) 
        FROM ActeMariage a 
        WHERE a.epoux.dateNaissance IS NOT NULL
    """)
    fun moyenneAgeEpouxAuMariage(): Double?

    @Query("""
        SELECT AVG(YEAR(a.dateMariage) - YEAR(a.epouse.dateNaissance)) 
        FROM ActeMariage a 
        WHERE a.epouse.dateNaissance IS NOT NULL
    """)
    fun moyenneAgeEpouseAuMariage(): Double?

    @Query("""
        SELECT AVG(YEAR(a.dateMariage) - YEAR(a.epoux.dateNaissance)) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId AND a.epoux.dateNaissance IS NOT NULL
    """)
    fun moyenneAgeEpouxAuMariageParProvince(provinceId: Long): Double?

    @Query("""
        SELECT AVG(YEAR(a.dateMariage) - YEAR(a.epouse.dateNaissance)) 
        FROM ActeMariage a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId AND a.epouse.dateNaissance IS NOT NULL
    """)
    fun moyenneAgeEpouseAuMariageParProvince(provinceId: Long): Double?
}






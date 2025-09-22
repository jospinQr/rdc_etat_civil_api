package org.megamind.rdc_etat_civil.deces

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
interface ActeDecesRepository : JpaRepository<ActeDeces, Long> {

    // ====== RECHERCHES DE BASE ======

    fun findByNumeroActe(numeroActe: String): ActeDeces?

    fun existsByNumeroActe(numeroActe: String): Boolean

    fun findByDefunt(defunt: Personne): ActeDeces?

    fun existsByDefunt(defunt: Personne): Boolean

    fun findByCommune(commune: Commune, pageable: Pageable): Page<ActeDeces>

    fun countByCommune(commune: Commune): Long

    // ====== RECHERCHES PAR TERRITOIRE ======

    @Query("""
        SELECT a FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
    """)
    fun findByProvinceId(provinceId: Long, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE p.id = :provinceId
    """)
    fun countByProvinceId(provinceId: Long): Long

    @Query("""
        SELECT a FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
    """)
    fun findByEntiteId(entiteId: Long, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE e.id = :entiteId
    """)
    fun countByEntiteId(entiteId: Long): Long

    // ====== RECHERCHES PAR SEXE ======

    @Query("""
        SELECT a FROM ActeDeces a 
        WHERE a.defunt.sexe = :sexe
    """)
    fun findBySexeDefunt(sexe: Sexe, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        WHERE a.defunt.sexe = :sexe
    """)
    fun countBySexeDefunt(sexe: Sexe): Long

    @Query("""
        SELECT a FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.defunt.sexe = :sexe AND p.id = :provinceId
    """)
    fun findBySexeDefuntAndProvinceId(sexe: Sexe, provinceId: Long, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        JOIN e.province p 
        WHERE a.defunt.sexe = :sexe AND p.id = :provinceId
    """)
    fun countBySexeDefuntAndProvinceId(sexe: Sexe, provinceId: Long): Long

    @Query("""
        SELECT a FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE a.defunt.sexe = :sexe AND e.id = :entiteId
    """)
    fun findBySexeDefuntAndEntiteId(sexe: Sexe, entiteId: Long, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        JOIN a.commune c 
        JOIN c.entite e 
        WHERE a.defunt.sexe = :sexe AND e.id = :entiteId
    """)
    fun countBySexeDefuntAndEntiteId(sexe: Sexe, entiteId: Long): Long

    @Query("""
        SELECT a FROM ActeDeces a 
        WHERE a.defunt.sexe = :sexe AND a.commune.id = :communeId
    """)
    fun findBySexeDefuntAndCommuneId(sexe: Sexe, communeId: Long, pageable: Pageable): Page<ActeDeces>

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        WHERE a.defunt.sexe = :sexe AND a.commune.id = :communeId
    """)
    fun countBySexeDefuntAndCommuneId(sexe: Sexe, communeId: Long): Long

    // ====== RECHERCHES PAR NOM ======

    @Query("""
        SELECT a FROM ActeDeces a 
        WHERE LOWER(a.defunt.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.defunt.postnom) LIKE LOWER(CONCAT('%', :terme, '%'))
        OR LOWER(a.defunt.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
    """)
    fun rechercherParNomDefunt(terme: String, pageable: Pageable): Page<ActeDeces>

    // ====== RECHERCHES MULTICRITÈRES ======

    @Query("""
        SELECT a FROM ActeDeces a 
        WHERE (:numeroActe IS NULL OR LOWER(a.numeroActe) LIKE LOWER(CONCAT('%', :numeroActe, '%')))
        AND (:nomDefunt IS NULL OR LOWER(a.defunt.nom) LIKE LOWER(CONCAT('%', :nomDefunt, '%')))
        AND (:postnomDefunt IS NULL OR LOWER(a.defunt.postnom) LIKE LOWER(CONCAT('%', :postnomDefunt, '%')))
        AND (:prenomDefunt IS NULL OR LOWER(a.defunt.prenom) LIKE LOWER(CONCAT('%', :prenomDefunt, '%')))
        AND (:officier IS NULL OR LOWER(a.officier) LIKE LOWER(CONCAT('%', :officier, '%')))
        AND (:declarant IS NULL OR LOWER(a.declarant) LIKE LOWER(CONCAT('%', :declarant, '%')))
        AND (:medecin IS NULL OR LOWER(a.medecin) LIKE LOWER(CONCAT('%', :medecin, '%')))
        AND (:dateDebutDeces IS NULL OR a.dateDeces >= :dateDebutDeces)
        AND (:dateFinDeces IS NULL OR a.dateDeces <= :dateFinDeces)
        AND (:dateDebutEnreg IS NULL OR a.dateEnregistrement >= :dateDebutEnreg)
        AND (:dateFinEnreg IS NULL OR a.dateEnregistrement <= :dateFinEnreg)
    """)
    fun rechercheMulticriteres(
        @Param("numeroActe") numeroActe: String?,
        @Param("nomDefunt") nomDefunt: String?,
        @Param("postnomDefunt") postnomDefunt: String?,
        @Param("prenomDefunt") prenomDefunt: String?,
        @Param("officier") officier: String?,
        @Param("declarant") declarant: String?,
        @Param("medecin") medecin: String?,
        @Param("dateDebutDeces") dateDebutDeces: LocalDate?,
        @Param("dateFinDeces") dateFinDeces: LocalDate?,
        @Param("dateDebutEnreg") dateDebutEnreg: LocalDate?,
        @Param("dateFinEnreg") dateFinEnreg: LocalDate?,
        pageable: Pageable
    ): Page<ActeDeces>

    // ====== STATISTIQUES ======

    fun countByDateEnregistrement(date: LocalDate): Long

    fun countByDateEnregistrementBetween(dateDebut: LocalDate, dateFin: LocalDate): Long

    @Query("""
        SELECT COUNT(a) FROM ActeDeces a 
        WHERE a.dateEnregistrement > a.dateDeces
    """)
    fun countActesEnregistrementTardif(): Long

    @Query("""
        SELECT c.designation, COUNT(a) 
        FROM ActeDeces a 
        JOIN a.commune c 
        GROUP BY c.designation 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParCommune(): List<Array<Any>>

    @Query("""
        SELECT a.officier, COUNT(a) 
        FROM ActeDeces a 
        GROUP BY a.officier 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParOfficier(): List<Array<Any>>

    @Query("""
        SELECT YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement), COUNT(a)
        FROM ActeDeces a 
        WHERE a.dateEnregistrement >= :dateDebut
        GROUP BY YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement)
        ORDER BY YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement)
    """)
    fun statistiquesParMoisEnregistrement(dateDebut: LocalDate): List<Array<Any>>

    @Query("""
        SELECT a.causeDeces, COUNT(a) 
        FROM ActeDeces a 
        WHERE a.causeDeces IS NOT NULL 
        GROUP BY a.causeDeces 
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParCauseDeces(): List<Array<Any>>

    @Query("""
        SELECT a.defunt.sexe, COUNT(a) 
        FROM ActeDeces a 
        GROUP BY a.defunt.sexe
    """)
    fun statistiquesParSexe(): List<Array<Any>>

    @Query("""
        SELECT AVG(YEAR(a.dateDeces) - YEAR(a.defunt.dateNaissance)) 
        FROM ActeDeces a 
        WHERE a.defunt.dateNaissance IS NOT NULL
    """)
    fun moyenneAgeAuDeces(): Double?

    @Query("""
        SELECT COUNT(a) 
        FROM ActeDeces a 
        WHERE a.causeDeces IS NOT NULL AND a.causeDeces != ''
    """)
    fun countActesAvecCause(): Long

    @Query("""
        SELECT COUNT(a) 
        FROM ActeDeces a 
        WHERE a.medecin IS NOT NULL AND a.medecin != ''
    """)
    fun countActesAvecMedecin(): Long
}
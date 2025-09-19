package org.megamind.rdc_etat_civil.naissance

import org.megamind.rdc_etat_civil.personne.Personne
import org.megamind.rdc_etat_civil.personne.Sexe
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ActeNaissanceRepository : JpaRepository<ActeNaissance, Long> {

    // ====== RECHERCHES ESSENTIELLES AVEC PAGINATION ======

    /**
     * Recherche par numéro d'acte (unique)
     */
    fun findByNumeroActe(numeroActe: String): ActeNaissance?

    /**
     * Vérification d'existence d'un numéro d'acte
     */
    fun existsByNumeroActe(numeroActe: String): Boolean

    /**
     * Recherche par enfant concerné
     */
    fun findByEnfant(enfant: Personne): ActeNaissance?

    /**
     * Vérification d'existence d'un acte pour un enfant donné
     */
    fun existsByEnfant(enfant: Personne): Boolean

    /**
     * Recherche multicritères avec pagination (la plus importante pour l'administration)
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        JOIN FETCH a.commune c
        WHERE (:numeroActe IS NULL OR UPPER(a.numeroActe) LIKE UPPER(CONCAT('%', :numeroActe, '%')))
        AND (:nomEnfant IS NULL OR UPPER(e.nom) LIKE UPPER(CONCAT('%', :nomEnfant, '%')))
        AND (:postnomEnfant IS NULL OR UPPER(e.postnom) LIKE UPPER(CONCAT('%', :postnomEnfant, '%')))
        AND (:prenomEnfant IS NULL OR (e.prenom IS NOT NULL AND UPPER(e.prenom) LIKE UPPER(CONCAT('%', :prenomEnfant, '%'))))
        AND (:communeNom IS NULL OR UPPER(c.designation) LIKE UPPER(CONCAT('%', :communeNom, '%')))
        AND (:officier IS NULL OR UPPER(a.officier) LIKE UPPER(CONCAT('%', :officier, '%')))
        AND (:declarant IS NULL OR UPPER(a.declarant) LIKE UPPER(CONCAT('%', :declarant, '%')))
        AND (:dateDebutEnreg IS NULL OR a.dateEnregistrement >= :dateDebutEnreg)
        AND (:dateFinEnreg IS NULL OR a.dateEnregistrement <= :dateFinEnreg)
        AND (:dateDebutNaiss IS NULL OR e.dateNaissance >= :dateDebutNaiss)
        AND (:dateFinNaiss IS NULL OR e.dateNaissance <= :dateFinNaiss)
        ORDER BY a.dateEnregistrement DESC, a.numeroActe
    """)
    fun rechercheMulticriteres(
        @Param("numeroActe") numeroActe: String?,
        @Param("nomEnfant") nomEnfant: String?,
        @Param("postnomEnfant") postnomEnfant: String?,
        @Param("prenomEnfant") prenomEnfant: String?,
        @Param("communeNom") communeNom: String?,
        @Param("officier") officier: String?,
        @Param("declarant") declarant: String?,
        @Param("dateDebutEnreg") dateDebutEnreg: LocalDate?,
        @Param("dateFinEnreg") dateFinEnreg: LocalDate?,
        @Param("dateDebutNaiss") dateDebutNaiss: LocalDate?,
        @Param("dateFinNaiss") dateFinNaiss: LocalDate?,
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Recherche par nom de l'enfant avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE UPPER(e.nom) LIKE UPPER(CONCAT('%', :terme, '%')) 
        OR UPPER(e.postnom) LIKE UPPER(CONCAT('%', :terme, '%')) 
        OR (e.prenom IS NOT NULL AND UPPER(e.prenom) LIKE UPPER(CONCAT('%', :terme, '%')))
        ORDER BY e.nom, e.postnom, e.prenom
    """)
    fun rechercherParNomEnfant(@Param("terme") terme: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par commune avec pagination
     */
    fun findByCommune(commune: Commune, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par commune (nom) avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.commune c
        WHERE UPPER(c.designation) LIKE UPPER(CONCAT('%', :nomCommune, '%'))
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByCommuneNomContaining(@Param("nomCommune") nomCommune: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par province avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        JOIN e.province p
        WHERE p = :province
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByProvince(@Param("province") province: Province, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par province (ID) avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        JOIN e.province p
        WHERE p.id = :provinceId
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByProvinceId(@Param("provinceId") provinceId: Long, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par province (nom) avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        JOIN e.province p
        WHERE UPPER(p.designation) LIKE UPPER(CONCAT('%', :nomProvince, '%'))
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByProvinceNomContaining(@Param("nomProvince") nomProvince: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par entité avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        WHERE e = :entite
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByEntite(@Param("entite") entite: Entite, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par entité (ID) avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.commune c
        JOIN FETCH c.entite e
        WHERE e.id = :entiteId
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByEntiteId(@Param("entiteId") entiteId: Long, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par entité (nom) avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.commune c
        JOIN FETCH c.entite e
        WHERE UPPER(e.designation) LIKE UPPER(CONCAT('%', :nomEntite, '%'))
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByEntiteNomContaining(@Param("nomEntite") nomEntite: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par entité dans une province spécifique
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.commune c
        JOIN FETCH c.entite e
        JOIN FETCH e.province p
        WHERE e = :entite AND p = :province
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByEntiteAndProvince(
        @Param("entite") entite: Entite,
        @Param("province") province: Province,
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Recherche par type d'entité (ville ou territoire)
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.commune c
        JOIN FETCH c.entite e
        WHERE e.estVille = :estVille
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findByTypeEntite(@Param("estVille") estVille: Boolean, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par officier d'état civil avec pagination
     */
    fun findByOfficierContainingIgnoreCase(officier: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par déclarant avec pagination
     */
    fun findByDeclarantContainingIgnoreCase(declarant: String, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche par période d'enregistrement avec pagination
     */
    fun findByDateEnregistrementBetween(
        dateDebut: LocalDate,
        dateFin: LocalDate,
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Recherche par période de naissance de l'enfant avec pagination
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.dateNaissance BETWEEN :dateDebut AND :dateFin
        ORDER BY e.dateNaissance DESC
    """)
    fun findByPeriodeNaissanceEnfant(
        @Param("dateDebut") dateDebut: LocalDate,
        @Param("dateFin") dateFin: LocalDate,
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Actes enregistrés aujourd'hui
     */
    fun findByDateEnregistrement(date: LocalDate, pageable: Pageable): Page<ActeNaissance>

    /**
     * Actes enregistrés dans la semaine courante
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        WHERE a.dateEnregistrement >= :dateDebut
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findActesRecents(@Param("dateDebut") dateDebut: LocalDate, pageable: Pageable): Page<ActeNaissance>

    // ====== RECHERCHES PAR SEXE DES ENFANTS ======

    /**
     * Recherche des actes de naissance par sexe de l'enfant
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.sexe = :sexe
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findBySexeEnfant(@Param("sexe") sexe: Sexe, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche des actes par sexe dans une province spécifique
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        JOIN FETCH a.commune c
        JOIN FETCH c.entite ent
        JOIN FETCH ent.province p
        WHERE e.sexe = :sexe AND p.id = :provinceId
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findBySexeEnfantAndProvinceId(
        @Param("sexe") sexe: Sexe, 
        @Param("provinceId") provinceId: Long, 
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Recherche des actes par sexe dans une entité spécifique
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        JOIN FETCH a.commune c
        JOIN FETCH c.entite ent
        WHERE e.sexe = :sexe AND ent.id = :entiteId
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findBySexeEnfantAndEntiteId(
        @Param("sexe") sexe: Sexe, 
        @Param("entiteId") entiteId: Long, 
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Recherche des actes par sexe dans une commune spécifique
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        JOIN FETCH a.commune c
        WHERE e.sexe = :sexe AND c.id = :communeId
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findBySexeEnfantAndCommuneId(
        @Param("sexe") sexe: Sexe, 
        @Param("communeId") communeId: Long, 
        pageable: Pageable
    ): Page<ActeNaissance>

    /**
     * Compter les actes par sexe de l'enfant
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.enfant e
        WHERE e.sexe = :sexe
    """)
    fun countBySexeEnfant(@Param("sexe") sexe: Sexe): Long

    /**
     * Compter les actes par sexe dans une province
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.enfant e
        JOIN a.commune c
        JOIN c.entite ent
        JOIN ent.province p
        WHERE e.sexe = :sexe AND p.id = :provinceId
    """)
    fun countBySexeEnfantAndProvinceId(@Param("sexe") sexe: Sexe, @Param("provinceId") provinceId: Long): Long

    /**
     * Compter les actes par sexe dans une entité
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.enfant e
        JOIN a.commune c
        JOIN c.entite ent
        WHERE e.sexe = :sexe AND ent.id = :entiteId
    """)
    fun countBySexeEnfantAndEntiteId(@Param("sexe") sexe: Sexe, @Param("entiteId") entiteId: Long): Long

    /**
     * Compter les actes par sexe dans une commune
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.enfant e
        JOIN a.commune c
        WHERE e.sexe = :sexe AND c.id = :communeId
    """)
    fun countBySexeEnfantAndCommuneId(@Param("sexe") sexe: Sexe, @Param("communeId") communeId: Long): Long

    // ====== RECHERCHES POUR ENFANTS DE PARENTS SPÉCIFIQUES ======

    /**
     * Recherche d'actes d'enfants d'un père donné
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.pere = :pere
        ORDER BY e.dateNaissance DESC
    """)
    fun findByPere(@Param("pere") pere: Personne, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche d'actes d'enfants d'une mère donnée
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.mere = :mere
        ORDER BY e.dateNaissance DESC
    """)
    fun findByMere(@Param("mere") mere: Personne, pageable: Pageable): Page<ActeNaissance>

    /**
     * Recherche d'actes d'enfants d'un couple donné
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.pere = :pere AND e.mere = :mere
        ORDER BY e.dateNaissance DESC
    """)
    fun findByPereAndMere(
        @Param("pere") pere: Personne,
        @Param("mere") mere: Personne,
        pageable: Pageable
    ): Page<ActeNaissance>

    // ====== COMPTAGES ET STATISTIQUES ======

    /**
     * Nombre total d'actes de naissance
     */
    override fun count(): Long

    /**
     * Nombre d'actes par commune
     */
    fun countByCommune(commune: Commune): Long

    /**
     * Nombre d'actes par province
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        WHERE e.province = :province
    """)
    fun countByProvince(@Param("province") province: Province): Long

    /**
     * Nombre d'actes par province (ID)
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        WHERE e.province.id = :provinceId
    """)
    fun countByProvinceId(@Param("provinceId") provinceId: Long): Long

    /**
     * Nombre d'actes par entité
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.commune c
        WHERE c.entite = :entite
    """)
    fun countByEntite(@Param("entite") entite: Entite): Long

    /**
     * Nombre d'actes par entité (ID)
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.commune c
        WHERE c.entite.id = :entiteId
    """)
    fun countByEntiteId(@Param("entiteId") entiteId: Long): Long

    /**
     * Nombre d'actes par type d'entité (villes vs territoires)
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        WHERE e.estVille = :estVille
    """)
    fun countByTypeEntite(@Param("estVille") estVille: Boolean): Long

    /**
     * Nombre d'actes par officier
     */
    fun countByOfficier(officier: String): Long

    /**
     * Nombre d'actes enregistrés dans une période
     */
    fun countByDateEnregistrementBetween(dateDebut: LocalDate, dateFin: LocalDate): Long

    /**
     * Nombre d'actes pour des naissances dans une période
     */
    @Query("""
        SELECT COUNT(a) FROM ActeNaissance a
        JOIN a.enfant e
        WHERE e.dateNaissance BETWEEN :dateDebut AND :dateFin
    """)
    fun countByPeriodeNaissanceEnfant(
        @Param("dateDebut") dateDebut: LocalDate,
        @Param("dateFin") dateFin: LocalDate
    ): Long

    /**
     * Statistiques par commune
     */
    @Query("""
        SELECT c.designation, COUNT(a) 
        FROM ActeNaissance a
        JOIN a.commune c
        GROUP BY c.designation
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParCommune(): List<Array<Any>>

    /**
     * Statistiques par province
     */
    @Query("""
        SELECT p.designation, COUNT(a) 
        FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        JOIN e.province p
        GROUP BY p.designation
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParProvince(): List<Array<Any>>

    /**
     * Statistiques par entité
     */
    @Query("""
        SELECT e.designation, COUNT(a) 
        FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        GROUP BY e.designation
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParEntite(): List<Array<Any>>

    /**
     * Statistiques par entité dans une province spécifique
     */
    @Query("""
        SELECT e.designation, COUNT(a) 
        FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        WHERE e.province = :province
        GROUP BY e.designation
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParEntiteDansProvince(@Param("province") province: Province): List<Array<Any>>

    /**
     * Statistiques par type d'entité (villes vs territoires)
     */
    @Query("""
        SELECT 
            CASE WHEN e.estVille = true THEN 'Ville' ELSE 'Territoire' END,
            COUNT(a) 
        FROM ActeNaissance a
        JOIN a.commune c
        JOIN c.entite e
        GROUP BY e.estVille
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParTypeEntite(): List<Array<Any>>

    /**
     * Statistiques par officier d'état civil
     */
    @Query("""
        SELECT a.officier, COUNT(a) 
        FROM ActeNaissance a
        GROUP BY a.officier
        ORDER BY COUNT(a) DESC
    """)
    fun statistiquesParOfficier(): List<Array<Any>>

    /**
     * Statistiques par mois d'enregistrement
     */
    @Query("""
        SELECT YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement), COUNT(a)
        FROM ActeNaissance a
        WHERE a.dateEnregistrement >= :dateDebut
        GROUP BY YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement)
        ORDER BY YEAR(a.dateEnregistrement) DESC, MONTH(a.dateEnregistrement) DESC
    """)
    fun statistiquesParMoisEnregistrement(@Param("dateDebut") dateDebut: LocalDate): List<Array<Any>>

    /**
     * Statistiques par mois de naissance
     */
    @Query("""
        SELECT YEAR(e.dateNaissance), MONTH(e.dateNaissance), COUNT(a)
        FROM ActeNaissance a
        JOIN a.enfant e
        WHERE e.dateNaissance >= :dateDebut
        GROUP BY YEAR(e.dateNaissance), MONTH(e.dateNaissance)
        ORDER BY YEAR(e.dateNaissance) DESC, MONTH(e.dateNaissance) DESC
    """)
    fun statistiquesParMoisNaissance(@Param("dateDebut") dateDebut: LocalDate): List<Array<Any>>

    /**
     * Actes enregistrés aujourd'hui (comptage)
     */
    fun countByDateEnregistrement(date: LocalDate): Long

    /**
     * Actes en retard (enregistrement tardif - plus de 90 jours après la naissance)
     */
    @Query(value = """
        SELECT COUNT(*) FROM actes_naissance a
        JOIN personnes e ON a.enfant_id = e.id
        WHERE a.date_enregistrement > DATE_ADD(e.date_naissance, INTERVAL 90 DAY)
    """, nativeQuery = true)
    fun countActesEnregistrementTardif(): Long

    /**
     * Liste des actes en retard avec pagination
     */
    @Query(value = """
        SELECT a.* FROM actes_naissance a
        JOIN personnes e ON a.enfant_id = e.id
        WHERE a.date_enregistrement > DATE_ADD(e.date_naissance, INTERVAL 90 DAY)
        ORDER BY DATEDIFF(a.date_enregistrement, e.date_naissance) DESC
    """, nativeQuery = true)
    fun findActesEnregistrementTardif(pageable: Pageable): Page<ActeNaissance>

    // ====== VALIDATIONS SPÉCIFIQUES AU CONTEXTE RDC ======

    /**
     * Recherche d'actes potentiellement dupliqués (même enfant, données similaires)
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        JOIN FETCH a.enfant e
        WHERE e.nom = :nom 
        AND e.postnom = :postnom 
        AND e.dateNaissance = :dateNaissance
        AND a.id != :excludeId
    """)
    fun findPotentielDoublon(
        @Param("nom") nom: String,
        @Param("postnom") postnom: String,
        @Param("dateNaissance") dateNaissance: LocalDate,
        @Param("excludeId") excludeId: Long
    ): List<ActeNaissance>

    /**
     * Vérification de cohérence : enfant ayant déjà un acte de naissance
     */
    @Query("""
        SELECT COUNT(a) > 1 FROM ActeNaissance a
        WHERE a.enfant.id = :enfantId
    """)
    fun enfantADejaUnActe(@Param("enfantId") enfantId: Long): Boolean

    /**
     * Actes avec témoins pour validation communautaire
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        WHERE a.temoin1 IS NOT NULL OR a.temoin2 IS NOT NULL
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findActesAvecTemoins(pageable: Pageable): Page<ActeNaissance>

    /**
     * Actes sans témoins (nécessitant attention particulière)
     */
    @Query("""
        SELECT a FROM ActeNaissance a
        WHERE a.temoin1 IS NULL AND a.temoin2 IS NULL
        ORDER BY a.dateEnregistrement DESC
    """)
    fun findActesSansTemoin(pageable: Pageable): Page<ActeNaissance>
}

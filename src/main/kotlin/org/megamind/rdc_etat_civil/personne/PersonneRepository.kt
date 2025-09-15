package org.megamind.rdc_etat_civil.personne

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PersonneRepository : JpaRepository<Personne, Long> {

    // ====== RECHERCHES ESSENTIELLES AVEC PAGINATION ======

    /**
     * Recherche globale par nom, postnom ou prénom avec pagination
     */
    @Query("""
        SELECT p FROM Personne p 
        WHERE UPPER(p.nom) LIKE UPPER(CONCAT('%', :terme, '%')) 
        OR UPPER(p.postnom) LIKE UPPER(CONCAT('%', :terme, '%')) 
        OR UPPER(p.prenom) LIKE UPPER(CONCAT('%', :terme, '%'))
        ORDER BY p.nom, p.postnom, p.prenom
    """)
    fun rechercherParNom(@Param("terme") terme: String, pageable: Pageable): Page<Personne>

    /**
     * Recherche multicritères avec pagination (la plus importante)
     */
    @Query("""
        SELECT p FROM Personne p 
        WHERE (:nom IS NULL OR UPPER(p.nom) LIKE UPPER(CONCAT('%', :nom, '%')))
        AND (:postnom IS NULL OR UPPER(p.postnom) LIKE UPPER(CONCAT('%', :postnom, '%')))
        AND (:prenom IS NULL OR UPPER(p.prenom) LIKE UPPER(CONCAT('%', :prenom, '%')))
        AND (:sexe IS NULL OR p.sexe = :sexe)
        AND (:statut IS NULL OR p.statut = :statut)
        AND (:commune IS NULL OR UPPER(p.communeChefferie) LIKE UPPER(CONCAT('%', :commune, '%')))
        AND (:dateDebut IS NULL OR p.dateNaissance >= :dateDebut)
        AND (:dateFin IS NULL OR p.dateNaissance <= :dateFin)
        ORDER BY p.nom, p.postnom, p.prenom
    """)
    fun rechercheMulticriteres(
        @Param("nom") nom: String?,
        @Param("postnom") postnom: String?,
        @Param("prenom") prenom: String?,
        @Param("sexe") sexe: Sexe?,
        @Param("statut") statut: StatutPersonne?,
        @Param("commune") commune: String?,
        @Param("dateDebut") dateDebut: LocalDate?,
        @Param("dateFin") dateFin: LocalDate?,
        pageable: Pageable
    ): Page<Personne>

    /**
     * Lister toutes les personnes avec pagination
     */
    override fun findAll(pageable: Pageable): Page<Personne>

    /**
     * Recherche par statut avec pagination
     */
    fun findByStatut(statut: StatutPersonne, pageable: Pageable): Page<Personne>

    /**
     * Recherche par commune avec pagination
     */
    fun findByCommuneChefferieContainingIgnoreCase(commune: String, pageable: Pageable): Page<Personne>

    // ====== VÉRIFICATIONS ET VALIDATIONS ======

    /**
     * Vérification d'existence pour éviter les doublons
     */
    fun existsByNomAndPostnomAndPrenomAndDateNaissance(
        nom: String, 
        postnom: String, 
        prenom: String, 
        dateNaissance: LocalDate
    ): Boolean

    /**
     * Recherche par nom exact et date de naissance (pour éviter les doublons)
     */
    fun findByNomAndPostnomAndPrenomAndDateNaissance(
        nom: String, 
        postnom: String, 
        prenom: String, 
        dateNaissance: LocalDate
    ): Personne?

    // ====== RELATIONS FAMILIALES ESSENTIELLES ======

    /**
     * Recherche des enfants avec pagination
     */
    fun findByPere(pere: Personne, pageable: Pageable): Page<Personne>
    fun findByMere(mere: Personne, pageable: Pageable): Page<Personne>

    // ====== COMPTAGES POUR STATISTIQUES ======

    /**
     * Nombre total de personnes
     */
    override fun count(): Long

    /**
     * Nombre de personnes par sexe
     */
    fun countBySexe(sexe: Sexe): Long

    /**
     * Nombre de personnes par statut
     */
    fun countByStatut(statut: StatutPersonne): Long

    /**
     * Nombre de personnes par situation matrimoniale
     */
    fun countBySituationMatrimoniale(situation: SituationMatrimoniale): Long

    /**
     * Nombre de personnes par commune
     */
    fun countByCommuneChefferieContainingIgnoreCase(commune: String): Long

    /**
     * Nombre de personnes nées dans une période
     */
    fun countByDateNaissanceBetween(dateDebut: LocalDate, dateFin: LocalDate): Long

    /**
     * Statistiques démographiques rapides
     */
    @Query("""
        SELECT p.sexe, COUNT(p) 
        FROM Personne p 
        WHERE p.statut = org.megamind.rdc_etat_civil.personne.StatutPersonne.VIVANT
        GROUP BY p.sexe
    """)
    fun statistiquesParSexe(): List<Array<Any>>

    @Query("""
        SELECT p.communeChefferie, COUNT(p) 
        FROM Personne p 
        WHERE p.communeChefferie IS NOT NULL 
        GROUP BY p.communeChefferie 
        ORDER BY COUNT(p) DESC
    """)
    fun statistiquesParCommune(): List<Array<Any>>

    /**
     * Nombre de mineurs (moins de 18 ans)
     */
    @Query("""
        SELECT COUNT(p) FROM Personne p 
        WHERE p.dateNaissance > :dateLimite 
        AND p.statut = org.megamind.rdc_etat_civil.personne.StatutPersonne.VIVANT
    """)
    fun countMineurs(@Param("dateLimite") dateLimite: LocalDate): Long

    /**
     * Nombre de majeurs (18 ans et plus)
     */
    @Query("""
        SELECT COUNT(p) FROM Personne p 
        WHERE p.dateNaissance <= :dateLimite 
        AND p.statut = org.megamind.rdc_etat_civil.personne.StatutPersonne.VIVANT
    """)
    fun countMajeurs(@Param("dateLimite") dateLimite: LocalDate): Long
}

package org.megamind.rdc_etat_civil.statistique

import jakarta.persistence.EntityManager
import org.megamind.rdc_etat_civil.personne.Sexe
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class StatistiqueCustomRepository(private val entityManager: EntityManager) {

    private fun buildFiltres(provinceId: Long?, entiteId: Long?, communeId: Long?): String {
        val conditions = mutableListOf<String>()
        if (provinceId != null) conditions.add("p.id = :provinceId")
        if (entiteId != null) conditions.add("ent.id = :entiteId")
        if (communeId != null) conditions.add("c.id = :communeId")
        
        return if (conditions.isNotEmpty()) " AND " + conditions.joinToString(" AND ") else ""
    }

    private fun bindParams(query: jakarta.persistence.Query, provinceId: Long?, entiteId: Long?, communeId: Long?) {
        if (provinceId != null) query.setParameter("provinceId", provinceId)
        if (entiteId != null) query.setParameter("entiteId", entiteId)
        if (communeId != null) query.setParameter("communeId", communeId)
    }

    fun getGlobalStats(debut: LocalDate, fin: LocalDate, provinceId: Long? = null, entiteId: Long? = null, communeId: Long? = null): StatistiqueDto {
        val filtres = buildFiltres(provinceId, entiteId, communeId)
        val joinClause = if (provinceId != null || entiteId != null || communeId != null) " JOIN a.commune c JOIN c.entite ent JOIN ent.province p" else ""

        // Naissances
        val nq = """
            SELECT COUNT(a.id), 
                   SUM(CASE WHEN e.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN e.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeNaissance a JOIN a.enfant e $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
        """.trimIndent()
        
        val qN = entityManager.createQuery(nq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qN, provinceId, entiteId, communeId)
        
        val rowN = qN.singleResult as Array<*>
        val totalN = (rowN[0] as? Number)?.toLong() ?: 0L
        val garcons = (rowN[1] as? Number)?.toLong() ?: 0L
        val filles = (rowN[2] as? Number)?.toLong() ?: 0L

        // Mariages
        val mq = """
            SELECT COUNT(a.id)
            FROM ActeMariage a $joinClause
            WHERE a.dateMariage BETWEEN :debut AND :fin $filtres
        """.trimIndent()
        
        val qM = entityManager.createQuery(mq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
        bindParams(qM, provinceId, entiteId, communeId)
        
        val totalM = (qM.singleResult as? Number)?.toLong() ?: 0L

        // Deces
        val dq = """
            SELECT COUNT(a.id), 
                   SUM(CASE WHEN d.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN d.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeDeces a JOIN a.defunt d $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
        """.trimIndent()
        
        val qD = entityManager.createQuery(dq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qD, provinceId, entiteId, communeId)
            
        val rowD = qD.singleResult as Array<*>
        val totalD = (rowD[0] as? Number)?.toLong() ?: 0L
        val hommes = (rowD[1] as? Number)?.toLong() ?: 0L
        val femmes = (rowD[2] as? Number)?.toLong() ?: 0L
        
        return StatistiqueDto(
            nombreTotalNaissance = totalN,
            naissancesGarcons = garcons,
            naissancesFilles = filles,
            nombreTotalMariage = totalM,
            nombreTotalDeces = totalD,
            decesHommes = hommes,
            decesFemmes = femmes
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatsGrouped(debut: LocalDate, fin: LocalDate, niveau: NiveauGroupement, provinceId: Long? = null, entiteId: Long? = null, communeId: Long? = null): List<StatistiqueRegionDto> {
        val regionSelect = when(niveau) {
            NiveauGroupement.PROVINCE -> "p.designation"
            NiveauGroupement.ENTITE -> "ent.designation"
            NiveauGroupement.COMMUNE -> "c.designation"
        }
        
        val joinClause = " JOIN a.commune c JOIN c.entite ent JOIN ent.province p"
        val filtres = buildFiltres(provinceId, entiteId, communeId)

        // Naissances groupées
        val nq = """
            SELECT $regionSelect, COUNT(a.id), 
                   SUM(CASE WHEN e.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN e.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeNaissance a JOIN a.enfant e $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
            GROUP BY $regionSelect
        """.trimIndent()
        
        val qN = entityManager.createQuery(nq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qN, provinceId, entiteId, communeId)
        val listN = qN.resultList as List<Array<*>>
            
        // Mariages groupées
        val mq = """
            SELECT $regionSelect, COUNT(a.id)
            FROM ActeMariage a $joinClause
            WHERE a.dateMariage BETWEEN :debut AND :fin $filtres
            GROUP BY $regionSelect
        """.trimIndent()
        
        val qM = entityManager.createQuery(mq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
        bindParams(qM, provinceId, entiteId, communeId)
        val listM = qM.resultList as List<Array<*>>
            
        // Deces groupées
        val dq = """
            SELECT $regionSelect, COUNT(a.id), 
                   SUM(CASE WHEN d.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN d.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeDeces a JOIN a.defunt d $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
            GROUP BY $regionSelect
        """.trimIndent()
        
        val qD = entityManager.createQuery(dq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qD, provinceId, entiteId, communeId)
        val listD = qD.resultList as List<Array<*>>
            
        // Agrégation par région
        val map = mutableMapOf<String, StatistiqueDto>()
        
        for (row in listN) {
            val reg = row[0] as String
            val tN = (row[1] as? Number)?.toLong() ?: 0L
            val g = (row[2] as? Number)?.toLong() ?: 0L
            val f = (row[3] as? Number)?.toLong() ?: 0L
            map[reg] = StatistiqueDto(
                nombreTotalNaissance = tN,
                naissancesGarcons = g,
                naissancesFilles = f
            )
        }
        
        for (row in listM) {
            val reg = row[0] as String
            val tM = (row[1] as? Number)?.toLong() ?: 0L
            val s = map.getOrDefault(reg, StatistiqueDto())
            map[reg] = s.copy(nombreTotalMariage = tM)
        }
        
        for (row in listD) {
            val reg = row[0] as String
            val tD = (row[1] as? Number)?.toLong() ?: 0L
            val h = (row[2] as? Number)?.toLong() ?: 0L
            val f = (row[3] as? Number)?.toLong() ?: 0L
            val s = map.getOrDefault(reg, StatistiqueDto())
            map[reg] = s.copy(
                nombreTotalDeces = tD,
                decesHommes = h,
                decesFemmes = f
            )
        }
        
        return map.map { (region, stats) -> StatistiqueRegionDto(region, stats) }.sortedBy { it.nomRegion }
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatsEvolution(debut: LocalDate, fin: LocalDate, provinceId: Long? = null, entiteId: Long? = null, communeId: Long? = null): List<StatistiqueEvolutionDto> {
        val filtres = buildFiltres(provinceId, entiteId, communeId)
        val joinClause = if (provinceId != null || entiteId != null || communeId != null) " JOIN a.commune c JOIN c.entite ent JOIN ent.province p" else ""

        val nq = """
            SELECT YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement), COUNT(a.id), 
                   SUM(CASE WHEN e.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN e.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeNaissance a JOIN a.enfant e $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
            GROUP BY YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement)
        """.trimIndent()
        
        val qN = entityManager.createQuery(nq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qN, provinceId, entiteId, communeId)
        val listN = qN.resultList as List<Array<*>>

        val mq = """
            SELECT YEAR(a.dateMariage), MONTH(a.dateMariage), COUNT(a.id)
            FROM ActeMariage a $joinClause
            WHERE a.dateMariage BETWEEN :debut AND :fin $filtres
            GROUP BY YEAR(a.dateMariage), MONTH(a.dateMariage)
        """.trimIndent()
        
        val qM = entityManager.createQuery(mq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
        bindParams(qM, provinceId, entiteId, communeId)
        val listM = qM.resultList as List<Array<*>>

        val dq = """
            SELECT YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement), COUNT(a.id), 
                   SUM(CASE WHEN d.sexe = :sexeM THEN 1 ELSE 0 END), 
                   SUM(CASE WHEN d.sexe = :sexeF THEN 1 ELSE 0 END)
            FROM ActeDeces a JOIN a.defunt d $joinClause
            WHERE a.dateEnregistrement BETWEEN :debut AND :fin $filtres
            GROUP BY YEAR(a.dateEnregistrement), MONTH(a.dateEnregistrement)
        """.trimIndent()

        val qD = entityManager.createQuery(dq)
            .setParameter("debut", debut)
            .setParameter("fin", fin)
            .setParameter("sexeM", Sexe.MASCULIN)
            .setParameter("sexeF", Sexe.FEMININ)
        bindParams(qD, provinceId, entiteId, communeId)
        val listD = qD.resultList as List<Array<*>>

        val map = mutableMapOf<String, StatistiqueDto>()
        
        fun formatPeriode(year: Number?, month: Number?): String {
            val y = year?.toInt() ?: 0
            val m = month?.toInt() ?: 0
            return String.format("%04d-%02d", y, m)
        }

        for (row in listN) {
            val periode = formatPeriode(row[0] as? Number, row[1] as? Number)
            val tN = (row[2] as? Number)?.toLong() ?: 0L
            val g = (row[3] as? Number)?.toLong() ?: 0L
            val f = (row[4] as? Number)?.toLong() ?: 0L
            map[periode] = StatistiqueDto(
                nombreTotalNaissance = tN,
                naissancesGarcons = g,
                naissancesFilles = f
            )
        }
        
        for (row in listM) {
            val periode = formatPeriode(row[0] as? Number, row[1] as? Number)
            val tM = (row[2] as? Number)?.toLong() ?: 0L
            val s = map.getOrDefault(periode, StatistiqueDto())
            map[periode] = s.copy(nombreTotalMariage = tM)
        }
        
        for (row in listD) {
            val periode = formatPeriode(row[0] as? Number, row[1] as? Number)
            val tD = (row[2] as? Number)?.toLong() ?: 0L
            val h = (row[3] as? Number)?.toLong() ?: 0L
            val f = (row[4] as? Number)?.toLong() ?: 0L
            val s = map.getOrDefault(periode, StatistiqueDto())
            map[periode] = s.copy(
                nombreTotalDeces = tD,
                decesHommes = h,
                decesFemmes = f
            )
        }
        
        return map.map { (periode, stats) -> StatistiqueEvolutionDto(periode, stats) }.sortedBy { it.periode }
    }
}

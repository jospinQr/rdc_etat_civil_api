package org.megamind.rdc_etat_civil.naissance.pdf

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.megamind.rdc_etat_civil.naissance.dto.*
import org.megamind.rdc_etat_civil.personne.Sexe
import java.time.LocalDate
import java.time.LocalTime

@Tag("unit")
class ActeNaissancePdfServiceTest {

    private val pdfService = ActeNaissancePdfService()

    @Test
    fun `generateActeNaissancePdf - retourne un PDF non vide`() {
        val acte = ActeNaissanceCompletDto(
            id = 1L,
            numeroActe = "AN-2026-0001",
            dateEnregistrement = LocalDate.now(),
            officier = "OEC MUKENDI",
            declarant = "DECLARANT",
            temoin1 = "TEMOIN 1",
            temoin2 = "TEMOIN 2",
            enfant = EnfantCompletDto(
                id = 10L,
                nom = "KABAMBA",
                postnom = "MULUMBA",
                prenom = "JEAN",
                sexe = Sexe.MASCULIN,
                dateNaissance = LocalDate.of(2026, 3, 10),
                heureNaissance = LocalTime.of(6, 15),
                lieuNaissance = "KINSHASA",
                pere = ParentCompletDto(
                    id = 20L,
                    nom = "PERE",
                    postnom = "KABAMBA",
                    prenom = "PAUL",
                    profession = "INGENIEUR",
                    nationalite = "Congolaise",
                    dateNaissance = LocalDate.of(1990, 1, 1),
                    lieuNaissance = "KINSHASA"
                ),
                mere = ParentCompletDto(
                    id = 21L,
                    nom = "MERE",
                    postnom = "MULUMBA",
                    prenom = "ANNE",
                    profession = "COMMERÇANTE",
                    nationalite = "Congolaise",
                    dateNaissance = LocalDate.of(1992, 2, 2),
                    lieuNaissance = "KINSHASA"
                )
            ),
            commune = CommuneInfo(id = 1L, nom = "GOMBE"),
            entite = EntiteInfo(id = 1L, nom = "GOMBE", estVille = true),
            province = ProvinceInfo(id = 1L, nom = "KINSHASA")
        )

        val bytes = pdfService.generateActeNaissancePdf(acte)

        assertNotNull(bytes)
        assertTrue(bytes.size > 100)
        // Signature PDF: "%PDF"
        val header = bytes.take(4).toByteArray().toString(Charsets.ISO_8859_1)
        assertEquals("%PDF", header)
    }
}


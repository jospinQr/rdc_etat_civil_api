package org.megamind.rdc_etat_civil.naissance.pdf

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import org.megamind.rdc_etat_civil.naissance.dto.ActeNaissanceCompletDto
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

/**
 * Service pour la génération de PDF des actes de naissance
 */
@Service
class ActeNaissancePdfService {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        private val FRENCH_MONTHS = mapOf(
            1 to "janvier", 2 to "février", 3 to "mars", 4 to "avril",
            5 to "mai", 6 to "juin", 7 to "juillet", 8 to "août",
            9 to "septembre", 10 to "octobre", 11 to "novembre", 12 to "décembre"
        )
    }

    /**
     * Génère un PDF pour un acte de naissance
     */
    fun generateActeNaissancePdf(acteData: ActeNaissanceCompletDto): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 50f, 50f, 50f, 50f)

        try {
            val writer = PdfWriter.getInstance(document, outputStream)

            // Ajouter un gestionnaire d'événements pour l'arrière-plan
            writer.pageEvent = object : PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    try {
                        addBackgroundImage(writer, document)
                    } catch (e: Exception) {
                        // Log l'erreur mais continue sans arrière-plan
                        println("Erreur lors de l'ajout de l'arrière-plan: ${e.message}")
                    }
                }
            }

            document.open()
            
            // En-tête avec informations territoriales et drapeau
            addHeader(document, acteData)
            
            // Titre principal
            addTitle(document)
            
            // Contenu principal de l'acte
            addActeContent(document, acteData)
            
            // Signatures
            addSignatures(document)

        } catch (e: Exception) {
            throw RuntimeException("Erreur lors de la génération du PDF", e)
        } finally {
            document.close()
        }

        return outputStream.toByteArray()
    }

    private fun addBackgroundImage(writer: PdfWriter, document: Document) {
        val bgPath = this::class.java.getResource("/images/background.png")?.toString()
        if (bgPath != null) {
            val bgImage = Image.getInstance(bgPath)
            bgImage.scaleAbsolute(document.pageSize)
            bgImage.setAbsolutePosition(0f, 0f)
            
            val canvas = writer.directContentUnder
            val gs1 = PdfGState()
            gs1.setFillOpacity(0.15f)
            
            canvas.saveState()
            canvas.setGState(gs1)
            canvas.addImage(bgImage)
            canvas.restoreState()
        }
    }

    private fun addHeader(document: Document, acteData: ActeNaissanceCompletDto) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(70f, 30f))

        // Cellule texte à gauche
        val leftCell = PdfPCell()
        leftCell.border = Rectangle.NO_BORDER

        
        leftCell.addElement(createHeaderParagraph("REPUBLIQUE DEMOCRATIQUE DU CONGO"))
        leftCell.addElement(createHeaderParagraph("Province de ${acteData.province.nom.uppercase()}"))
        leftCell.addElement(createHeaderParagraph("Entité de ${acteData.entite.nom.uppercase()}"))
        leftCell.addElement(createHeaderParagraph("Commune de ${acteData.commune.nom.uppercase()}"))
        leftCell.addElement(createHeaderParagraph("Bureau d'état-civil"))
        leftCell.addElement(createHeaderParagraph("Acte no ${acteData.numeroActe}"))
        
        table.addCell(leftCell)

        // Cellule image à droite (drapeau)
        val rightCell = PdfPCell()
        rightCell.border = Rectangle.NO_BORDER
        rightCell.horizontalAlignment = Element.ALIGN_RIGHT
        rightCell.verticalAlignment = Element.ALIGN_TOP
        
        try {
            val flagPath = this::class.java.getResource("/images/flag-rdc.png")?.toString()
            if (flagPath != null) {
                val flag = Image.getInstance(flagPath)
                flag.scaleToFit(80f, 60f)
                rightCell.addElement(flag)
            }
        } catch (e: Exception) {
            // Si l'image n'existe pas, ajouter un placeholder
            rightCell.addElement(Paragraph("DRAPEAU RDC"))
        }
        
        table.addCell(rightCell)
        document.add(table)
        document.add(Chunk.NEWLINE)
    }

    private fun createHeaderParagraph(text: String): Paragraph {
        val font = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
        val paragraph = Paragraph(text, font)
        paragraph.spacingAfter = 2f
        return paragraph
    }

    private fun addTitle(document: Document) {
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD or Font.UNDERLINE)
        val titleParagraph = Paragraph("ACTE DE NAISSANCE", titleFont)
        titleParagraph.alignment = Element.ALIGN_CENTER
        titleParagraph.spacingAfter = 20f
        document.add(titleParagraph)
    }

    private fun addActeContent(document: Document, acteData: ActeNaissanceCompletDto) {
        val contentFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
        
        // Date et heure de l'acte
        val dateActe = formatDateForActe(acteData.dateEnregistrement)
        document.add(Paragraph("L'an ${dateActe.year} le ${dateActe.day} jour du mois de ${dateActe.month}", contentFont))
        document.add(Paragraph("à ${dateActe.hour} heures ${dateActe.minute}", contentFont))
        
        // Officier d'état civil
        document.add(Paragraph("Par devant nous ${acteData.officier.uppercase()}", contentFont))
        document.add(Paragraph("Officier de l'État civil de ${acteData.commune.nom.uppercase()}", contentFont))
        
        // Déclarant
        document.add(Paragraph("A comparu ${acteData.declarant?.uppercase() ?: "NON SPÉCIFIÉ"}", contentFont))
        document.add(Paragraph("en qualité de déclarant", contentFont))
        
        // Informations sur l'enfant
        document.add(Paragraph("Lequel (laquelle) nous a déclaré ce qui suit:", contentFont))
        document.add(Paragraph("Le ${formatDateForActe(acteData.enfant.dateNaissance).day} jour du mois de ${formatDateForActe(acteData.enfant.dateNaissance).month} de l'année ${formatDateForActe(acteData.enfant.dateNaissance).year}", contentFont))
        
        val sexeText = if (acteData.enfant.sexe.name == "MASCULIN") "un garçon" else "une fille"
        document.add(Paragraph("est né(e) à ${acteData.enfant.lieuNaissance?.uppercase() ?: "NON SPÉCIFIÉ"} $sexeText", contentFont))
        
        document.add(Paragraph("nommé(e) ${acteData.enfant.prenom?.uppercase()} ${acteData.enfant.nom.uppercase()} ${acteData.enfant.postnom.uppercase()}", contentFont))
        
        // Parents
        if (acteData.enfant.pere != null) {
            document.add(Paragraph("fils/fille de ${acteData.enfant.pere.prenom?.uppercase()} ${acteData.enfant.pere.nom.uppercase()} ${acteData.enfant.pere.postnom.uppercase()}", contentFont))
            document.add(Paragraph("né à ${acteData.enfant.pere.lieuNaissance?.uppercase() ?: "NON SPÉCIFIÉ"}", contentFont))
            document.add(Paragraph("le ${formatDateForActe(acteData.enfant.pere.dateNaissance)}", contentFont))
            document.add(Paragraph("nationalité ${acteData.enfant.pere.nationalite?.uppercase() ?: "CONGOLAISE"}", contentFont))
            document.add(Paragraph("profession ${acteData.enfant.pere.profession?.uppercase() ?: "NON SPÉCIFIÉE"}", contentFont))
        }
        
        if (acteData.enfant.mere != null) {
            document.add(Paragraph("et de ${acteData.enfant.mere.prenom?.uppercase()} ${acteData.enfant.mere.nom.uppercase()} ${acteData.enfant.mere.postnom.uppercase()}", contentFont))
            document.add(Paragraph("née à ${acteData.enfant.mere.lieuNaissance?.uppercase() ?: "NON SPÉCIFIÉ"}", contentFont))
            document.add(Paragraph("le ${formatDateForActe(acteData.enfant.mere.dateNaissance)}", contentFont))
            document.add(Paragraph("nationalité ${acteData.enfant.mere.nationalite?.uppercase() ?: "CONGOLAISE"}", contentFont))
            document.add(Paragraph("profession ${acteData.enfant.mere.profession?.uppercase() ?: "NON SPÉCIFIÉE"}", contentFont))
        }
        
        document.add(Paragraph("conjoints", contentFont))
        
        // Témoins
        if (acteData.temoin1 != null || acteData.temoin2 != null) {
            document.add(Chunk.NEWLINE)
            document.add(Paragraph("En présence des témoins:", contentFont))
            acteData.temoin1?.let { 
                document.add(Paragraph("- ${it.uppercase()}", contentFont))
            }
            acteData.temoin2?.let { 
                document.add(Paragraph("- ${it.uppercase()}", contentFont))
            }
        }
        
        // Fin de l'acte
        document.add(Chunk.NEWLINE)
        document.add(Paragraph("Lecture de l'acte a été faite et connaissance de l'acte a été donnée", contentFont))
        document.add(Paragraph("en langue française que nous connaissons", contentFont))
        document.add(Paragraph("OU par OEC interprète ayant prêté serment", contentFont))
        document.add(Chunk.NEWLINE)
        document.add(Paragraph("En foi de quoi, avons dressé le présent acte.", contentFont))
    }

    private fun addSignatures(document: Document) {
        document.add(Chunk.NEWLINE)
        document.add(Chunk.NEWLINE)
        
        val signatureTable = PdfPTable(2)
        signatureTable.widthPercentage = 100f
        signatureTable.setWidths(floatArrayOf(50f, 50f))
        
        val declarantCell = PdfPCell(Paragraph("Le déclarant"))
        declarantCell.border = Rectangle.NO_BORDER
        declarantCell.horizontalAlignment = Element.ALIGN_CENTER
        
        val officierCell = PdfPCell(Paragraph("L'officier de l'État-civil"))
        officierCell.border = Rectangle.NO_BORDER
        officierCell.horizontalAlignment = Element.ALIGN_CENTER
        
        signatureTable.addCell(declarantCell)
        signatureTable.addCell(officierCell)
        
        document.add(signatureTable)
    }

    private fun formatDateForActe(date: java.time.LocalDate?): DateInfo {
        return if (date != null) {
            DateInfo(
                day = date.dayOfMonth,
                month = FRENCH_MONTHS[date.monthValue] ?: "",
                year = date.year,
                hour = 12, // Heure par défaut
                minute = 0
            )
        } else {
            DateInfo(1, "janvier", 2024, 12, 0)
        }
    }

    private data class DateInfo(
        val day: Int,
        val month: String,
        val year: Int,
        val hour: Int,
        val minute: Int
    )
}

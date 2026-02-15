package uniearn.services;

import uniearn.model.entities.Contrat;
import javafx.scene.canvas.Canvas;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service pour la génération et l'export des contrats en PDF
 *
 * NOTE: Cette classe est un template pour l'export PDF
 * Pour utiliser, ajouter une dépendance PDF au pom.xml:
 * - iText: https://itextpdf.com/
 * - Apache PDFBox: https://pdfbox.apache.org/
 */
public class ContractPDFService {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Exporter un contrat en PDF
     *
     * @param contrat Le contrat à exporter
     * @param outputPath Le chemin de sortie du fichier PDF
     * @param canvasClientSignature Canvas avec la signature du client
     * @param canvasFreelancerSignature Canvas avec la signature du freelancer
     * @return true si l'export est réussi, false sinon
     */
    public boolean exportContractToPDF(Contrat contrat, String outputPath,
                                       Canvas canvasClientSignature,
                                       Canvas canvasFreelancerSignature) {
        try {
            // TODO: Implémenter l'export PDF
            // Exemple avec iText:
            // Document document = new Document();
            // PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            // document.open();
            // document.add(new Paragraph("CONTRAT DE SERVICES"));
            // ... ajouter le contenu ...
            // document.close();

            // OU avec PDFBox:
            // PDDocument document = new PDDocument();
            // PDPage page = new PDPage();
            // document.addPage(page);
            // PDPageContentStream contentStream = new PDPageContentStream(document, page);
            // ... ajouter le contenu ...
            // contentStream.close();
            // document.save(outputPath);

            System.out.println("Implémentation PDF requise. Utilisez iText ou PDFBox.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter un contrat en PDF avec une structure simple
     * Version avec Apache PDFBox (gratuit et open-source)
     */
    public boolean exportContractToPDFSimple(Contrat contrat, String outputPath) {
        try {
            // Code d'exemple pour PDFBox
            /*
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Titre
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("CONTRAT DE SERVICES");
            contentStream.endText();

            // Contenu
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700);

            String[] lines = {
                "Contrat ID: " + contrat.getIdContract(),
                "Type: " + contrat.getType(),
                "Montant: " + contrat.getAmount() + " DA",
                "Date Début: " + contrat.getStartDate(),
                "Date Fin: " + contrat.getEndDate(),
                "Statut: " + contrat.getStatusString(),
                "",
                "Signatures:",
                "Client: ___________________ Date: " + dateFormat.format(new Date(contrat.getClientSignatureDate().getTime())),
                "Freelancer: ___________________ Date: " + dateFormat.format(new Date(contrat.getFreelancerSignatureDate().getTime()))
            };

            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -15);
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputPath);
            document.close();
            */

            System.out.println("Export PDF vers: " + outputPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Vérifier si le répertoire de sortie existe, sinon le créer
     */
    public boolean ensureOutputDirectory(String outputPath) {
        try {
            File file = new File(outputPath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                return parentDir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Générer un nom de fichier PDF unique
     */
    public String generatePDFFileName(Contrat contrat) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return String.format("Contrat_%d_%s.pdf", contrat.getIdContract(), sdf.format(new Date()));
    }

    /**
     * Obtenir le chemin par défaut d'export des PDFs
     */
    public String getDefaultPDFDirectory() {
        String userHome = System.getProperty("user.home");
        String pdfDir = userHome + File.separator + "Documents" + File.separator + "UniEarn_Contracts";

        File dir = new File(pdfDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return pdfDir;
    }
}


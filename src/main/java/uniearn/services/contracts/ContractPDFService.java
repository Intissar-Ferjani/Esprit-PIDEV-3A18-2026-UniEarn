package uniearn.services.contracts;

import uniearn.model.entities.contracts.Contrat;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service pour la génération et l'export des contrats en PDF
 * Utilise iText 5 pour la génération de PDF
 */
public class ContractPDFService {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Exporter un contrat en PDF
     *
     * @param contrat Le contrat à exporter
     * @param outputPath Le chemin de sortie du fichier PDF
     * @return true si l'export est réussi, false sinon
     */
    public boolean exportContractToPDF(Contrat contrat, String outputPath) {
        FileOutputStream fos = null;
        Document document = null;

        try {
            System.out.println("DEBUG: Début de l'export PDF");
            System.out.println("DEBUG: Contrat à exporter: " + contrat.getIdContract());
            System.out.println("DEBUG: Chemin de sortie: " + outputPath);

            // Vérifier que le répertoire existe
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean mkdirsResult = parentDir.mkdirs();
                System.out.println("DEBUG: Création du répertoire: " + mkdirsResult);
                if (!mkdirsResult && !parentDir.exists()) {
                    System.err.println("ERROR: Impossible de créer le répertoire");
                    return false;
                }
            }

            // Créer le document PDF
            document = new Document();
            fos = new FileOutputStream(outputPath);
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            writer.setCompressionLevel(9);

            // Ouvrir le document
            document.open();

            System.out.println("DEBUG: Document PDF créé et ouvert");

            // Ajouter le titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("CONTRAT DE SERVICES", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Ajouter une ligne vide
            document.add(new Paragraph("\n"));

            // Ajouter les informations du contrat
            addContractInfo(document, contrat);

            // Ajouter les signatures
            document.add(new Paragraph("\n"));
            addSignatureSection(document, contrat);

            System.out.println("DEBUG: Contenu ajouté au document");

            // Fermer le document AVANT de fermer le flux
            document.close();
            System.out.println("DEBUG: Document fermé avec succès");

            // Fermer le flux après le document
            fos.flush();
            fos.close();
            fos = null;

            System.out.println("DEBUG: Flux d'entrée/sortie fermé");

            // Attendre un peu pour que le système d'exploitation libère le fichier
            Thread.sleep(500);

            // Vérifier que le fichier a bien été créé
            if (outputFile.exists()) {
                long fileSize = outputFile.length();
                System.out.println("DEBUG: ✓ Fichier PDF créé avec succès!");
                System.out.println("DEBUG: Chemin absolu: " + outputFile.getAbsolutePath());
                System.out.println("DEBUG: Taille du fichier: " + fileSize + " bytes");

                if (fileSize > 0) {
                    System.out.println("DEBUG: Le fichier contient des données ✓");
                    return true;
                } else {
                    System.out.println("ERROR: Le fichier est vide!");
                    return false;
                }
            } else {
                System.out.println("ERROR: Le fichier PDF n'a pas été créé!");
                return false;
            }

        } catch (DocumentException e) {
            System.err.println("ERREUR DocumentException lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            System.err.println("ERREUR InterruptedException: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("ERREUR lors de la génération du PDF: " + e.getMessage());
            System.err.println("Type d'erreur: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        } finally {
            // S'assurer que tout est fermé
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }

    /**
     * Ajouter les informations du contrat au PDF
     */
    private void addContractInfo(Document document, Contrat contrat) throws DocumentException {
        try {
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10);

            document.add(new Paragraph("INFORMATIONS DU CONTRAT", headerFont));
            document.add(new Paragraph(""));

            document.add(new Paragraph("ID Contrat: " + contrat.getIdContract(), bodyFont));
            document.add(new Paragraph("Type: " + (contrat.getType() != null ? contrat.getType() : "Non spécifié"), bodyFont));
            document.add(new Paragraph("Montant: " + String.format("%.2f DA", contrat.getAmount()), bodyFont));
            document.add(new Paragraph("Date Début: " + (contrat.getStartDate() != null ? dateFormat.format(contrat.getStartDate()) : "N/A"), bodyFont));
            document.add(new Paragraph("Date Fin: " + (contrat.getEndDate() != null ? dateFormat.format(contrat.getEndDate()) : "N/A"), bodyFont));
            document.add(new Paragraph("Statut: " + contrat.getStatusString(), bodyFont));
            document.add(new Paragraph("Client ID: " + contrat.getClientID(), bodyFont));
            document.add(new Paragraph("Projet ID: " + contrat.getProjectID(), bodyFont));
            document.add(new Paragraph("Freelancer ID: " + contrat.getFreelancerID(), bodyFont));

            // Ajouter les dates de signature si disponibles
            if (contrat.getClientSignatureDate() != null) {
                document.add(new Paragraph("Signature Client: " + dateTimeFormat.format(contrat.getClientSignatureDate()), smallFont));
            }
            if (contrat.getFreelancerSignatureDate() != null) {
                document.add(new Paragraph("Signature Freelancer: " + dateTimeFormat.format(contrat.getFreelancerSignatureDate()), smallFont));
            }

            document.add(new Paragraph(""));
            document.add(new Paragraph("DESCRIPTION", headerFont));
            document.add(new Paragraph("Ce contrat établit les conditions et modalités de travail entre les parties signataires.", bodyFont));

            document.add(new Paragraph(""));
            document.add(new Paragraph("CONDITIONS GÉNÉRALES", headerFont));
            document.add(new Paragraph("1. Les deux parties acceptent les termes et conditions énoncés ci-dessus.", bodyFont));
            document.add(new Paragraph("2. Le paiement sera effectué selon le calendrier convenu.", bodyFont));
            document.add(new Paragraph("3. Les modifications du contrat doivent être approuvées par les deux parties.", bodyFont));

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout des infos au PDF: " + e.getMessage());
            throw new DocumentException("Erreur lors de l'ajout des informations du contrat", e);
        }
    }

    /**
     * Ajouter la section des signatures
     */
    private void addSignatureSection(Document document, Contrat contrat) throws DocumentException {
        try {
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11);

            document.add(new Paragraph("SIGNATURES", headerFont));
            document.add(new Paragraph(""));

            // ✅ NOUVEAU: Afficher les images de signature

            // Signature Client
            document.add(new Paragraph("Signature Client:", bodyFont));
            if (contrat.getClientSignatureImage() != null && contrat.getClientSignatureImage().length > 0) {
                try {
                    System.out.println("DEBUG PDF: Ajout image signature client - Taille: " + contrat.getClientSignatureImage().length);

                    // Convertir les bytes en Image JavaFX
                    javafx.scene.image.Image fxImage = SignatureImageService.bytesToImage(contrat.getClientSignatureImage());

                    if (fxImage != null) {
                        // Créer un fichier temporaire PNG à partir de l'image
                        File tempFile = File.createTempFile("signature_client_", ".png");

                        // Convertir l'Image JavaFX en BufferedImage et l'écrire en PNG
                        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                            (int) fxImage.getWidth(),
                            (int) fxImage.getHeight(),
                            java.awt.image.BufferedImage.TYPE_INT_ARGB
                        );

                        // Copier les pixels
                        javafx.scene.image.PixelReader reader = fxImage.getPixelReader();
                        for (int y = 0; y < fxImage.getHeight(); y++) {
                            for (int x = 0; x < fxImage.getWidth(); x++) {
                                int argb = reader.getArgb(x, y);
                                bufferedImage.setRGB(x, y, argb);
                            }
                        }

                        // Écrire en PNG
                        javax.imageio.ImageIO.write(bufferedImage, "png", tempFile);

                        // Ajouter au PDF
                        Image pdfImage = Image.getInstance(tempFile.getAbsolutePath());
                        pdfImage.setAlignment(Image.ALIGN_LEFT);
                        pdfImage.scaleToFit(200, 100);
                        document.add(pdfImage);

                        tempFile.deleteOnExit();
                        System.out.println("DEBUG PDF: Image client ajoutée avec succès");
                    } else {
                        document.add(new Paragraph("[✓ Signature enregistrée]", bodyFont));
                    }
                } catch (Exception e) {
                    System.err.println("ERREUR: Impossible d'ajouter l'image client au PDF: " + e.getMessage());
                    document.add(new Paragraph("[✓ Signature enregistrée]", bodyFont));
                }
            } else {
                document.add(new Paragraph("_________________________________", bodyFont));
            }

            if (contrat.getClientSignatureDate() != null) {
                document.add(new Paragraph("Date: " + dateTimeFormat.format(contrat.getClientSignatureDate()), bodyFont));
            } else {
                document.add(new Paragraph("Date: ____________________________", bodyFont));
            }

            document.add(new Paragraph(""));

            // Signature Freelancer
            document.add(new Paragraph("Signature Freelancer:", bodyFont));
            if (contrat.getFreelancerSignatureImage() != null && contrat.getFreelancerSignatureImage().length > 0) {
                try {
                    System.out.println("DEBUG PDF: Ajout image signature freelancer - Taille: " + contrat.getFreelancerSignatureImage().length);

                    // Convertir les bytes en Image JavaFX
                    javafx.scene.image.Image fxImage = SignatureImageService.bytesToImage(contrat.getFreelancerSignatureImage());

                    if (fxImage != null) {
                        // Créer un fichier temporaire PNG à partir de l'image
                        File tempFile = File.createTempFile("signature_freelancer_", ".png");

                        // Convertir l'Image JavaFX en BufferedImage et l'écrire en PNG
                        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                            (int) fxImage.getWidth(),
                            (int) fxImage.getHeight(),
                            java.awt.image.BufferedImage.TYPE_INT_ARGB
                        );

                        // Copier les pixels
                        javafx.scene.image.PixelReader reader = fxImage.getPixelReader();
                        for (int y = 0; y < fxImage.getHeight(); y++) {
                            for (int x = 0; x < fxImage.getWidth(); x++) {
                                int argb = reader.getArgb(x, y);
                                bufferedImage.setRGB(x, y, argb);
                            }
                        }

                        // Écrire en PNG
                        javax.imageio.ImageIO.write(bufferedImage, "png", tempFile);

                        // Ajouter au PDF
                        Image pdfImage = Image.getInstance(tempFile.getAbsolutePath());
                        pdfImage.setAlignment(Image.ALIGN_LEFT);
                        pdfImage.scaleToFit(200, 100);
                        document.add(pdfImage);

                        tempFile.deleteOnExit();
                        System.out.println("DEBUG PDF: Image freelancer ajoutée avec succès");
                    } else {
                        document.add(new Paragraph("[✓ Signature enregistrée]", bodyFont));
                    }
                } catch (Exception e) {
                    System.err.println("ERREUR: Impossible d'ajouter l'image freelancer au PDF: " + e.getMessage());
                    document.add(new Paragraph("[✓ Signature enregistrée]", bodyFont));
                }
            } else {
                document.add(new Paragraph("_________________________________", bodyFont));
            }

            if (contrat.getFreelancerSignatureDate() != null) {
                document.add(new Paragraph("Date: " + dateTimeFormat.format(contrat.getFreelancerSignatureDate()), bodyFont));
            } else {
                document.add(new Paragraph("Date: ____________________________", bodyFont));
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout des signatures: " + e.getMessage());
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
                boolean created = parentDir.mkdirs();
                System.out.println("Répertoire créé: " + parentDir.getAbsolutePath() + " - Succès: " + created);
                return created;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du répertoire: " + e.getMessage());
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
            boolean created = dir.mkdirs();
            System.out.println("Répertoire de PDFs créé: " + pdfDir + " - Succès: " + created);
        }

        return pdfDir;
    }
}




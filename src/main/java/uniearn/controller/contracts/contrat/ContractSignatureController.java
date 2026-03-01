package uniearn.controller.contracts.contrat;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContractPDFService;
import uniearn.services.contracts.ContratService;
import uniearn.services.contracts.SignatureImageService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contrôleur pour la signature des contrats
 */
public class ContractSignatureController {

    @FXML private Label lblTitle;
    @FXML private Label lblContractInfo;
    @FXML private Label lblType;
    @FXML private Label lblAmount;
    @FXML private Label lblStartDate;
    @FXML private Label lblEndDate;
    @FXML private Label lblStatus;
    @FXML private TextArea taContractContent;

    @FXML private Canvas canvasClientSignature;
    @FXML private Canvas canvasFreelancerSignature;
    @FXML private Button btnClearClientSignature;
    @FXML private Button btnClearFreelancerSignature;
    @FXML private Button btnSignClient;
    @FXML private Button btnSignFreelancer;
    @FXML private Label lblClientSignatureDate;
    @FXML private Label lblFreelancerSignatureDate;
    @FXML private Button btnClose;
    @FXML private Button btnExportPDF;

    private Stage dialogStage;
    private Contrat contract;
    private ContratService contratService;
    private ContractPDFService pdfService;
    private Runnable onSignatureComplete;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private String userType = "CLIENT"; // CLIENT ou FREELANCER

    private boolean isDrawingClient = false;
    private boolean isDrawingFreelancer = false;
    private boolean clientSigned = false;
    private boolean freelancerSigned = false;

    @FXML
    public void initialize() {
        setupCanvases();
        setupButtonListeners();
    }

    private void setupCanvases() {
        // Canvas Client Signature
        canvasClientSignature.setOnMousePressed(this::startDrawingClient);
        canvasClientSignature.setOnMouseDragged(this::drawOnClientCanvas);
        canvasClientSignature.setOnMouseReleased(e -> isDrawingClient = false);

        // Canvas Freelancer Signature
        canvasFreelancerSignature.setOnMousePressed(this::startDrawingFreelancer);
        canvasFreelancerSignature.setOnMouseDragged(this::drawOnFreelancerCanvas);
        canvasFreelancerSignature.setOnMouseReleased(e -> isDrawingFreelancer = false);
    }

    private void setupButtonListeners() {
        btnClearClientSignature.setOnAction(e -> clearCanvas(canvasClientSignature));
        btnClearFreelancerSignature.setOnAction(e -> clearCanvas(canvasFreelancerSignature));

        btnSignClient.setOnAction(e -> {
            if (isCanvasEmpty(canvasClientSignature)) {
                showAlert("Erreur", "Veuillez dessiner votre signature", Alert.AlertType.WARNING);
            } else {
                signByClient();
            }
        });

        btnSignFreelancer.setOnAction(e -> {
            if (isCanvasEmpty(canvasFreelancerSignature)) {
                showAlert("Erreur", "Veuillez dessiner votre signature", Alert.AlertType.WARNING);
            } else {
                signByFreelancer();
            }
        });

        btnClose.setOnAction(e -> dialogStage.close());
        btnExportPDF.setOnAction(e -> exportToPDF());
    }

    public void setContract(Contrat contract) {
        this.contract = contract;
        displayContractInfo();
    }

    public void setContratService(ContratService service) {
        this.contratService = service;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSignatureComplete(Runnable callback) {
        this.onSignatureComplete = callback;
    }

    public void setUserType(String type) {
        this.userType = type != null ? type.toUpperCase() : "CLIENT";
    }

    private void displayContractInfo() {
        if (contract == null) return;

        lblContractInfo.setText("Contrat ID: " + contract.getIdContract());
        lblType.setText(contract.getType());
        lblAmount.setText(contract.getAmount() + " DA");
        lblStartDate.setText(contract.getStartDate().toString());
        lblEndDate.setText(contract.getEndDate().toString());
        lblStatus.setText(contract.getStatusString());

        // Construire le contenu du contrat
        StringBuilder content = new StringBuilder();
        content.append("CONTRAT DE SERVICES\n\n");
        content.append("Contrat ID: ").append(contract.getIdContract()).append("\n");
        content.append("Type: ").append(contract.getType()).append("\n");
        content.append("Montant: ").append(contract.getAmount()).append(" DA\n");
        content.append("Date Début: ").append(contract.getStartDate()).append("\n");
        content.append("Date Fin: ").append(contract.getEndDate()).append("\n");
        content.append("Statut: ").append(contract.getStatusString()).append("\n\n");

        content.append("Le présent contrat définit les termes et conditions de la prestation de services.\n");
        content.append("Les deux parties acceptent les termes et conditions énoncés ci-dessus.\n");

        taContractContent.setText(content.toString());

        // ✅ NOUVEAU: Charger et afficher les images de signature enregistrées
        if (contract.getClientSignatureImage() != null && contract.getClientSignatureImage().length > 0) {
            System.out.println("DEBUG: Chargement image signature client - Taille: " + contract.getClientSignatureImage().length);
            Image clientSignatureImg = SignatureImageService.bytesToImage(contract.getClientSignatureImage());
            if (clientSignatureImg != null) {
                System.out.println("DEBUG: Image client convertie avec succès");
                // Afficher l'image sur le canvas
                GraphicsContext gcClient = canvasClientSignature.getGraphicsContext2D();
                gcClient.clearRect(0, 0, canvasClientSignature.getWidth(), canvasClientSignature.getHeight());
                gcClient.drawImage(clientSignatureImg, 0, 0);
                System.out.println("DEBUG: Image client affichée sur le canvas");
            } else {
                System.err.println("ERROR: Impossible de convertir l'image client");
            }
        }

        if (contract.getFreelancerSignatureImage() != null && contract.getFreelancerSignatureImage().length > 0) {
            System.out.println("DEBUG: Chargement image signature freelancer - Taille: " + contract.getFreelancerSignatureImage().length);
            Image freelancerSignatureImg = SignatureImageService.bytesToImage(contract.getFreelancerSignatureImage());
            if (freelancerSignatureImg != null) {
                System.out.println("DEBUG: Image freelancer convertie avec succès");
                // Afficher l'image sur le canvas
                GraphicsContext gcFreelancer = canvasFreelancerSignature.getGraphicsContext2D();
                gcFreelancer.clearRect(0, 0, canvasFreelancerSignature.getWidth(), canvasFreelancerSignature.getHeight());
                gcFreelancer.drawImage(freelancerSignatureImg, 0, 0);
                System.out.println("DEBUG: Image freelancer affichée sur le canvas");
            } else {
                System.err.println("ERROR: Impossible de convertir l'image freelancer");
            }
        }

        // Mettre à jour les dates de signature
        if (contract.getClientSignatureDate() != null) {
            clientSigned = true;
            lblClientSignatureDate.setText("Signé le: " + dateFormat.format(new Date(contract.getClientSignatureDate().getTime())));
            btnSignClient.setDisable(true);
            btnClearClientSignature.setDisable(true);
        }

        if (contract.getFreelancerSignatureDate() != null) {
            freelancerSigned = true;
            lblFreelancerSignatureDate.setText("Signé le: " + dateFormat.format(new Date(contract.getFreelancerSignatureDate().getTime())));
            btnSignFreelancer.setDisable(true);
            btnClearFreelancerSignature.setDisable(true);
        }

        // Désactiver les zones de signature inappropriées selon le type d'utilisateur
        if ("CLIENT".equals(userType)) {
            // Le client ne peut signer que sa propre zone
            canvasFreelancerSignature.setDisable(true);
            btnSignFreelancer.setDisable(true);
            btnClearFreelancerSignature.setDisable(true);
        } else if ("FREELANCER".equals(userType)) {
            // Le freelancer ne peut signer que sa propre zone
            canvasClientSignature.setDisable(true);
            btnSignClient.setDisable(true);
            btnClearClientSignature.setDisable(true);
        }
    }

    private void startDrawingClient(MouseEvent e) {
        if (!clientSigned) {
            isDrawingClient = true;
        }
    }

    private void drawOnClientCanvas(MouseEvent e) {
        if (isDrawingClient && !clientSigned) {
            GraphicsContext gc = canvasClientSignature.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        }
    }

    private void startDrawingFreelancer(MouseEvent e) {
        if (!freelancerSigned) {
            isDrawingFreelancer = true;
        }
    }

    private void drawOnFreelancerCanvas(MouseEvent e) {
        if (isDrawingFreelancer && !freelancerSigned) {
            GraphicsContext gc = canvasFreelancerSignature.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        }
    }

    private void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private boolean isCanvasEmpty(Canvas canvas) {
        // Simple check: si le canvas a une hauteur spécifiée et le contexte graphique peut dessiner
        // Pour une vérification plus simple, on peut utiliser un flag
        // Sinon, faire une vérification basique
        return false; // Pour maintenant, on suppose qu'il y a du contenu si l'utilisateur clique sur "Signer"
    }

    private void signByClient() {
        try {
            System.out.println("=== DÉBUT SIGNATURE CLIENT ===");
            System.out.println("Contrat ID: " + contract.getIdContract());

            // Capturer l'image du Canvas
            System.out.println("Tentative de capture du Canvas...");
            byte[] signatureImage = SignatureImageService.canvasToPngBytes(canvasClientSignature);

            System.out.println("Résultat capture:");
            System.out.println("  - signatureImage est null: " + (signatureImage == null));
            if (signatureImage != null) {
                System.out.println("  - Taille: " + signatureImage.length + " bytes");
            }

            if (signatureImage == null || signatureImage.length == 0) {
                System.err.println("ERROR: Impossible de capturer la signature");
                showAlert("Erreur", "Impossible de capturer la signature - Vérifiez que vous avez tracé quelque chose", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("DEBUG: Image de signature capturée - Taille: " +
                SignatureImageService.formatImageSize(signatureImage));

            // Signer le contrat avec l'image
            System.out.println("Appel de signByClientWithImage...");
            boolean result = contratService.signByClientWithImage(contract.getIdContract(), signatureImage);
            System.out.println("Résultat: " + result);

            if (result) {
                clientSigned = true;
                contract.setClientSignatureImage(signatureImage);
                contract.setClientSignatureDate(new java.sql.Timestamp(System.currentTimeMillis()));
                lblClientSignatureDate.setText("Signé le: " + dateFormat.format(new Date()));
                btnSignClient.setDisable(true);
                btnClearClientSignature.setDisable(true);

                // Mettre à jour le statut: si freelancer a déjà signé, le contrat devient "Actif" (3)
                // Sinon, le contrat passe à "Client Signed" (1)
                if (contract.getFreelancerSignatureDate() != null) {
                    contract.setStatus(3); // Active
                    contratService.updateContractStatus(contract.getIdContract(), 3);
                } else {
                    contract.setStatus(1); // Client Signed
                    contratService.updateContractStatus(contract.getIdContract(), 1);
                }

                showAlert("Succès", "Contrat signé par le client avec signature enregistrée", Alert.AlertType.INFORMATION);

                if (onSignatureComplete != null) {
                    onSignatureComplete.run();
                }
            } else {
                System.err.println("ERROR: signByClientWithImage a retourné false");
                showAlert("Erreur", "Erreur lors de la signature - Vérifiez la base de données", Alert.AlertType.ERROR);
            }
            System.out.println("=== FIN SIGNATURE CLIENT ===\n");
        } catch (Exception e) {
            System.err.println("EXCEPTION lors de la signature du client: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void signByFreelancer() {
        try {
            System.out.println("=== DÉBUT SIGNATURE FREELANCER ===");
            System.out.println("Contrat ID: " + contract.getIdContract());

            // Capturer l'image du Canvas
            System.out.println("Tentative de capture du Canvas...");
            byte[] signatureImage = SignatureImageService.canvasToPngBytes(canvasFreelancerSignature);

            System.out.println("Résultat capture:");
            System.out.println("  - signatureImage est null: " + (signatureImage == null));
            if (signatureImage != null) {
                System.out.println("  - Taille: " + signatureImage.length + " bytes");
            }

            if (signatureImage == null || signatureImage.length == 0) {
                System.err.println("ERROR: Impossible de capturer la signature");
                showAlert("Erreur", "Impossible de capturer la signature - Vérifiez que vous avez tracé quelque chose", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("DEBUG: Image de signature capturée - Taille: " +
                SignatureImageService.formatImageSize(signatureImage));

            // Signer le contrat avec l'image
            System.out.println("Appel de signByFreelancerWithImage...");
            boolean result = contratService.signByFreelancerWithImage(contract.getIdContract(), signatureImage);
            System.out.println("Résultat: " + result);

            if (result) {
                freelancerSigned = true;
                contract.setFreelancerSignatureImage(signatureImage);
                contract.setFreelancerSignatureDate(new java.sql.Timestamp(System.currentTimeMillis()));
                lblFreelancerSignatureDate.setText("Signé le: " + dateFormat.format(new Date()));
                btnSignFreelancer.setDisable(true);
                btnClearFreelancerSignature.setDisable(true);

                // Mettre à jour le statut: si client a déjà signé, le contrat devient "Actif" (3)
                // Sinon, le contrat passe à "Freelancer Signed" (2)
                if (contract.getClientSignatureDate() != null) {
                    contract.setStatus(3); // Active
                    contratService.updateContractStatus(contract.getIdContract(), 3);
                } else {
                    contract.setStatus(2); // Freelancer Signed
                    contratService.updateContractStatus(contract.getIdContract(), 2);
                }

                showAlert("Succès", "Contrat signé par le freelancer avec signature enregistrée", Alert.AlertType.INFORMATION);

                // Vérifier si les deux ont signé
                if (clientSigned && freelancerSigned) {
                    showAlert("Succès", "Contrat complètement signé! Vous pouvez exporter en PDF", Alert.AlertType.INFORMATION);
                    btnExportPDF.setStyle("-fx-background-color: #FF9800;");
                }

                if (onSignatureComplete != null) {
                    onSignatureComplete.run();
                }
            } else {
                System.err.println("ERROR: signByFreelancerWithImage a retourné false");
                showAlert("Erreur", "Erreur lors de la signature - Vérifiez la base de données", Alert.AlertType.ERROR);
            }
            System.out.println("=== FIN SIGNATURE FREELANCER ===\n");
        } catch (Exception e) {
            System.err.println("EXCEPTION lors de la signature du freelancer: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportToPDF() {
        if (!clientSigned || !freelancerSigned) {
            showAlert("Attention", "Les deux parties doivent signer avant d'exporter en PDF", Alert.AlertType.WARNING);
            return;
        }

        try {
            pdfService = new ContractPDFService();
            System.out.println("DEBUG: Début de l'export PDF");

            // Créer un FileChooser pour choisir le dossier de destination
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le contrat en PDF");
            fileChooser.setInitialDirectory(new File(pdfService.getDefaultPDFDirectory()));
            fileChooser.setInitialFileName(pdfService.generatePDFFileName(contract));
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            File selectedFile = fileChooser.showSaveDialog(dialogStage);
            if (selectedFile != null) {
                String outputPath = selectedFile.getAbsolutePath();
                System.out.println("DEBUG: Chemin de sortie: " + outputPath);

                // Exporter le PDF avec la nouvelle implémentation iText
                if (pdfService.exportContractToPDF(contract, outputPath)) {
                    showAlert("Succès", "Contrat exporté avec succès:\n" + outputPath, Alert.AlertType.INFORMATION);
                    System.out.println("PDF exporté avec succès: " + outputPath);

                    // Vérifier que le fichier a bien été créé
                    File pdfFile = new File(outputPath);
                    System.out.println("Fichier existe: " + pdfFile.exists());
                    System.out.println("Taille du fichier: " + pdfFile.length() + " bytes");
                } else {
                    showAlert("Erreur", "Erreur lors de l'export du PDF", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


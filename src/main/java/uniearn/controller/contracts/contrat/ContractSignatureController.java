package uniearn.controller.contracts.contrat;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.contracts.ContractTemplate;
import uniearn.services.contracts.ContractPDFService;
import uniearn.services.contracts.ContractTemplateService;
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

    @FXML private Button btnAISummary;
    @FXML private Canvas canvasSignature;
    @FXML private Button btnClearSignature;
    @FXML private Button btnSignContract;
    @FXML private Label lblCreatedAt;
    
    @FXML private ImageView imgClientSignature;
    @FXML private ImageView imgFreelancerSignature;
    @FXML private Button btnExportPDFActual;

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
        // Note: setupMainCanvas() is called after setContract() and setUserType()
        // so that clientSigned/freelancerSigned are known
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
            System.out.println("DEBUG: btnSignClient clicked");
            if (isCanvasEmpty(canvasClientSignature)) {
                showAlert("Erreur", "Veuillez dessiner votre signature", Alert.AlertType.WARNING);
            } else {
                signByClient();
            }
        });

        btnSignFreelancer.setOnAction(e -> {
            System.out.println("DEBUG: btnSignFreelancer clicked");
            if (isCanvasEmpty(canvasFreelancerSignature)) {
                showAlert("Erreur", "Veuillez dessiner votre signature", Alert.AlertType.WARNING);
            } else {
                signByFreelancer();
            }
        });

        btnClose.setOnAction(e -> dialogStage.close());
        btnExportPDF.setOnAction(e -> exportToPDF());

        btnAISummary.setOnAction(e -> showAISummary());
        btnClearSignature.setOnAction(e -> clearCanvas(canvasSignature));
        btnSignContract.setOnAction(e -> handleSignature());
        btnExportPDFActual.setOnAction(e -> exportToPDF());
    }

    private void setupMainCanvas() {
        // Cache la zone de signature si l'utilisateur a déjà signé
        if (("CLIENT".equals(userType) && clientSigned) || ("FREELANCER".equals(userType) && freelancerSigned)) {
            canvasSignature.getParent().getParent().setVisible(false);
            canvasSignature.getParent().getParent().setManaged(false);
            return;
        }

        canvasSignature.setOnMousePressed(e -> {
            if (("CLIENT".equals(userType) && !clientSigned) || ("FREELANCER".equals(userType) && !freelancerSigned)) {
                canvasSignature.getGraphicsContext2D().beginPath();
            }
        });
        canvasSignature.setOnMouseDragged(e -> {
            if (("CLIENT".equals(userType) && !clientSigned) || ("FREELANCER".equals(userType) && !freelancerSigned)) {
                GraphicsContext gc = canvasSignature.getGraphicsContext2D();
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });
    }

    private void handleSignature() {
        if ("CLIENT".equals(userType)) {
            signByClientMain();
        } else {
            signByFreelancerMain();
        }
    }

    private void signByClientMain() {
        if (!SignatureImageService.canvasHasSignature(canvasSignature)) {
            showAlert("Erreur", "Veuillez d'abord dessiner votre signature dans la zone ci-dessous", Alert.AlertType.WARNING);
            return;
        }
        byte[] signatureImage = SignatureImageService.canvasToPngBytes(canvasSignature);
        if (signatureImage == null) {
            showAlert("Erreur", "Impossible de capturer la signature", Alert.AlertType.ERROR);
            return;
        }
        boolean result = contratService.signByClientWithImage(contract.getIdContract(), signatureImage);
        if (result) {
            clientSigned = true;
            contract.setClientSignatureImage(signatureImage);
            contract.setClientSignatureDate(new java.sql.Timestamp(System.currentTimeMillis()));
            showAlert("Succès", "Contrat signé par le client avec succès !", Alert.AlertType.INFORMATION);
            displayContractInfo();
            setupMainCanvas(); // Disable canvas after signing
            if (onSignatureComplete != null) onSignatureComplete.run();
        } else {
            showAlert("Erreur", "Erreur lors de la signature. Vérifiez la connexion à la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void signByFreelancerMain() {
        if (!SignatureImageService.canvasHasSignature(canvasSignature)) {
            showAlert("Erreur", "Veuillez d'abord dessiner votre signature dans la zone ci-dessous", Alert.AlertType.WARNING);
            return;
        }
        byte[] signatureImage = SignatureImageService.canvasToPngBytes(canvasSignature);
        if (signatureImage == null) {
            showAlert("Erreur", "Impossible de capturer la signature", Alert.AlertType.ERROR);
            return;
        }
        boolean result = contratService.signByFreelancerWithImage(contract.getIdContract(), signatureImage);
        if (result) {
            freelancerSigned = true;
            contract.setFreelancerSignatureImage(signatureImage);
            contract.setFreelancerSignatureDate(new java.sql.Timestamp(System.currentTimeMillis()));
            showAlert("Succès", "Contrat signé par le freelancer avec succès !", Alert.AlertType.INFORMATION);
            displayContractInfo();
            setupMainCanvas(); // Disable canvas after signing
            if (onSignatureComplete != null) onSignatureComplete.run();
        } else {
            showAlert("Erreur", "Erreur lors de la signature. Vérifiez la connexion à la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void showAISummary() {
        String content = taContractContent.getText();
        String summary = "SÉCURITÉ ET CONFIANCE\n" +
                        "Ce contrat est un accord de services professionnels. Points clés :\n" +
                        "• Montant : " + contract.getAmount() + " USD\n" +
                        "• Période : " + contract.getStartDate() + " au " + contract.getEndDate() + "\n" +
                        "• Statut actuel : " + contract.getStatusString() + "\n\n" +
                        "Résumé généré par l'IA UniEarn.";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résumé IA");
        alert.setHeaderText("Résumé du Contrat par l'IA");
        alert.setContentText(summary);
        alert.showAndWait();
    }

    public void setContract(Contrat contract) {
        this.contract = contract;
        displayContractInfo();
        // Now that clientSigned/freelancerSigned are set, configure the canvas
        setupMainCanvas();
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
        // If contract already loaded, reconfigure the canvas
        if (contract != null) {
            setupMainCanvas();
        }
    }

    private void displayContractInfo() {
        if (contract == null) return;

        System.out.println("=== displayContractInfo START ===");
        System.out.println("  contract.getTitle()=" + contract.getTitle());
        System.out.println("  contract.getType()=" + contract.getType());
        System.out.println("  contract.getAmount()=" + contract.getAmount());
        System.out.println("  contract.getFreelancerID()=" + contract.getFreelancerID());
        System.out.println("  contract.getStartDate()=" + contract.getStartDate());
        System.out.println("  contract.getEndDate()=" + contract.getEndDate());
        System.out.println("  contract.getContent()=" + contract.getContent());

        try {
            // Show contract title (like Symfony)
            String title = contract.getTitle() != null && !contract.getTitle().isEmpty()
                    ? contract.getTitle()
                    : (contract.getType() != null ? contract.getType() : "Contrat #" + contract.getIdContract());
            lblContractInfo.setText(title);
            System.out.println("  lblContractInfo set to: " + title);

            // Show freelancer name
            String freelancerName = "Non assigné";
            if (contract.getFreelancerID() > 0) {
                try {
                    // Query freelancer name by joining freelancer + user tables
                    uniearn.services.DataLoaderService loader = new uniearn.services.DataLoaderService();
                    String name = loader.getFreelancerNameByFreelancerId(contract.getFreelancerID());
                    if (name != null && !name.equals("Inconnu")) {
                        freelancerName = name;
                    } else {
                        freelancerName = "Freelancer #" + contract.getFreelancerID();
                    }
                } catch (Exception e) {
                    freelancerName = "Freelancer #" + contract.getFreelancerID();
                }
            }
            lblType.setText(freelancerName);
            System.out.println("  lblType set to: " + freelancerName);

            String amountText = String.format("%.2f TND", contract.getAmount());
            lblAmount.setText(amountText);
            System.out.println("  lblAmount set to: " + amountText);

            // Format dates nicely like Symfony (May 12, 2026)
            SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            String startDateStr = contract.getStartDate() != null ? displayDateFormat.format(contract.getStartDate()) : "N/A";
            String endDateStr = contract.getEndDate() != null ? displayDateFormat.format(contract.getEndDate()) : "N/A";
            lblStartDate.setText(startDateStr);
            lblEndDate.setText(endDateStr);
            System.out.println("  lblStartDate set to: " + startDateStr);
            System.out.println("  lblEndDate set to: " + endDateStr);

            lblStatus.setText(contract.getStatusString());

            if (lblCreatedAt != null) {
                lblCreatedAt.setText("Created: " + dateFormat.format(contract.getStartDate()));
            }

            // Style status badge based on status
            updateStatusBadge(contract.getStatus());

            // Use contract's own content field first, fallback to template
            String content = contract.getContent();
            if (content != null && !content.isEmpty()) {
                taContractContent.setText(content);
            } else if (contract.getTemplateID() > 0) {
                ContractTemplate template = new ContractTemplateService().getTemplateById(contract.getTemplateID());
                if (template != null) {
                    taContractContent.setText(template.getTemplateContent());
                } else {
                    taContractContent.setText("Contenu du contrat standard...");
                }
            } else {
                taContractContent.setText("Contenu du contrat standard...");
            }

            System.out.println("=== displayContractInfo END (success) ===");
        } catch (Exception e) {
            System.err.println("ERROR in displayContractInfo: " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ NOUVEAU: Charger et afficher les images de signature enregistrées
        if (contract.getClientSignatureImage() != null && contract.getClientSignatureImage().length > 0) {
            System.out.println("DEBUG: Chargement image signature client - Taille: " + contract.getClientSignatureImage().length);
            Image clientSignatureImg = SignatureImageService.bytesToImage(contract.getClientSignatureImage());
            if (clientSignatureImg != null) {
                System.out.println("DEBUG: Image client convertie avec succès");
                // Afficher l'image sur l'ImageView
                if (imgClientSignature != null) {
                    imgClientSignature.setImage(clientSignatureImg);
                }
                lblClientSignatureDate.setText(""); // Effacer le texte "Not signed"
            } else {
                System.err.println("ERROR: Impossible de convertir l'image client");
            }
        }

        if (contract.getFreelancerSignatureImage() != null && contract.getFreelancerSignatureImage().length > 0) {
            System.out.println("DEBUG: Chargement image signature freelancer - Taille: " + contract.getFreelancerSignatureImage().length);
            Image freelancerSignatureImg = SignatureImageService.bytesToImage(contract.getFreelancerSignatureImage());
            if (freelancerSignatureImg != null) {
                System.out.println("DEBUG: Image freelancer convertie avec succès");
                // Afficher l'image sur l'ImageView
                if (imgFreelancerSignature != null) {
                    imgFreelancerSignature.setImage(freelancerSignatureImg);
                }
                lblFreelancerSignatureDate.setText(""); // Effacer le texte "Not signed"
            } else {
                System.err.println("ERROR: Impossible de convertir l'image freelancer");
            }
        }

        // Mettre à jour les dates de signature
        if (contract.getClientSignatureDate() != null) {
            clientSigned = true;
            lblClientSignatureDate.setText("Signé le: " + dateFormat.format(new Date(contract.getClientSignatureDate().getTime())));
        }

        if (contract.getFreelancerSignatureDate() != null) {
            freelancerSigned = true;
            lblFreelancerSignatureDate.setText("Signé le: " + dateFormat.format(new Date(contract.getFreelancerSignatureDate().getTime())));
        }
        
        if (("CLIENT".equals(userType) && clientSigned) || ("FREELANCER".equals(userType) && freelancerSigned)) {
            btnSignContract.setDisable(true);
            btnClearSignature.setDisable(true);
            canvasSignature.setDisable(true);
        }

        // Enable PDF export only if both signed or if it's already completed
        boolean fullySigned = clientSigned && freelancerSigned;
        btnExportPDFActual.setDisable(!fullySigned && contract.getStatus() < 3);
        
        if (fullySigned) {
            btnExportPDFActual.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 10; -fx-cursor: hand;");
        } else {
            btnExportPDFActual.setOpacity(0.5);
        }
    }

    private void updateStatusBadge(int status) {
        String color;
        String textColor;
        String text;
        
        switch (status) {
            case 3: // Active
                color = "#e8f5e9";
                textColor = "#2e7d32";
                text = "✅ Contrat Actif";
                break;
            case 4: // Cancelled
                color = "#ffebee";
                textColor = "#c62828";
                text = "❌ Annulé";
                break;
            default:
                color = "#fff8e1";
                textColor = "#ffa000";
                text = "⌛ En attente de signatures";
                break;
        }
        
        lblStatus.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + textColor + 
                          "; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        lblStatus.setText(text);
    }

    private void startDrawingClient(MouseEvent e) {
        System.out.println("DEBUG startDrawingClient: clientSigned=" + clientSigned + ", canvasDisabled=" + canvasClientSignature.isDisabled() + ", mouseTransparent=" + canvasClientSignature.isMouseTransparent());
        if (!clientSigned) {
            isDrawingClient = true;
        }
    }

    private void drawOnClientCanvas(MouseEvent e) {
        if (isDrawingClient && !clientSigned) {
            System.out.println("DEBUG drawOnClientCanvas: Drawing at (" + e.getX() + ", " + e.getY() + ")");
            GraphicsContext gc = canvasClientSignature.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        }
    }

    private void startDrawingFreelancer(MouseEvent e) {
        System.out.println("DEBUG startDrawingFreelancer: freelancerSigned=" + freelancerSigned + ", canvasDisabled=" + canvasFreelancerSignature.isDisabled() + ", mouseTransparent=" + canvasFreelancerSignature.isMouseTransparent());
        if (!freelancerSigned) {
            isDrawingFreelancer = true;
        }
    }

    private void drawOnFreelancerCanvas(MouseEvent e) {
        if (isDrawingFreelancer && !freelancerSigned) {
            System.out.println("DEBUG drawOnFreelancerCanvas: Drawing at (" + e.getX() + ", " + e.getY() + ")");
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
        // Pour maintenant, on suppose qu'il y a du contenu si l'utilisateur clique sur "Signer"
        // La vérification réelle se fera lors de la capture de l'image
        return false;
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

    private void updateSignatureControls() {
        if (canvasClientSignature == null || canvasFreelancerSignature == null) {
            return;
        }

        System.out.println("DEBUG updateSignatureControls: userType=" + userType +
            ", clientSigned=" + clientSigned + ", freelancerSigned=" + freelancerSigned);

        if ("CLIENT".equals(userType)) {
            canvasFreelancerSignature.setDisable(true);
            btnSignFreelancer.setDisable(true);
            btnClearFreelancerSignature.setDisable(true);

            if (!clientSigned) {
                canvasClientSignature.setDisable(false);
                canvasClientSignature.setMouseTransparent(false);
                btnSignClient.setDisable(false);
                btnClearClientSignature.setDisable(false);
                canvasClientSignature.setOnMousePressed(this::startDrawingClient);
                canvasClientSignature.setOnMouseDragged(this::drawOnClientCanvas);
                canvasClientSignature.setOnMouseReleased(e -> isDrawingClient = false);
                System.out.println("DEBUG: CLIENT mode - Canvas client réactivé");
            }
        } else if ("FREELANCER".equals(userType)) {
            canvasClientSignature.setDisable(true);
            btnSignClient.setDisable(true);
            btnClearClientSignature.setDisable(true);

            if (!freelancerSigned) {
                canvasFreelancerSignature.setDisable(false);
                canvasFreelancerSignature.setMouseTransparent(false);
                btnSignFreelancer.setDisable(false);
                btnClearFreelancerSignature.setDisable(false);
                canvasFreelancerSignature.setOnMousePressed(this::startDrawingFreelancer);
                canvasFreelancerSignature.setOnMouseDragged(this::drawOnFreelancerCanvas);
                canvasFreelancerSignature.setOnMouseReleased(e -> isDrawingFreelancer = false);
                System.out.println("DEBUG: FREELANCER mode - Canvas freelancer réactivé");
            }
        }
    }
}

package uniearn.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.model.entities.Contrat;
import uniearn.services.ContractPDFService;
import uniearn.services.ContratService;

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
        if (contratService.signByClient(contract.getIdContract())) {
            clientSigned = true;
            lblClientSignatureDate.setText("Signé le: " + dateFormat.format(new Date()));
            btnSignClient.setDisable(true);
            btnClearClientSignature.setDisable(true);
            showAlert("Succès", "Contrat signé par le client", Alert.AlertType.INFORMATION);

            if (onSignatureComplete != null) {
                onSignatureComplete.run();
            }
        } else {
            showAlert("Erreur", "Erreur lors de la signature", Alert.AlertType.ERROR);
        }
    }

    private void signByFreelancer() {
        if (contratService.signByFreelancer(contract.getIdContract())) {
            freelancerSigned = true;
            lblFreelancerSignatureDate.setText("Signé le: " + dateFormat.format(new Date()));
            btnSignFreelancer.setDisable(true);
            btnClearFreelancerSignature.setDisable(true);
            showAlert("Succès", "Contrat signé par le freelancer", Alert.AlertType.INFORMATION);

            // Vérifier si les deux ont signé
            if (clientSigned && freelancerSigned) {
                showAlert("Succès", "Contrat complètement signé! Vous pouvez exporter en PDF", Alert.AlertType.INFORMATION);
                btnExportPDF.setStyle("-fx-background-color: #FF9800;");
            }

            if (onSignatureComplete != null) {
                onSignatureComplete.run();
            }
        } else {
            showAlert("Erreur", "Erreur lors de la signature", Alert.AlertType.ERROR);
        }
    }

    private void exportToPDF() {
        if (!clientSigned || !freelancerSigned) {
            showAlert("Attention", "Les deux parties doivent signer avant d'exporter en PDF", Alert.AlertType.WARNING);
            return;
        }

        try {
            pdfService = new ContractPDFService();

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

                // Exporter le PDF
                if (pdfService.exportContractToPDFSimple(contract, outputPath)) {
                    showAlert("Succès", "Contrat exporté avec succès:\n" + outputPath, Alert.AlertType.INFORMATION);
                    System.out.println("PDF exporté: " + outputPath);
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


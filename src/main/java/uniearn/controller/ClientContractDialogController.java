package uniearn.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.Contrat;
import uniearn.model.entities.ContractTemplate;
import uniearn.services.ContractTemplateService;
import uniearn.services.ContratService;
import uniearn.services.DataLoaderService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Contrôleur pour le dialog de création/modification de contrat client
 */
public class ClientContractDialogController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<ContractTemplate> cbTemplate;
    @FXML private ComboBox<String> cbContractType;
    @FXML private ComboBox<Integer> cbFreelancer;
    @FXML private ComboBox<Integer> cbProject;
    @FXML private TextField tfAmount;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<Integer> cbPayment;
    @FXML private TextArea taTemplatePreview;
    @FXML private Button btnCancel;
    @FXML private Button btnCreate;

    private Stage dialogStage;
    private int clientID;
    private ContractTemplateService templateService;
    private ContratService contratService;
    private Consumer<Contrat> onContractCreated;

    @FXML
    public void initialize() {
        templateService = new ContractTemplateService();
        contratService = new ContratService();
        setupListeners();
        loadTemplates();
        loadFreelancers();
        loadProjects();
        loadPayments();
    }

    private void setupListeners() {
        btnCancel.setOnAction(e -> dialogStage.close());

        btnCreate.setOnAction(e -> {
            if (validateForm()) {
                createContract();
            }
        });

        // Afficher l'aperçu du template sélectionné
        cbTemplate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTemplatePreview(newVal);
            }
        });
    }

    private void loadTemplates() {
        var templates = templateService.getAllTemplates();
        cbTemplate.setItems(FXCollections.observableArrayList(templates));
        if (!templates.isEmpty()) {
            cbTemplate.getSelectionModel().selectFirst();
        }
    }

    private void loadFreelancers() {
        // TODO: Charger depuis la BD
        try {
            DataLoaderService loader = new DataLoaderService();
            var freelancers = loader.getAllFreelancers();
            var freelancerIds = new ArrayList<Integer>();
            for (var f : freelancers) {
                freelancerIds.add(f.getIdUser());
            }
            cbFreelancer.setItems(FXCollections.observableArrayList(freelancerIds));
        } catch (Exception e) {
            System.err.println("Erreur chargement freelancers: " + e.getMessage());
            cbFreelancer.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        }
    }

    private void loadProjects() {
        // TODO: Charger les projets du client
        try {
            DataLoaderService loader = new DataLoaderService();
            var projects = loader.getProjectsByClient(clientID);
            cbProject.setItems(FXCollections.observableArrayList(projects));
        } catch (Exception e) {
            System.err.println("Erreur chargement projets: " + e.getMessage());
            cbProject.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        }
    }

    private void loadPayments() {
        // TODO: Charger les paiements disponibles
        try {
            DataLoaderService loader = new DataLoaderService();
            var payments = loader.getAvailablePayments();
            cbPayment.setItems(FXCollections.observableArrayList(payments));
        } catch (Exception e) {
            System.err.println("Erreur chargement paiements: " + e.getMessage());
            cbPayment.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        }
    }

    private void updateTemplatePreview(ContractTemplate template) {
        String preview = template.getTemplateContent()
            .replace("[ClientName]", "Votre Nom")
            .replace("[FreelancerName]", "Nom du Freelancer")
            .replace("[StartDate]", dpStartDate.getValue() != null ? dpStartDate.getValue().toString() : "01/01/2024")
            .replace("[EndDate]", dpEndDate.getValue() != null ? dpEndDate.getValue().toString() : "31/01/2024")
            .replace("[Amount]", tfAmount.getText().isEmpty() ? "0.00" : tfAmount.getText());

        taTemplatePreview.setText(preview);
    }

    private boolean validateForm() {
        if (cbTemplate.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un template", Alert.AlertType.ERROR);
            return false;
        }

        if (cbFreelancer.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un freelancer", Alert.AlertType.ERROR);
            return false;
        }

        if (cbProject.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un projet", Alert.AlertType.ERROR);
            return false;
        }

        if (tfAmount.getText().isEmpty()) {
            showAlert("Erreur", "Entrez un montant", Alert.AlertType.ERROR);
            return false;
        }

        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showAlert("Erreur", "Sélectionnez les dates", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void createContract() {
        try {
            Contrat contrat = new Contrat();
            contrat.setType(cbContractType.getValue() != null ? cbContractType.getValue() : "Standard");
            contrat.setTemplateID(cbTemplate.getValue().getIdTemplate());
            contrat.setClientID(clientID);
            contrat.setFreelancerID(cbFreelancer.getValue());
            contrat.setProjectID(cbProject.getValue());
            contrat.setAmount(Double.parseDouble(tfAmount.getText()));
            contrat.setStartDate(Timestamp.valueOf(dpStartDate.getValue().atStartOfDay()));
            contrat.setEndDate(Timestamp.valueOf(dpEndDate.getValue().atStartOfDay()));
            contrat.setStatus(0); // Brouillon
            contrat.setPaymentID(cbPayment.getValue() != null ? cbPayment.getValue() : 0);

            if (contratService.createContrat(contrat)) {
                showAlert("Succès", "Contrat créé avec succès", Alert.AlertType.INFORMATION);
                if (onContractCreated != null) {
                    onContractCreated.accept(contrat);
                }
            } else {
                showAlert("Erreur", "Erreur lors de la création du contrat", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnContractCreated(Consumer<Contrat> callback) {
        this.onContractCreated = callback;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


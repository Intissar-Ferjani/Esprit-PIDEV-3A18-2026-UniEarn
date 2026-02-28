package uniearn.controller.contracts.client;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.contracts.ContractTemplate;
import uniearn.model.entities.contracts.ContractType;
import uniearn.model.entities.Project;
import uniearn.model.entities.Payment;
import uniearn.services.contracts.ContractTemplateService;
import uniearn.services.contracts.ContractTypeService;
import uniearn.services.contracts.ContratService;
import uniearn.services.DataLoaderService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Contrôleur pour le dialog de création/modification de contrat client
 */
public class ClientContractDialogController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<ContractTemplate> cbTemplate;
    @FXML private ComboBox<String> cbMetier;
    @FXML private ComboBox<ContractType> cbContractType;
    @FXML private ComboBox<Integer> cbFreelancer;
    @FXML private ComboBox<Project> cbProject;
    @FXML private TextField tfAmount;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<Payment> cbPayment;
    @FXML private TextArea taTemplatePreview;
    @FXML private Button btnCancel;
    @FXML private Button btnCreate;

    private Stage dialogStage;
    private int clientID;
    private ContractTemplateService templateService;
    private ContractTypeService contractTypeService;
    private ContratService contratService;
    private DataLoaderService dataLoaderService;
    private Consumer<Contrat> onContractCreated;

    @FXML
    public void initialize() {
        templateService = new ContractTemplateService();
        contractTypeService = new ContractTypeService();
        contratService = new ContratService();
        dataLoaderService = new DataLoaderService();
        setupListeners();
        loadTemplates();
        loadMetiers();
        loadFreelancers();
        loadPayments();
        // Les projets seront chargés dans setClientID()
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

        // Charger les types de contrats quand le métier change
        cbMetier.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadContractTypesByMetier(newVal);
            }
        });
    }

    private void loadTemplates() {
        var templates = templateService.getAllTemplates();
        cbTemplate.setItems(FXCollections.observableArrayList(templates));

        // Afficher le nom du template
        cbTemplate.setCellFactory(param -> new ListCell<ContractTemplate>() {
            @Override
            protected void updateItem(ContractTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTemplateName());
            }
        });

        cbTemplate.setButtonCell(new ListCell<ContractTemplate>() {
            @Override
            protected void updateItem(ContractTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTemplateName());
            }
        });

        if (!templates.isEmpty()) {
            cbTemplate.getSelectionModel().selectFirst();
        }
    }

    private void loadMetiers() {
        try {
            Set<String> metiers = dataLoaderService.getAllMetiers();
            cbMetier.setItems(FXCollections.observableArrayList(metiers));
            if (!metiers.isEmpty()) {
                cbMetier.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement métiers: " + e.getMessage());
            cbMetier.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadContractTypesByMetier(String metier) {
        try {
            List<ContractType> types = dataLoaderService.getContractTypesByMetier(metier);
            cbContractType.setItems(FXCollections.observableArrayList(types));

            // Afficher le nom du type de contrat
            cbContractType.setCellFactory(param -> new ListCell<ContractType>() {
                @Override
                protected void updateItem(ContractType item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTypeName());
                }
            });

            cbContractType.setButtonCell(new ListCell<ContractType>() {
                @Override
                protected void updateItem(ContractType item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTypeName());
                }
            });

            if (!types.isEmpty()) {
                cbContractType.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement types de contrats: " + e.getMessage());
            cbContractType.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadFreelancers() {
        try {
            DataLoaderService loader = new DataLoaderService();
            var freelancers = loader.getAllFreelancers();

            // Afficher le nom du freelancer
            cbFreelancer.setCellFactory(param -> new ListCell<Integer>() {
                private DataLoaderService loaderService = new DataLoaderService();

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = loaderService.getFreelancerName(item);
                        setText(name + " (ID: " + item + ")");
                    }
                }
            });

            cbFreelancer.setButtonCell(new ListCell<Integer>() {
                private DataLoaderService loaderService = new DataLoaderService();

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = loaderService.getFreelancerName(item);
                        setText(name + " (ID: " + item + ")");
                    }
                }
            });

            var freelancerIds = new ArrayList<Integer>();
            for (var f : freelancers) {
                freelancerIds.add(f.getIdUser());
            }
            cbFreelancer.setItems(FXCollections.observableArrayList(freelancerIds));
        } catch (Exception e) {
            System.err.println("Erreur chargement freelancers: " + e.getMessage());
            cbFreelancer.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadProjects() {
        try {
            System.out.println("DEBUG: loadProjects() - clientID = " + clientID);
            var projects = dataLoaderService.getProjectsByClient(clientID);
            System.out.println("DEBUG: Récupéré " + projects.size() + " projets");
            for (Project p : projects) {
                System.out.println("  - " + p.getIdProject() + ": " + p.getTitle());
            }
            cbProject.setItems(FXCollections.observableArrayList(projects));

            // Afficher le titre du projet
            cbProject.setCellFactory(param -> new ListCell<Project>() {
                @Override
                protected void updateItem(Project item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitle());
                }
            });

            cbProject.setButtonCell(new ListCell<Project>() {
                @Override
                protected void updateItem(Project item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitle());
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur chargement projets: " + e.getMessage());
            e.printStackTrace();
            cbProject.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadPayments() {
        try {
            DataLoaderService loader = new DataLoaderService();
            var payments = loader.getAvailablePayments();
            cbPayment.setItems(FXCollections.observableArrayList(payments));

            // Afficher le montant du paiement
            cbPayment.setCellFactory(param -> new ListCell<Payment>() {
                @Override
                protected void updateItem(Payment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : String.format("Paiement #%d - %.2f DA", item.getIdPayment(), item.getAmount()));
                }
            });

            cbPayment.setButtonCell(new ListCell<Payment>() {
                @Override
                protected void updateItem(Payment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : String.format("Paiement #%d - %.2f DA", item.getIdPayment(), item.getAmount()));
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur chargement paiements: " + e.getMessage());
            cbPayment.setItems(FXCollections.observableArrayList());
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

        if (cbMetier.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un métier", Alert.AlertType.ERROR);
            return false;
        }

        if (cbContractType.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un type de contrat", Alert.AlertType.ERROR);
            return false;
        }

        // Freelancer est optionnel
        // if (cbFreelancer.getValue() == null) {
        //     showAlert("Erreur", "Sélectionnez un freelancer", Alert.AlertType.ERROR);
        //     return false;
        // }

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
            System.out.println("DEBUG: Tentative de création du contrat");

            Contrat contrat = new Contrat();
            ContractType selectedType = cbContractType.getValue();
            String typeName = selectedType != null ? selectedType.getTypeName() : "Standard";
            contrat.setType(typeName);
            contrat.setTemplateID(cbTemplate.getValue().getIdTemplate());
            contrat.setClientID(clientID);
            // Ne pas définir le freelancerID - le laisser NULL pour l'instant
            contrat.setFreelancerID(0);
            contrat.setProjectID(cbProject.getValue().getIdProject());
            contrat.setAmount(Double.parseDouble(tfAmount.getText()));
            contrat.setStartDate(Timestamp.valueOf(dpStartDate.getValue().atStartOfDay()));
            contrat.setEndDate(Timestamp.valueOf(dpEndDate.getValue().atStartOfDay()));
            contrat.setStatus(0); // Brouillon
            contrat.setPaymentID(cbPayment.getValue() != null ? cbPayment.getValue().getIdPayment() : 0);

            System.out.println("DEBUG: Contrat à créer: " + contrat);

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
        System.out.println("DEBUG: setClientID() appelé avec clientID = " + clientID);
        // Charger les projets maintenant que clientID est défini
        loadProjects();
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


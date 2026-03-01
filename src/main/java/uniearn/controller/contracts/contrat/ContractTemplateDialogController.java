package uniearn.controller.contracts.contrat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.ContractTemplate;

import java.util.function.Consumer;

/**
 * Contrôleur pour le dialog d'ajout/modification de templates
 */
public class ContractTemplateDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField tfTemplateName;
    @FXML private TextField tfDescription;
    @FXML private TextArea taTemplateContent;
    @FXML private TextArea taPreview;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private Stage dialogStage;
    private ContractTemplate template;
    private Consumer<ContractTemplate> onSave;

    @FXML
    public void initialize() {
        setupListeners();
    }

    private void setupListeners() {
        btnCancel.setOnAction(e -> dialogStage.close());

        btnSave.setOnAction(e -> {
            if (validateForm()) {
                ContractTemplate result = new ContractTemplate();
                result.setTemplateName(tfTemplateName.getText());
                result.setDescription(tfDescription.getText());
                result.setTemplateContent(taTemplateContent.getText());

                if (onSave != null) {
                    onSave.accept(result);
                }
            }
        });

        // Live preview
        taTemplateContent.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePreview(newVal);
        });
    }

    public void setTemplate(ContractTemplate template) {
        this.template = template;
        lblTitle.setText("Modifier Template");
        tfTemplateName.setText(template.getTemplateName());
        tfDescription.setText(template.getDescription());
        taTemplateContent.setText(template.getTemplateContent());
        updatePreview(template.getTemplateContent());
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSave(Consumer<ContractTemplate> callback) {
        this.onSave = callback;
    }

    private void updatePreview(String content) {
        // Aperçu avec variables remplacées par des exemples
        String preview = content
            .replace("[ClientName]", "Jean Dupont")
            .replace("[FreelancerName]", "Marie Martin")
            .replace("[StartDate]", "01/01/2024")
            .replace("[EndDate]", "31/01/2024")
            .replace("[Amount]", "50000");

        taPreview.setText(preview);
    }

    private boolean validateForm() {
        if (tfTemplateName.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom du template est obligatoire", Alert.AlertType.ERROR);
            return false;
        }

        if (taTemplateContent.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le contenu du template est obligatoire", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


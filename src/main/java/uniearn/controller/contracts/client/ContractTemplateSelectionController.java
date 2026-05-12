package uniearn.controller.contracts.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.ContractTemplate;
import uniearn.services.contracts.ContractTemplateService;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ContractTemplateSelectionController {

    @FXML private FlowPane templatesContainer;
    @FXML private Button btnMyContracts;

    private ContractTemplateService templateService;
    private int clientID;
    private Stage dialogStage;
    private Consumer<Void> onContractsRequested;
    private Runnable onContractCreated;

    @FXML
    public void initialize() {
        templateService = new ContractTemplateService();
        loadTemplates();
        
        btnMyContracts.setOnAction(e -> {
            if (onContractsRequested != null) {
                onContractsRequested.accept(null);
            }
            dialogStage.close();
        });
    }

    private void loadTemplates() {
        List<ContractTemplate> templates = templateService.getAllTemplates();
        templatesContainer.getChildren().clear();

        for (ContractTemplate template : templates) {
            templatesContainer.getChildren().add(createTemplateCard(template));
        }
    }

    private VBox createTemplateCard(ContractTemplate template) {
        VBox card = new VBox(10);
        card.getStyleClass().add("template-card");
        card.setPrefSize(250, 200);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label titleLabel = new Label(template.getTemplateName());
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label typeBadge = new Label("fixed_price"); // Defaulting to fixed price as per capture
        typeBadge.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-padding: 3 10; " +
                          "-fx-background-radius: 10; -fx-font-size: 11;");

        Label descLabel = new Label(template.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13;");
        descLabel.setPrefHeight(60);

        Button useBtn = new Button("Use This Template");
        useBtn.getStyleClass().add("btn-primary");
        useBtn.setMaxWidth(Double.MAX_VALUE);
        useBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-padding: 10; " +
                       "-fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        
        useBtn.setOnAction(e -> openContractForm(template));

        card.getChildren().addAll(titleLabel, typeBadge, descLabel, useBtn);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                                                "-fx-effect: dropshadow(three-pass-box, rgba(26,115,232,0.2), 15, 0, 0, 8); -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-translate-y: 0;"));

        return card;
    }

    private void openContractForm(ContractTemplate template) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/client_contract_dialog.fxml"));
            Parent root = loader.load();

            ClientContractDialogController controller = loader.getController();
            controller.setClientID(clientID);
            controller.setSelectedTemplate(template);

            Stage stage = new Stage();
            stage.setTitle("New Contract - " + template.getTemplateName());
            stage.setScene(new Scene(root, 800, 800));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dialogStage);
            
            controller.setDialogStage(stage);
            
            if (onContractCreated != null) {
                controller.setOnContractCreated(contract -> {
                    onContractCreated.run();
                });
            }
            
            // Close selection dialog and open form
            dialogStage.close();
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setOnContractsRequested(Consumer<Void> onContractsRequested) {
        this.onContractsRequested = onContractsRequested;
    }

    public void setOnContractCreated(Runnable onContractCreated) {
        this.onContractCreated = onContractCreated;
    }
}

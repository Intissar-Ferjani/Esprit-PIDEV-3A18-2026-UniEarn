package uniearn.test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test l'interface Signature - Signature Électronique
 * Version simplifiée avec Canvas de test
 */
public class TestContractSignature extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Créer une interface de test simple
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: white;");

            VBox header = new VBox(10);
            header.setPadding(new Insets(20));
            header.setStyle("-fx-background-color: #1E56DB;");

            Label title = new Label("SIGNATURE DU CONTRAT");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

            Label contractInfo = new Label("Contrat ID: Test");
            contractInfo.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 12;");

            header.getChildren().addAll(title, contractInfo);

            // Canvas pour signature client
            Label lblClient = new Label("Signature Client:");
            lblClient.setStyle("-fx-font-weight: bold;");
            Canvas canvasClient = new Canvas(400, 100);
            canvasClient.setStyle("-fx-border-color: #999; -fx-border-width: 1;");
            Button btnSignClient = new Button("✓ Signer");
            btnSignClient.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            VBox signClientBox = new VBox(5);
            signClientBox.getChildren().addAll(lblClient, canvasClient, btnSignClient);

            // Canvas pour signature freelancer
            Label lblFreelancer = new Label("Signature Freelancer:");
            lblFreelancer.setStyle("-fx-font-weight: bold;");
            Canvas canvasFreelancer = new Canvas(400, 100);
            canvasFreelancer.setStyle("-fx-border-color: #999; -fx-border-width: 1;");
            Button btnSignFreelancer = new Button("✓ Signer");
            btnSignFreelancer.setStyle("-fx-background-color: #1E56DB; -fx-text-fill: white;");

            VBox signFreelancerBox = new VBox(5);
            signFreelancerBox.getChildren().addAll(lblFreelancer, canvasFreelancer, btnSignFreelancer);

            HBox signatureBoxes = new HBox(20);
            signatureBoxes.getChildren().addAll(signClientBox, signFreelancerBox);

            Label status = new Label("✅ Interface Signature chargée avec succès!");
            status.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12; -fx-font-weight: bold;");

            Button btnExportPDF = new Button("📄 Exporter en PDF");
            btnExportPDF.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20;");

            root.getChildren().addAll(header, signatureBoxes, status, btnExportPDF);

            Scene scene = new Scene(root, 1000, 500);
            stage.setTitle("Test: Signature du Contrat");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ TestContractSignature démarré avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement interface:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


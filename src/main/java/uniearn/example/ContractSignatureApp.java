package uniearn.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uniearn.controller.contracts.ContractSignatureController;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.sql.Timestamp;
import java.time.LocalDate;

public class ContractSignatureApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
        Scene scene = new Scene(loader.load(), 900, 800);

        // Créer un contrat de test
        Contrat contrat = new Contrat();
        contrat.setIdContract(12);
        contrat.setType("Standard");
        contrat.setClientID(1);
        contrat.setFreelancerID(2);
        contrat.setProjectID(2);
        contrat.setAmount(75000);
        contrat.setStatus(1);
        contrat.setStartDate(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        contrat.setEndDate(Timestamp.valueOf(LocalDate.now().plusMonths(2).atStartOfDay()));

        ContractSignatureController controller = loader.getController();
        controller.setContract(contrat);
        controller.setContratService(new ContratService());
        controller.setDialogStage(primaryStage);

        primaryStage.setTitle("UniEarn - Signature de Contrat");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


package uniearn.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import uniearn.model.entities.Project;
import uniearn.model.enums.taskstatusenum;
import uniearn.services.ProjectService;

import java.sql.SQLException;

public class ProjectController {

    private ProjectService services= new ProjectService();

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<?, ?> budgetColumn;

    @FXML
    private TextField budgetField;

    @FXML
    private Button clearButton;

    @FXML
    private TableColumn<?, ?> clientIdColumn;

    @FXML
    private TextField clientIdField;

    @FXML
    private Label countLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TableColumn<Project,String> descriptionColumn;

    @FXML
    private TableColumn<Project, Integer> idColumn;

    @FXML
    private TableView<> projectTable;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<?, ?> statusColumn;

    @FXML
    private ComboBox<taskstatusenum> statusComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private TableColumn<?, ?> titleColumn;

    @FXML
    private TextField titleField;

    @FXML
    private Button updateButton;

    @FXML
    void handleAdd(ActionEvent event) {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            double budget = Double.parseDouble(budgetField.getText());
            int status = statusComboBox.getSelectionModel().getSelectedIndex();
            int clientId = Integer.parseInt(clientIdField.getText());

            // Create a new Project object
            Project newProject = new Project(title, description, budget, status, clientId);

            // Add the project to the database
            try {
                services.addProject(newProject);
                // Refresh the project table view
                handleRefresh();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle any errors that occur during database operations
            }
    }

    @FXML
    void handleClear(ActionEvent event) {

    }

    @FXML
    void handleDelete(ActionEvent event) {

    }

    @FXML
    void handleRefresh() {
        services.getAllProjects();

    }

    @FXML
    void handleUpdate(ActionEvent event) {

    }

}

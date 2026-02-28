package uniearn.controller.projet;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import uniearn.controller.profile.admin.AdminDashboardController;
import uniearn.controller.profile.freelancer.ListFreelancersController;
import uniearn.database.SessionManager;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.users.client.Client;
import uniearn.model.enums.taskstatusenum;
import uniearn.services.projet.ProjectService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProjectController {

    private ProjectService services = new ProjectService();

    private Integer selectedProjectId = null;

    private Client currentClient;

    public void setClientData(Client client) {
        this.currentClient = client;
    }

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Project, Double> budgetColumn;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button editProfileButton;

    @FXML
    private TextField budgetField;

    @FXML
    private Button clearButton;

    @FXML
    private TableColumn<Project, Integer> clientIdColumn;

    @FXML
    private TextField clientIdField;

    @FXML
    private Label countLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TableColumn<Project, String> descriptionColumn;

    @FXML
    private TableColumn<Project, Integer> idColumn;

    @FXML
    private TableView<Project> projectTable;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Project, String> statusColumn;

    @FXML
    private ComboBox<taskstatusenum> statusComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private TableColumn<Project, String> titleColumn;

    @FXML
    private TextField titleField;

    @FXML
    private Label nameLabel;

    @FXML
    private Button updateButton;

    @FXML
    private TableColumn<Project, Void> actionColumn;

    @FXML
    public void initialize() {
        if (statusComboBox != null) {
            statusComboBox.getItems().setAll(taskstatusenum.values());
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("idproject"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        // map integer status to readable enum name
        statusColumn.setCellValueFactory(cellData -> {
            int s = cellData.getValue().getStatus();
            String text = "";
            try {
                text = taskstatusenum.values()[s].name();
            } catch (Exception e) {
                text = "UNKNOWN";
            }
            return new ReadOnlyStringWrapper(text);
        });
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("client_id"));

        // add action buttons column (Modifier / Supprimer) if present in FXML
        if (actionColumn != null) {
            Callback<TableColumn<Project, Void>, TableCell<Project, Void>> cellFactory = new Callback<>() {
                @Override
                public TableCell<Project, Void> call(final TableColumn<Project, Void> param) {
                    return new TableCell<>() {

                        private final Button btnEdit = new Button("Modifier");
                        private final Button btnDelete = new Button("Supprimer");
                        private final HBox pane = new HBox(8, btnEdit, btnDelete);

                        {

                            btnEdit.getStyleClass().addAll("button-primary");
                            btnDelete.getStyleClass().addAll("button-danger");

                            btnEdit.setOnAction((ActionEvent event) -> {
                                Project project = getTableView().getItems().get(getIndex());
                                populateFormForEdit(project);
                            });

                            btnDelete.setOnAction((ActionEvent event) -> {
                                Project project = getTableView().getItems().get(getIndex());
                                boolean ok = showConfirm("Supprimer le projet",
                                        "Voulez-vous vraiment supprimer le projet \"" + project.getTitle() + "\" ?");
                                if (!ok)
                                    return;
                                try {
                                    services.deleteProject(project.getIdproject());
                                    handleRefresh();
                                    showInfo("Projet supprimé", "Le projet a été supprimé avec succès.");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(pane);
                            }
                        }
                    };
                }
            };

            actionColumn.setCellFactory(cellFactory);
        }

        handleRefresh();
    }

    private void populateFormForEdit(Project project) {
        if (project == null)
            return;
        selectedProjectId = project.getIdproject();
        titleField.setText(project.getTitle());
        descriptionArea.setText(project.getDescription());
        budgetField.setText(String.valueOf(project.getBudget()));
        clientIdField.setText(String.valueOf(project.getClient_id()));
        // select status by index if valid
        try {
            statusComboBox.getSelectionModel().select(project.getStatus());
        } catch (Exception e) {
            statusComboBox.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void handleAdd(ActionEvent event) {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        double budget = Double.parseDouble(budgetField.getText());
        int status = statusComboBox.getSelectionModel().getSelectedIndex();
        int clientId = Integer.parseInt(clientIdField.getText());
        int freelancerIDD = 23;

        // Create a new Project object
        Project newProject = new Project(title, description, budget, status, clientId, freelancerIDD);

        // Add the project to the database
        try {
            services.addProject(newProject);
            // Refresh the project table view
            handleRefresh();
            showInfo("Projet ajouté", "Le projet a été ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any errors that occur during database operations
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        budgetField.clear();
        clientIdField.clear();
        statusComboBox.getSelectionModel().clearSelection();
        projectTable.getSelectionModel().clearSelection();
        selectedProjectId = null;
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearForm();
    }
    /*
     * @FXML
     * void handleDelete(ActionEvent event) {
     * Project selected = projectTable.getSelectionModel().getSelectedItem();
     * if (selected != null) {
     * boolean ok = showConfirm("Supprimer le projet",
     * "Voulez-vous vraiment supprimer le projet \"" + selected.getTitle() +
     * "\" ?");
     * if (!ok) return;
     * services.deleteProject(selected.getIdproject());
     * handleRefresh();
     * showInfo("Projet supprimé", "Le projet a été supprimé avec succès.");
     * }
     * 
     * }
     */

    @FXML
    void handleRefresh() {
        // Fetch all projects from the service and populate the TableView
        List<Project> projects = services.getAllProjects();
        ObservableList<Project> data = FXCollections.observableArrayList(projects);
        projectTable.setItems(data);

        // Update count label
        if (countLabel != null) {
            countLabel.setText("Total: " + data.size() + " projets");
        }
    }

    @FXML
    private void handleBrowseFreelancers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/list-freelancers.fxml"));
            Parent root = loader.load();

            ListFreelancersController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Browse Freelancers - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load browse freelancers page: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedProjectId == null) {
            // nothing selected for edit
            return;
        }

        String title = titleField.getText();
        String description = descriptionArea.getText();
        double budget = 0;
        try {
            budget = Double.parseDouble(budgetField.getText());
        } catch (NumberFormatException e) {
        }
        int status = statusComboBox.getSelectionModel().getSelectedIndex();
        int clientId = 0;
        try {
            clientId = Integer.parseInt(clientIdField.getText());
        } catch (NumberFormatException e) {
        }

        Project updated = new Project(title, description, budget, status, clientId, 23);
        try {
            services.updateProject(selectedProjectId, updated);
            handleRefresh();
            clearForm();
            showInfo("Projet modifié", "Le projet a été modifié avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to show information alerts
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper to show confirmation dialogs and return true if user confirms
    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to access your account.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    SessionManager.getInstance().logout();
                    redirectToLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Error", "Failed to logout: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBackToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
            Parent root = loader.load();

            uniearn.controller.profile.client.ClientProfileController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            // e.printStackTrace();
            showErrorAlert("Error", "Failed to load profile page: " + e.getMessage());
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();

            Stage stage = null;
            if (projectTable != null && projectTable.getScene() != null) {
                stage = (Stage) projectTable.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(new Scene(root, 750, 600));
                stage.setTitle("Login - UniEarn");
                stage.centerOnScreen();
            } else {
                showErrorAlert("Error", "Unable to navigate to login page.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Account Settings");
        alert.setContentText(
                "Settings page coming soon!\n\nFeatures:\n• Notification preferences\n• Privacy settings\n• Language selection");
        alert.showAndWait();
    }
}

package uniearn.controller.projet;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import uniearn.controller.profile.admin.AdminDashboardController;
import uniearn.controller.profile.forum.MessagesController;
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

    private Parent embeddedDashboard;

    private ObservableList<Project> projectList = FXCollections.observableArrayList();

    private boolean isFreelancerMode = false;

    public void setFreelancerMode(boolean isFreelancer) {
        this.isFreelancerMode = isFreelancer;
        if (isFreelancer) {
            if (addButton != null)
                addButton.setVisible(false);
            if (updateButton != null)
                updateButton.setVisible(false);
            if (clearButton != null)
                clearButton.setVisible(false);
        }
        handleRefresh();
    }

    public void setClientData(Client client) {
        this.currentClient = client;
        handleRefresh();
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
    private ScrollPane dashboardView;

    @FXML
    private TextField budgetField;

    @FXML
    private Button clearButton;

    @FXML
    private TableColumn<Project, Integer> clientIdColumn;

    @FXML
    private Label countLabel;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private Label budgetErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

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
    private HBox statusContainer;

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
    private StackPane contentArea;

    @FXML
    private TableColumn<Project, Void> actionColumn;

    @FXML
    private TableColumn<Project, Void> chatColumn;

    @FXML
    public void initialize() {
        if (statusComboBox != null) {
            statusComboBox.getItems().setAll(taskstatusenum.values());
            statusComboBox.getSelectionModel().select(0); // Default to TODO
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

        projectTable.setItems(projectList); // Initialize table with the observable list

        // Add selection listener to populate form when a row is clicked
        projectTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFormForEdit(newValue);
            }
        });

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
                            btnEdit.setStyle(
                                    "-fx-background-color: forestgreen; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                            btnDelete.setStyle(
                                    "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

                            btnEdit.setOnAction(event -> {
                                Project project = getTableView().getItems().get(getIndex());
                                populateFormForEdit(project);
                            });

                            btnDelete.setOnAction(event -> {
                                Project project = getTableView().getItems().get(getIndex());
                                handleDelete(project);
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

        // Chat column — open messaging with the freelancer
        if (chatColumn != null) {
            chatColumn.setCellFactory(col -> new TableCell<>() {
                private final Button chatBtn = new Button("💬 Chat");
                {
                    chatBtn.setStyle(
                            "-fx-background-color: #1a56db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
                    chatBtn.setOnAction(event -> {
                        Project project = getTableView().getItems().get(getIndex());
                        String freelancerName = project.getFreelancerName();
                        if (freelancerName == null || freelancerName.equals("Unknown")) {
                            freelancerName = services.getFreelancerNameById(project.getFreelancerid());
                        }
                        openChatWithFreelancer(freelancerName);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : chatBtn);
                }
            });
        }

        // handleRefresh() is now called in setClientData, so it's not needed here
        // unless currentClient is guaranteed to be set before initialize
        // For safety, we can keep it, but it might refresh twice if setClientData is
        // called immediately after initialize.
        // A better approach is to ensure setClientData is always called and triggers
        // the first refresh.
        // For now, I'll remove it here as setClientData will handle the initial load.
        // handleRefresh();
    }

    private void populateFormForEdit(Project project) {
        if (project == null)
            return;
        selectedProjectId = project.getIdproject();
        titleField.setText(project.getTitle());
        descriptionArea.setText(project.getDescription());
        budgetField.setText(String.valueOf(project.getBudget()));
        // clientIdField.setText(String.valueOf(project.getClient_id())); // Removed
        // clientIdField
        // select status by index if valid
        try {
            statusComboBox.getSelectionModel().select(project.getStatus());
        } catch (Exception e) {
            statusComboBox.getSelectionModel().clearSelection();
        }

        if (statusContainer != null) {
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            titleErrorLabel.setText("Le titre est requis.");
            titleErrorLabel.setVisible(true);
            titleErrorLabel.setManaged(true);
            isValid = false;
        } else if (!titleField.getText().matches(".*[a-zA-Z].*")) {
            titleErrorLabel.setText("Le titre doit contenir au moins une lettre.");
            titleErrorLabel.setVisible(true);
            titleErrorLabel.setManaged(true);
            isValid = false;
        } else {
            titleErrorLabel.setVisible(false);
            titleErrorLabel.setManaged(false);
        }

        // Validate Budget
        if (budgetField.getText() == null || budgetField.getText().trim().isEmpty()) {
            budgetErrorLabel.setText("Le budget est requis.");
            budgetErrorLabel.setVisible(true);
            budgetErrorLabel.setManaged(true);
            isValid = false;
        } else if (!budgetField.getText().trim().matches("^[0-9]+(\\.[0-9]+)?$")) {
            budgetErrorLabel.setText("Le budget doit contenir uniquement des chiffres.");
            budgetErrorLabel.setVisible(true);
            budgetErrorLabel.setManaged(true);
            isValid = false;
        } else {
            try {
                double budget = Double.parseDouble(budgetField.getText());
                if (budget <= 0) {
                    budgetErrorLabel.setText("Le budget doit être supérieur à 0.");
                    budgetErrorLabel.setVisible(true);
                    budgetErrorLabel.setManaged(true);
                    isValid = false;
                } else {
                    budgetErrorLabel.setVisible(false);
                    budgetErrorLabel.setManaged(false);
                }
            } catch (NumberFormatException e) {
                budgetErrorLabel.setText("Le budget doit être un nombre valide.");
                budgetErrorLabel.setVisible(true);
                budgetErrorLabel.setManaged(true);
                isValid = false;
            }
        }

        // Validate Description
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            descriptionErrorLabel.setManaged(true);
            isValid = false;
        } else if (!descriptionArea.getText().matches(".*[a-zA-Z].*")) {
            descriptionErrorLabel.setText("La description doit contenir  des lettres !");
            descriptionErrorLabel.setVisible(true);
            descriptionErrorLabel.setManaged(true);
            isValid = false;
        } else {
            descriptionErrorLabel.setVisible(false);
            descriptionErrorLabel.setManaged(false);
        }

        return isValid;
    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        String title = titleField.getText();
        String description = descriptionArea.getText();
        double budget = Double.parseDouble(budgetField.getText());
        int status = 0; // Forced default TODO
        int freelancerIDD = 23; // Default or placeholder freelancer ID

        if (currentClient == null) {
            showErrorAlert("Error", "Client data not available. Cannot add project.");
            return;
        }
        int clientId = currentClient.getIdClient();

        // Create a new Project object
        Project newProject = new Project(title, description, budget, status, clientId, freelancerIDD);

        // Add the project to the database
        try {
            services.addProject(newProject);
            // Refresh the project table view
            handleRefresh();
            clearForm(); // Clear form after successful add
            showInfo("Projet ajouté", "Le projet a été ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to add project: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Input Error", "Please enter a valid number for budget.");
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        budgetField.clear();
        statusComboBox.getSelectionModel().clearSelection();
        projectTable.getSelectionModel().clearSelection();
        selectedProjectId = null;
        // Clear error labels
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        budgetErrorLabel.setVisible(false);
        budgetErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);

        // Hide status container
        if (statusContainer != null) {
            statusContainer.setVisible(false);
            statusContainer.setManaged(false);
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearForm();
    }

    private void handleDelete(Project project) {
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
            showErrorAlert("Database Error", "Failed to delete project: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh() {
        if (isFreelancerMode) {
            projectList.setAll(services.getAllProjects());
            setupFiltering();
        } else if (currentClient != null) {
            projectList.setAll(services.getProjectsByClientId(currentClient.getIdClient()));
            setupFiltering();
        } else {
            projectList.clear();
        }
        if (countLabel != null) {
            countLabel.setText("Total : " + projectList.size() + " projets");
        }
    }

    private void setupFiltering() {
        if (searchField == null)
            return;

        FilteredList<Project> filteredData = new FilteredList<>(projectList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(project -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (project.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (project.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
            countLabel.setText("Total : " + filteredData.size() + " projets");
        });

        SortedList<Project> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(projectTable.comparatorProperty());
        projectTable.setItems(sortedData);
    }

    @FXML
    private void handleBrowseFreelancers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/Freelancer/list-freelancers.fxml"));
            Parent root = loader.load();

            ListFreelancersController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Browse Freelancers - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load browse freelancers page: " + e.getMessage());
        }
    }

    private void openChatWithFreelancer(String freelancerName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Messages.fxml"));
            Parent root = loader.load();
            MessagesController controller = loader.getController();
            controller.setChatWith(freelancerName);
            controller.initialize();
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat with " + freelancerName);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open chat: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedProjectId == null) {
            showErrorAlert("Error", "Veuillez sélectionner un projet à modifier.");
            return;
        }

        if (!validateInputs()) {
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
        int clientId = currentClient != null ? currentClient.getIdClient() : 0;

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
    private void handleTaskBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/TaskBoard.fxml"));
            Parent root = loader.load();
            TaskBoardController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowDashboard() {
        if (embeddedDashboard != null) {
            embeddedDashboard.setVisible(false);
            embeddedDashboard.setManaged(false);
        }

        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        System.out.println("✓ Switched back to main profile dashboard");
    }

    @FXML
    private void handleApplications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/ClientDashboardView.fxml"));
            Parent root = loader.load();

            // ClientDashboardController controller = loader.getController();

            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Applications & Reviews - UniEarn");
            stage.centerOnScreen();

            System.out.println("✓ Switched to Applications Dashboard scene");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load applications page: " + e.getMessage());
        }
    }

}

package uniearn.controller.projet;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import uniearn.controller.profile.client.ClientProfileController;
import uniearn.controller.profile.freelancer.FreelancerProfileController;
import uniearn.controller.profile.freelancer.ListFreelancersController;
import uniearn.database.SessionManager;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.projet.Task;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.taskpriorityenum;
import uniearn.model.enums.taskstatusenum;
import uniearn.services.projet.ProjectService;
import uniearn.services.projet.TaskService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskBoardController {

    @FXML
    private ToggleButton viewToggle;

    @FXML
    private TextField searchField;

    @FXML
    private HBox boardContainer;

    private Client currentClient;
    private Freelancer currentFreelancer; // Freelancer mode support

    private final TaskService taskService = new TaskService();
    private final ProjectService projectService = new ProjectService();

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private List<Project> clientProjects = new ArrayList<>();

    private boolean isViewByStatus = true; // default view

    public void setClientData(Client client) {
        this.currentClient = client;
        loadData();
    }

    public void setFreelancerData(Freelancer freelancer) {
        this.currentFreelancer = freelancer;
        loadData();
    }

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            renderBoard();
        });

        viewToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isViewByStatus = newVal;
            viewToggle.setText(isViewByStatus ? "View: By Status" : "View: By Project");
            renderBoard();
        });
        viewToggle.setSelected(true); // Default to Status view
    }

    private void loadData() {
        List<Task> allDBTasks = taskService.getAllTasks();
        List<Task> tempTasks = new ArrayList<>();

        if (currentFreelancer != null) {
            // Freelancer mode: show tasks where the freelancerIDD matches
            // Get ALL projects and collect those that match this freelancer
            List<Project> allProjects = projectService.getAllProjects();
            clientProjects = new ArrayList<>();
            for (Project p : allProjects) {
                if (p.getFreelancerid() == currentFreelancer.getIdFreelancer()) {
                    clientProjects.add(p);
                }
            }
            // Filter tasks that belong to those projects
            for (Task t : allDBTasks) {
                boolean belongs = clientProjects.stream().anyMatch(p -> p.getIdproject() == t.getProjectid());
                if (belongs)
                    tempTasks.add(t);
            }
        } else if (currentClient != null) {
            // Client mode: show tasks from projects owned by client
            clientProjects = projectService.getProjectsByClientId(currentClient.getIdClient());
            for (Task t : allDBTasks) {
                boolean belongs = clientProjects.stream().anyMatch(p -> p.getIdproject() == t.getProjectid());
                if (belongs)
                    tempTasks.add(t);
            }
        }

        allTasks.setAll(tempTasks);
        renderBoard();
    }

    private void renderBoard() {
        boardContainer.getChildren().clear();
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : allTasks) {
            if (t.getTitle().toLowerCase().contains(searchText) ||
                    (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchText))) {
                filteredTasks.add(t);
            }
        }

        if (isViewByStatus) {
            renderByStatus(filteredTasks);
        } else {
            renderByProject(filteredTasks);
        }
    }

    private void renderByStatus(List<Task> tasks) {
        // Define Columns: To Do, In Progress, Done, Blocked
        Map<String, VBox> columns = new HashMap<>();
        String[] statuses = { "A_FAIRE", "EN_COURS", "TERMINEE", "BLOCKED" };
        String[] labels = { "To Do", "In Progress", "Done", "Blocked" };
        String[] borders = { "#a89b8c", "#7db8e8", "#7ec8a4", "#e07d7d" };

        for (int i = 0; i < statuses.length; i++) {
            VBox col = createColumn(labels[i], borders[i]);
            columns.put(statuses[i], col);
            boardContainer.getChildren().add(col);
        }

        int delayCounter = 0;
        for (Task task : tasks) {
            String statusKey = task.getTaskstatus().name();
            // Map our enum to the 4 columns
            VBox targetCol = columns.get(statusKey);
            // Default mappings if enum doesn't exact match
            if (targetCol == null) {
                if (statusKey.contains("FAIRE"))
                    targetCol = columns.get("A_FAIRE");
                else if (statusKey.contains("COURS"))
                    targetCol = columns.get("EN_COURS");
                else
                    targetCol = columns.get("TERMINEE");
            }

            if (targetCol != null) {
                Node card = createCardNode(task, delayCounter++);
                targetCol.getChildren().add(card);
            }
        }
    }

    private void renderByProject(List<Task> tasks) {
        // One column per project
        Map<Integer, VBox> projectCols = new HashMap<>();

        for (Project p : clientProjects) {
            VBox col = createColumn(p.getTitle(), "#34495e");
            projectCols.put(p.getIdproject(), col);
            boardContainer.getChildren().add(col);
        }

        int delayCounter = 0;
        for (Task task : tasks) {
            VBox targetCol = projectCols.get(task.getProjectid());
            if (targetCol != null) {
                Node card = createCardNode(task, delayCounter++);
                targetCol.getChildren().add(card);
            }
        }
    }

    private VBox createColumn(String headerText, String borderColor) {
        VBox col = new VBox();
        col.setSpacing(15);
        col.setPrefWidth(280);
        col.setMinWidth(280);

        // Header
        Label headerLabel = new Label(headerText.toUpperCase());
        headerLabel.setStyle(
                "-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50; " +
                        "-fx-border-color: transparent transparent transparent " + borderColor
                        + "; -fx-border-width: 0 0 0 4px; -fx-padding: 0 0 0 10px;");

        HBox headerBox = new HBox(headerLabel);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        col.getChildren().add(headerBox);
        return col;
    }

    private Node createCardNode(Task task, int delayMultiplier) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2); -fx-cursor: hand;");

        // Priority & Status Row
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        String prioText = "→ Med";
        String prioColor = "#f39c12"; // Orange
        if (task.getPriority() == taskpriorityenum.High) {
            prioText = "↑ High";
            prioColor = "#e74c3c";
        } else if (task.getPriority() == taskpriorityenum.Low) {
            prioText = "↓ Low";
            prioColor = "#3498db";
        }

        Label priorityLabel = new Label(prioText);
        priorityLabel.setStyle("-fx-text-fill: " + prioColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusPill = new Label(task.getTaskstatus().name().replace("_", " "));
        statusPill.setStyle(
                "-fx-background-color: #f0f3f7; -fx-text-fill: #657786; -fx-padding: 3px 8px; -fx-background-radius: 10px; -fx-font-size: 10px;");

        topRow.getChildren().addAll(priorityLabel, spacer, statusPill);

        // Title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-text-fill: #14171a; -fx-font-size: 15px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        // Description (Truncated)
        String desc = task.getDescription() != null ? task.getDescription() : "";
        if (desc.length() > 60)
            desc = desc.substring(0, 57) + "...";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        // Deadline Row
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_RIGHT);

        String deadlineStr = "No Deadline";
        String dlStyle = "-fx-text-fill: #95a5a6; -fx-font-size: 11px;";
        if (task.getDeadline() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            deadlineStr = "⏱ " + dtf.format(task.getDeadline());
            if (task.getDeadline().isBefore(LocalDateTime.now()) && task.getTaskstatus() != taskstatusenum.Done) {
                dlStyle = "-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-weight: bold;";
            }
        }
        Label dlLabel = new Label(deadlineStr);
        dlLabel.setStyle(dlStyle);
        bottomRow.getChildren().add(dlLabel);

        card.getChildren().addAll(topRow, titleLabel, descLabel, bottomRow);

        // Animation
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(delayMultiplier * 35));
        ft.play();

        // Card Click Event
        card.setOnMouseClicked(e -> {
            openTaskForm(task);
        });

        return card;
    }

    @FXML
    private void handleViewToggle() {
        // View toggle is handled by the ToggleButton listener in initialize()
        // This method exists to satisfy FXML onAction reference
    }

    @FXML
    private void handleNewTask() {
        openTaskForm(null);
    }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/TaskForm.fxml"));
            Parent root = loader.load();
            TaskFormController controller = loader.getController();

            // Pass the context. On save, re-run loadData to refresh the board.
            controller.initData(task, clientProjects, this::loadData);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(task == null ? "Nouvelle Tâche" : "Modifier Tâche");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToProfile(ActionEvent event) {
        try {
            Stage stage = (Stage) boardContainer.getScene().getWindow();
            if (currentFreelancer != null) {
                // Navigate back to freelancer profile
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
                Parent root = loader.load();
                FreelancerProfileController controller = loader.getController();
                controller.setFreelancerData(currentFreelancer);
                stage.setScene(new Scene(root, 1200, 700));
            } else {
                // Navigate back to client profile
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
                Parent root = loader.load();
                ClientProfileController controller = loader.getController();
                controller.setClientData(currentClient);
                stage.setScene(new Scene(root));
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProjects() {
        try {
            if (currentFreelancer != null) {
                // Freelancer mode: navigate to projects browsing
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/profile/freelancer/freelancer-projects.fxml"));
                Parent root = loader.load();
                FreelancerProjectsController controller = loader.getController();
                controller.setFreelancerData(currentFreelancer);

                Stage stage = (Stage) boardContainer.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 700));
                stage.setTitle("Projets Disponibles - UniEarn");
            } else {
                // Client mode: navigate to client projects management
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/Projet.fxml"));
                Parent root = loader.load();
                ProjectController controller = loader.getController();
                controller.setClientData(currentClient);

                Stage stage = (Stage) boardContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseFreelancers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/list-freelancer.fxml"));
            Parent root = loader.load();
            ListFreelancersController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) boardContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) boardContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

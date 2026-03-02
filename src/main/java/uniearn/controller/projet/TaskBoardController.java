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
import uniearn.controller.profile.freelancer.FreelancerPortfolioController;
import uniearn.controller.profile.freelancer.FreelancerProfileController;
import uniearn.database.SessionManager;
import uniearn.services.users.freelancer.PortfolioService;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.projet.Task;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.model.enums.taskpriorityenum;
import uniearn.model.enums.taskstatusenum;
import uniearn.services.candidature.ApplicationService;
import uniearn.services.projet.PDFService;
import uniearn.services.projet.ProjectService;
import uniearn.services.projet.TaskAIService;
import uniearn.services.projet.TaskService;

import javafx.stage.FileChooser;
import java.io.File;

import java.io.IOException;
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

    @FXML
    private ComboBox<Project> projectSelector;

    private Client currentClient;
    private Freelancer currentFreelancer; // Freelancer mode support

    private final TaskService taskService = new TaskService();
    private final ProjectService projectService = new ProjectService();
    private final PortfolioService portfolioService = new PortfolioService();
    private final ApplicationService applicationService = new ApplicationService();
    private final TaskAIService taskAIService = new TaskAIService();
    private final PDFService pdfService = new PDFService();

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private List<Project> clientProjects = new ArrayList<>();

    private Portfolio currentPortfolio;

    private boolean isViewByStatus = true; // default view

    public void setClientData(Client client) {
        this.currentClient = client;
        loadData();
    }

    public void setFreelancerData(Freelancer freelancer) {
        this.currentFreelancer = freelancer;
        checkPortfolioStatus();
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

        projectSelector.setConverter(new javafx.util.StringConverter<Project>() {
            @Override
            public String toString(Project p) {
                return p != null ? p.getTitle() : "Tous les projets";
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        });

        projectSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            renderBoard();
        });
    }

    private void loadData() {
        List<Task> allDBTasks = taskService.getAllTasks();
        List<Task> tempTasks = new ArrayList<>();

        if (currentFreelancer != null) {
            // Freelancer mode: only show projects where the freelancer has an ACCEPTED
            // application
            clientProjects = new ArrayList<>();
            try {
                List<uniearn.model.entities.candidature.application.Application> acceptedApps = applicationService
                        .getApplicationsByFreelancer(currentFreelancer.getIdFreelancer())
                        .stream()
                        .filter(a -> a.getStatus() == uniearn.model.enums.ApplicationStatus.ACCEPTED)
                        .collect(java.util.stream.Collectors.toList());

                for (uniearn.model.entities.candidature.application.Application app : acceptedApps) {
                    Project p = projectService.getProjectById(app.getProjectId());
                    if (p != null) {
                        clientProjects.add(p);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Filter tasks that belong to those accepted projects
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
        projectSelector.setItems(FXCollections.observableArrayList(clientProjects));
        if (!clientProjects.isEmpty() && projectSelector.getValue() == null) {
            projectSelector.getSelectionModel().selectFirst();
        }
        renderBoard();
    }

    private void renderBoard() {
        if (boardContainer == null)
            return;
        boardContainer.getChildren().clear();
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        Project selectedProject = projectSelector.getValue();

        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : allTasks) {
            boolean matchesSearch = t.getTitle().toLowerCase().contains(searchText) ||
                    (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchText));

            boolean matchesProject = selectedProject == null || t.getProjectid() == selectedProject.getIdproject();

            if (matchesSearch && matchesProject) {
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
        String[] statuses = { "TODO", "InProgress", "Review", "Done" };
        String[] labels = { "To Do", "In Progress", "Review", "Done" };
        String[] borders = { "#a89b8c", "#7db8e8", "#f39c12", "#7ec8a4" };

        for (int i = 0; i < statuses.length; i++) {
            VBox col = createColumn(labels[i], borders[i], null, null);
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
                targetCol = columns.get("TODO");
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
            // Calculate progress for this project
            List<Task> projectTasks = allTasks.stream()
                    .filter(t -> t.getProjectid() == p.getIdproject())
                    .collect(java.util.stream.Collectors.toList());

            Double progress = null;
            LocalDateTime estimatedDeadline = null;
            if (!projectTasks.isEmpty()) {
                long totalTasks = projectTasks.size();
                long doneCount = projectTasks.stream()
                        .filter(t -> t.getTaskstatus() == taskstatusenum.Done)
                        .count();
                long remainingTasks = totalTasks - doneCount;
                progress = (double) doneCount / totalTasks;

                // Smart Deadline Adjustment: Today + SUM(weight of remaining tasks)
                // We use Priority as the weight variable: High=5, Medium=3, Low=1
                long totalEstimatedDays = projectTasks.stream()
                        .filter(t -> t.getTaskstatus() != taskstatusenum.Done)
                        .mapToLong(t -> {
                            if (t.getPriority() == taskpriorityenum.High)
                                return 5;
                            if (t.getPriority() == taskpriorityenum.Low)
                                return 1;
                            return 3; // Medium or default
                        })
                        .sum();

                if (totalEstimatedDays > 0) {
                    estimatedDeadline = LocalDateTime.now().plusDays(totalEstimatedDays);
                } else if (remainingTasks > 0) {
                    // Fallback
                    estimatedDeadline = LocalDateTime.now().plusDays(remainingTasks * 2);
                }
            }

            VBox col = createColumn(p.getTitle(), "#34495e", progress, estimatedDeadline);
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

    private VBox createColumn(String headerText, String borderColor, Double progress, LocalDateTime estimatedDeadline) {
        VBox col = new VBox();
        col.setSpacing(15);
        col.setPrefWidth(280);
        col.setMinWidth(280);

        // Header Label
        Label headerLabel = new Label(headerText.toUpperCase());
        headerLabel.setStyle(
                "-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50; " +
                        "-fx-border-color: transparent transparent transparent " + borderColor
                        + "; -fx-border-width: 0 0 0 4px; -fx-padding: 0 0 0 10px;");

        VBox headerContent = new VBox(8, headerLabel);

        // Add Progress UI if progress is provided (for project view)
        if (progress != null) {
            ProgressBar pb = new ProgressBar(progress);
            pb.setPrefWidth(250);
            pb.setPrefHeight(10);
            pb.setStyle(
                    "-fx-accent: #2ecc71; -fx-control-inner-background: #e1e8ed; -fx-background-radius: 5; -fx-padding: 0;");

            HBox labelsBox = new HBox();
            labelsBox.setAlignment(Pos.CENTER_LEFT);
            labelsBox.setSpacing(10);

            Label pctLabel = new Label(String.format("%.0f%% Completed", progress * 100));
            pctLabel.setStyle("-fx-text-fill: #14171a; -fx-font-size: 13px; -fx-font-weight: bold;");

            labelsBox.getChildren().add(pctLabel);

            if (estimatedDeadline != null) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");
                Label dlLabel = new Label("Deadline le  " + dtf.format(estimatedDeadline));
                dlLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 13px; -fx-font-weight: bold;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                labelsBox.getChildren().addAll(spacer, dlLabel);
            }

            headerContent.getChildren().addAll(pb, labelsBox);
        }

        HBox headerBox = new HBox(headerContent);
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

        // Delete Button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 0 0 0 15; -fx-font-weight: bold;");
        deleteBtn.setTooltip(new Tooltip("Supprimer la tâche"));

        // Hover Effect
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #c0392b; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 0 0 15; -fx-font-weight: bold;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 0 0 0 15; -fx-font-weight: bold;"));

        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            handleDeleteTask(task);
        });

        topRow.getChildren().addAll(priorityLabel, spacer, statusPill, deleteBtn);

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

    private void handleDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la tâche : " + task.getTitle());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette tâche ? Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskService.deleteTask(task.getIdtask())) {
                    loadData(); // Refresh board
                } else {
                    showErrorAlert("Erreur", "Impossible de supprimer la tâche de la base de données.");
                }
            }
        });
    }

    @FXML
    private void handleViewToggle() {
        // View toggle is handled by the ToggleButton listener in initialize()
        // This method exists to satisfy FXML onAction reference
    }

    @FXML
    private void handleFreelancerProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-projects.fxml"));
            Parent root = loader.load();
            uniearn.controller.projet.FreelancerProjectsController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            Stage stage = (Stage) boardContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Projets Disponibles - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load projects: " + e.getMessage());
        }
    }

    @FXML
    private void handleAISuggestions() {
        Project targetProject = projectSelector.getValue();
        if (targetProject == null) {
            showErrorAlert("Aucun projet", "Veuillez sélectionner un projet pour suggérer des tâches.");
            return;
        }

        // Progress indicator could be added here, but for now we'll just block briefly
        List<String> suggestions = taskAIService.suggestTasks(targetProject.getTitle(), targetProject.getDescription());

        if (suggestions.isEmpty()) {
            System.err.println("❌ AI Suggestions: No results for " + targetProject.getTitle());
            showErrorAlert("AI Error",
                    "L'IA n'a pas pu générer de suggestions. Vérifiez que votre projet a une description.");
            return;
        }

        int count = 0;
        for (String title : suggestions) {
            try {
                Task newTask = new Task();
                newTask.setTitle(title);
                newTask.setDescription("Généré par IA pour le projet: " + targetProject.getTitle());
                newTask.setProjectid(targetProject.getIdproject());
                newTask.setTaskstatus(taskstatusenum.TODO);
                newTask.setPriority(taskpriorityenum.Medium);
                newTask.setDateAssigned(LocalDateTime.now());
                newTask.setDeadline(LocalDateTime.now().plusDays(7));
                newTask.setRole("Freelancer"); // Default role

                if (taskService.addTask(newTask)) {
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count > 0) {
            loadData(); // Refresh board
            showSuccessAlert("IA Suggestions", count + " tâches ont été suggérées et ajoutées avec succès.");
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private void checkPortfolioStatus() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        currentPortfolio = portfolios.stream()
                .filter(p -> p.getFreelancerId() == currentFreelancer.getIdFreelancer())
                .findFirst()
                .orElse(null);

        updatePortfolioUI();
    }

    private void updatePortfolioUI() {
        // This is a simplified version of the profile logic
        // We could use this to enable/disable the Portfolio button or show an indicator
        System.out.println("Portfolio status updated: " + (currentPortfolio != null ? "Found" : "Not Found"));
    }

    @FXML
    private void handleViewPortfolio() {
        // Check if portfolio still exists before navigating
        if (currentPortfolio == null) {
            showErrorAlert("Error", "No portfolio found. Please create a portfolio first.");
            checkPortfolioStatus(); // Refresh UI state
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-portfolio.fxml"));
            Parent root = loader.load();

            FreelancerPortfolioController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer, currentPortfolio);

            Stage stage = (Stage) boardContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("My Portfolio - UniEarn");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load portfolio page: " + e.getMessage());
        }
    }

    /*
     * private void showSuccessAlert(String title, String message) {
     * Alert alert = new Alert(Alert.AlertType.INFORMATION);
     * alert.setTitle(title);
     * alert.setHeaderText(null);
     * alert.setContentText(message);
     * alert.showAndWait();
     * }
     */

    @FXML
    private void handleExportPDF() {
        Project targetProject = projectSelector.getValue();
        if (targetProject == null) {
            showErrorAlert("Aucun projet", "Veuillez sélectionner un projet pour exporter un rapport.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName(
                "Rapport_Projet_" + targetProject.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) boardContainer.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Filter tasks for this project
                List<Task> projectTasks = new ArrayList<>();
                for (Task t : allTasks) {
                    if (t.getProjectid() == targetProject.getIdproject()) {
                        projectTasks.add(t);
                    }
                }

                pdfService.generateProjectReport(targetProject, projectTasks, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export terminé");
                alert.setHeaderText(null);
                alert.setContentText("Le rapport PDF a été généré avec succès !");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Erreur Export", "Impossible de générer le PDF : " + e.getMessage());
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

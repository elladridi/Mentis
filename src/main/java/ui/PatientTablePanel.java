package ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.DatabaseConnection;
import services.userservice;
import javafx.scene.Node;
import java.sql.*;

public class PatientTablePanel extends VBox {

    private MentisLoginFrame parentApp;
    private TableView<PatientModel> table;
    private ObservableList<PatientModel> patientData;
    private Button backButton;

    // Color constants
    private static final Color BG_COLOR = Color.rgb(240, 245, 242);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ACCENT_GREEN_LIGHT = Color.rgb(200, 225, 210);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);

    public PatientTablePanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        setPadding(new Insets(0));
        setSpacing(0);

        initComponents();
        loadPatientsFromDatabase();
    }

    private void initComponents() {
        // Header Panel
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        headerPanel.setPadding(new Insets(30, 30, 20, 30));

        // Header Label
        Label headerLabel = new Label("Mentis - Patients");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        headerPanel.setLeft(headerLabel);

        // Back Button
        backButton = new Button("← Back to Dashboard");
        backButton.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        backButton.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        backButton.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        backButton.setOnMouseEntered(e ->
                backButton.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );
        backButton.setOnMouseExited(e ->
                backButton.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );

        backButton.setOnAction(e -> parentApp.showAdminDashboard());
        headerPanel.setRight(backButton);

        // Create Table
        createTable();

        // Add components to VBox
        getChildren().addAll(headerPanel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private void createTable() {
        table = new TableView<>();

        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 25, 0, 0, 8);" +
                        "-fx-selection-bar: #D7F5E3;" +
                        "-fx-selection-bar-non-focused: #D7F5E3;"
        );

        table.setFixedCellSize(65);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No patients found"));

        TableColumn<PatientModel, Integer> idCol = new TableColumn<>("CIN");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<PatientModel, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<PatientModel, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<PatientModel, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<PatientModel, String> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));

        TableColumn<PatientModel, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<PatientModel, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new ActionCell());

        table.getColumns().addAll(
                idCol, firstNameCol, lastNameCol, phoneCol, dobCol, emailCol, actionCol
        );

        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Platform.runLater(() -> {
                Node header = table.lookup("TableHeaderRow");

                if (header != null) {
                    header.setStyle(
                            "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                                    "-fx-background-radius: 24 24 0 0;"
                    );
                }

                table.lookupAll(".column-header").forEach(node -> node.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 18 10;"
                ));

                table.lookupAll(".column-header .label").forEach(node -> node.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                ));
            });
        });

        table.setRowFactory(tv -> new TableRow<PatientModel>() {
            @Override
            protected void updateItem(PatientModel item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(getIndex() % 2 == 0
                            ? "-fx-background-color: white;"
                            : "-fx-background-color: #F8FBF9;");
                }
            }
        });

        idCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setStyle("-fx-alignment: CENTER;");

        firstNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        lastNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        phoneCol.setStyle("-fx-alignment: CENTER-LEFT;");
        dobCol.setStyle("-fx-alignment: CENTER-LEFT;");
        emailCol.setStyle("-fx-alignment: CENTER-LEFT;");
    }    private void loadPatientsFromDatabase() {
        patientData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM user WHERE type = 'Patient'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PatientModel patient = new PatientModel(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        rs.getString("dateofbirth"),
                        rs.getString("email")
                );
                patientData.add(patient);
            }

            table.setItems(patientData);
            System.out.println("Loaded " + patientData.size() + " patients");

        } catch (SQLException e) {
            System.err.println("Error loading patients: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Error loading patients: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editPatient(PatientModel patient) {
        parentApp.showUpdatePatientDialog(
                this,
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone(),
                patient.getDob(),
                patient.getEmail()
        );
    }

    private void deletePatient(PatientModel patient) {
        String name = patient.getFirstName() + " " + patient.getLastName();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete patient: " + name + "?");

        // FIXED: getScene().getWindow() is not available in MentisLoginFrame
        // Use null or get the window from the parentApp's stage
        // Since this is a panel, we can't reliably get the owner window
        // So we'll just not set the owner

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = userservice.deleteuser(patient.getId());

                if (success) {
                    showAlert("Success", "Patient deleted successfully!", Alert.AlertType.INFORMATION);
                    refreshTable();
                } else {
                    showAlert("Error", "Failed to delete patient!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    public void refreshTable() {
        loadPatientsFromDatabase();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // alert.initOwner(parentApp.getScene().getWindow());
        alert.showAndWait();
    }

    // ================= ACTION CELL =================
    class ActionCell extends TableCell<PatientModel, Void> {
        private final HBox container;
        private final Button editButton;
        private final Button deleteButton;

        public ActionCell() {
            container = new HBox(5);
            container.setAlignment(Pos.CENTER);

            // Edit button
            editButton = new Button("✏");
            editButton.setPrefSize(35, 30);
            editButton.setStyle(
                    "-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;"
            );
            editButton.setTooltip(new Tooltip("Edit patient"));

            // Delete button
            deleteButton = new Button("🗑");
            deleteButton.setPrefSize(35, 30);
            deleteButton.setStyle(
                    "-fx-background-color: #ffcccc;" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;"
            );
            deleteButton.setTooltip(new Tooltip("Delete patient"));

            container.getChildren().addAll(editButton, deleteButton);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                PatientModel patient = getTableView().getItems().get(getIndex());

                // Set button actions
                editButton.setOnAction(e -> editPatient(patient));
                deleteButton.setOnAction(e -> deletePatient(patient));

                setGraphic(container);
            }
        }
    }

    // ================= PATIENT MODEL =================
    public static class PatientModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty dob;
        private final SimpleStringProperty email;

        public PatientModel(int id, String firstName, String lastName,
                            String phone, String dob, String email) {
            this.id = new SimpleIntegerProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.phone = new SimpleStringProperty(phone);
            this.dob = new SimpleStringProperty(dob);
            this.email = new SimpleStringProperty(email);
        }

        public int getId() { return id.get(); }
        public String getFirstName() { return firstName.get(); }
        public String getLastName() { return lastName.get(); }
        public String getPhone() { return phone.get(); }
        public String getDob() { return dob.get(); }
        public String getEmail() { return email.get(); }

        public void setId(int id) { this.id.set(id); }
        public void setFirstName(String firstName) { this.firstName.set(firstName); }
        public void setLastName(String lastName) { this.lastName.set(lastName); }
        public void setPhone(String phone) { this.phone.set(phone); }
        public void setDob(String dob) { this.dob.set(dob); }
        public void setEmail(String email) { this.email.set(email); }

        // Property getters for TableView
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty firstNameProperty() { return firstName; }
        public SimpleStringProperty lastNameProperty() { return lastName; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty dobProperty() { return dob; }
        public SimpleStringProperty emailProperty() { return email; }
    }

    // SimpleIntegerProperty wrapper
    public static class SimpleIntegerProperty extends javafx.beans.property.SimpleIntegerProperty {
        public SimpleIntegerProperty(int value) { super(value); }
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}
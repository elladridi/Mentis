package ui;

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

import java.sql.*;

public class PsychologistTablePanel extends VBox {

    private MentisLoginFrame parentApp;  // FIXED: Changed from MentisLoginFrame to MentisLoginFrame
    private TableView<PsychologistModel> table;
    private ObservableList<PsychologistModel> psychologistData;
    private Button addButton;
    private Button backButton;

    // Color constants
    private static final Color BG_COLOR = Color.rgb(240, 245, 242);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ACCENT_GREEN_LIGHT = Color.rgb(200, 225, 210);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);

    public PsychologistTablePanel(MentisLoginFrame parentApp) {  // FIXED: Parameter type
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        setPadding(new Insets(0));
        setSpacing(0);

        initComponents();
        loadPsychologistsFromDatabase();
    }

    private void initComponents() {
        // Header Panel
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        headerPanel.setPadding(new Insets(30, 30, 20, 30));

        // Header Label
        Label headerLabel = new Label("Mentis - Psychologists");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        headerPanel.setLeft(headerLabel);

        // Button Panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        // Back Button
        backButton = createBackButton();

        // Add Psychologist Button
        addButton = createAddButton();
        addButton.setOnAction(e -> showAddPsychologistDialog());

        buttonPanel.getChildren().addAll(backButton, addButton);
        headerPanel.setRight(buttonPanel);

        // Create Table
        createTable();

        // Add components to VBox
        getChildren().addAll(headerPanel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private Button createBackButton() {
        Button button = new Button("← Back to Dashboard");
        button.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        button.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnAction(e -> parentApp.showAdminDashboard());
        return button;
    }

    private Button createAddButton() {
        Button button = new Button("Add psychologist");
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 30;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 30;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 30;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void createTable() {
        table = new TableView<>();
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 2;"
        );
        // FIXED: setRowHeight doesn't exist in JavaFX TableView
        table.setFixedCellSize(40);  // Use setFixedCellSize instead of setRowHeight
        table.setPlaceholder(new Label("No psychologists found"));

        // Define columns
        TableColumn<PsychologistModel, Integer> idCol = new TableColumn<>("CIN");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<PsychologistModel, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(150);

        TableColumn<PsychologistModel, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(150);

        TableColumn<PsychologistModel, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<PsychologistModel, String> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        dobCol.setPrefWidth(150);

        TableColumn<PsychologistModel, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<PsychologistModel, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new ActionCell());

        table.getColumns().addAll(idCol, firstNameCol, lastNameCol, phoneCol, dobCol, emailCol, actionCol);

        // Style table header
        table.getColumns().forEach(col -> {
            col.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: CENTER;"
            );
        });

        // Center align specific columns
        idCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setStyle("-fx-alignment: CENTER;");

        // Left align text columns
        firstNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        lastNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        phoneCol.setStyle("-fx-alignment: CENTER-LEFT;");
        dobCol.setStyle("-fx-alignment: CENTER-LEFT;");
        emailCol.setStyle("-fx-alignment: CENTER-LEFT;");
    }

    private void loadPsychologistsFromDatabase() {
        psychologistData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM user WHERE type = 'psychologist'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PsychologistModel psychologist = new PsychologistModel(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        rs.getString("dateofbirth"),
                        rs.getString("email")
                );
                psychologistData.add(psychologist);
            }

            table.setItems(psychologistData);
            System.out.println("Loaded " + psychologistData.size() + " psychologists");

        } catch (SQLException e) {
            System.err.println("Error loading psychologists: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Error loading psychologists: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAddPsychologistDialog() {
        parentApp.showAddPsychologistDialog(this);
    }

    private void editPsychologist(PsychologistModel psychologist) {
        parentApp.showUpdatePsychologistDialog(
                this,
                psychologist.getId(),
                psychologist.getFirstName(),
                psychologist.getLastName(),
                psychologist.getPhone(),
                psychologist.getDob(),
                psychologist.getEmail()
        );
    }

    private void deletePsychologist(PsychologistModel psychologist) {
        String name = psychologist.getFirstName() + " " + psychologist.getLastName();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete psychologist: " + name + "?");

        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // confirm.initOwner(parentApp.getScene().getWindow());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = userservice.deleteuser(psychologist.getId());

                if (success) {
                    showAlert("Success", "Psychologist deleted successfully!", Alert.AlertType.INFORMATION);
                    refreshTable();
                } else {
                    showAlert("Error", "Failed to delete psychologist!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    public void refreshTable() {
        loadPsychologistsFromDatabase();
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
    class ActionCell extends TableCell<PsychologistModel, Void> {
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
            editButton.setTooltip(new Tooltip("Edit psychologist"));

            // Delete button
            deleteButton = new Button("🗑");
            deleteButton.setPrefSize(35, 30);
            deleteButton.setStyle(
                    "-fx-background-color: #ffcccc;" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;"
            );
            deleteButton.setTooltip(new Tooltip("Delete psychologist"));

            container.getChildren().addAll(editButton, deleteButton);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                PsychologistModel psychologist = getTableView().getItems().get(getIndex());

                // Set button actions
                editButton.setOnAction(e -> editPsychologist(psychologist));
                deleteButton.setOnAction(e -> deletePsychologist(psychologist));

                setGraphic(container);
            }
        }
    }

    // ================= PSYCHOLOGIST MODEL =================
    public static class PsychologistModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty dob;
        private final SimpleStringProperty email;

        public PsychologistModel(int id, String firstName, String lastName,
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

    // SimpleStringProperty wrapper
    public static class SimpleStringProperty extends javafx.beans.property.SimpleStringProperty {
        public SimpleStringProperty(String value) { super(value); }
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}
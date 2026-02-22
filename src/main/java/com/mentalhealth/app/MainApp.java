package com.mentalhealth.app;

import com.mentalhealth.app.controllers.EventController;
import com.mentalhealth.app.utils.DatabaseConnection;
import com.mentalhealth.app.views.ComponentFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BorderPane root;
    private BorderPane contentArea;
    private VBox sidebar;
    private String activeMenu = "Events";

    @Override
    public void start(Stage primaryStage) {
        DatabaseConnection.getConnection();

        root = new BorderPane();
        root.setStyle("-fx-background-color: #FFFFFF;");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());

        contentArea = new BorderPane();
        contentArea.setStyle("-fx-background-color: #FFFFFF;");
        root.setCenter(contentArea);

        loadEventsView();

        Scene scene = new Scene(root, 1300, 750);
        primaryStage.setTitle("MENTIS - Mental Health Platform");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #DDE5E2; -fx-border-width: 0 0 1 0;");

        // Logo
        ImageView logoImage = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream(
                    "/com/mentalhealth/app/images/logo.png"));
            logoImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }
        logoImage.setFitHeight(45);
        logoImage.setPreserveRatio(true);
        logoImage.setPickOnBounds(true);
        logoImage.setCursor(Cursor.HAND);

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);

        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Search the platform...");
        searchBar.setPrefWidth(300);
        searchBar.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #1E1E1E;" +
                "-fx-prompt-text-fill: #9CA3AF; -fx-background-radius: 20;" +
                "-fx-padding: 8 15; -fx-font-size: 13px;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 20;");

        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.ALWAYS);

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 20px;");
        Label userName = new Label("Admin User");
        userName.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 13px;");
        HBox userArea = new HBox(8, userIcon, userName);
        userArea.setAlignment(Pos.CENTER);

        header.getChildren().addAll(logoImage, s1, searchBar, s2, userArea);
        return header;
    }

    private VBox buildSidebar() {
        sidebar = new VBox(5);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #2F5D52; -fx-border-width: 0 1 0 0;");

        Label menuTitle = new Label("NAVIGATION");
        menuTitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;" +
                "-fx-font-weight: bold; -fx-padding: 0 0 10 10;");
        sidebar.getChildren().add(menuTitle);

        String[][] items = {
                {"📊", "Dashboard"},
                {"📅", "Bookings"},
                {"📝", "Assessments"},
                {"😊", "Mood Tracker"},
                {"🎯", "Goals"},
                {"💭", "Reflect"},
                {"📚", "Content"},
                {"📌", "Events"},
                {"⚙️", "Settings"}
        };

        for (String[] item : items) {
            Button btn = buildMenuButton(item[0], item[1]);
            if (item[1].equals("Events")) btn.setStyle(activeStyle());
            btn.setOnAction(e -> {
                activeMenu = item[1];
                refreshMenuStyles();
                switch (item[1]) {
                    case "Dashboard" -> loadFXMLView("/fxml/HomeView.fxml", "Dashboard");
                    case "Mood Tracker" -> loadFXMLView("/fxml/MoodView.fxml", "Mood Tracker");
                    case "Goals" -> loadFXMLView("/fxml/GoalView.fxml", "Goals");
                    case "Reflect" -> loadFXMLView("/fxml/ReflectView.fxml", "Reflect");
                    case "Events" -> loadEventsView();
                    case "Assessments" -> loadUIPanel("assessment");
                    case "Bookings" -> loadUIPanel("sessions");
                    case "Content" -> loadUIPanel("content");
                    default -> loadPlaceholder(item[0] + " " + item[1]);
                }
            });
            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #DDE5E2;");
        Label ver = new Label("v1.0.0");
        ver.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 0 0 0 10;");
        sidebar.getChildren().addAll(spacer, sep, ver);

        return sidebar;
    }

    private Button buildMenuButton(String icon, String text) {
        Button btn = new Button(icon + "  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(inactiveStyle());
        btn.setOnMouseEntered(e -> {
            if (!activeMenu.equals(text)) btn.setStyle(hoverStyle());
        });
        btn.setOnMouseExited(e -> {
            if (!activeMenu.equals(text)) btn.setStyle(inactiveStyle());
        });
        return btn;
    }

    private void refreshMenuStyles() {
        for (int i = 1; i < sidebar.getChildren().size(); i++) {
            if (sidebar.getChildren().get(i) instanceof Button btn) {
                btn.setStyle(btn.getText().contains(activeMenu) ?
                        activeStyle() : inactiveStyle());
            }
        }
    }

    private String activeStyle() {
        return "-fx-background-color: rgba(155,199,181,0.25);" +
                "-fx-text-fill: #2F5D52; -fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10; -fx-padding: 12 15; -fx-cursor: hand;" +
                "-fx-border-color: #2F5D52; -fx-border-width: 0 0 0 3;" +
                "-fx-border-radius: 10;";
    }

    private String inactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #6B7280;" +
                "-fx-font-size: 14px; -fx-background-radius: 10;" +
                "-fx-padding: 12 15; -fx-cursor: hand;";
    }

    private String hoverStyle() {
        return "-fx-background-color: rgba(155,199,181,0.12);" +
                "-fx-text-fill: #1E1E1E; -fx-font-size: 14px;" +
                "-fx-background-radius: 10; -fx-padding: 12 15; -fx-cursor: hand;";
    }

    // =================== VIEW LOADERS ===================

    /**
     * Load YOUR Events module
     */
    private void loadEventsView() {
        EventController controller = new EventController();
        contentArea.setCenter(controller.getView());
    }

    /**
     * Load teammate's FXML views (Mood, Goals, Reflect, Dashboard)
     */
    private void loadFXMLView(String fxmlPath, String name) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.setCenter(view);
        } catch (Exception e) {
            System.err.println("Error loading " + name + ": " + e.getMessage());
            e.printStackTrace();
            loadErrorView(name, e.getMessage());
        }
    }

    /**
     * Load teammate's UI panels (Assessments, Sessions, Content)
     * These are Java Swing/JavaFX programmatic panels
     */
    private void loadUIPanel(String panelType) {
        try {
            Node panel = null;
            switch (panelType) {
                case "assessment" -> {
                    // Try to load AssessmentPanel from ui package
                    Class<?> clazz = Class.forName("ui.AssessmentPanel");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    if (instance instanceof Node) {
                        panel = (Node) instance;
                    }
                }
                case "sessions" -> {
                    Class<?> clazz = Class.forName("ui.SessionPanel");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    if (instance instanceof Node) {
                        panel = (Node) instance;
                    }
                }
                case "content" -> {
                    Class<?> clazz = Class.forName("ui.ContentUploadPanel");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    if (instance instanceof Node) {
                        panel = (Node) instance;
                    }
                }
            }

            if (panel != null) {
                ScrollPane sp = new ScrollPane(panel);
                sp.setFitToWidth(true);
                sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
                contentArea.setCenter(sp);
            } else {
                loadPlaceholder("📋 " + panelType);
            }
        } catch (Exception e) {
            System.err.println("Error loading panel " + panelType + ": " + e.getMessage());
            e.printStackTrace();
            loadErrorView(panelType, e.getMessage());
        }
    }

    /**
     * Placeholder for modules not yet connected
     */
    private void loadPlaceholder(String pageName) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #FFFFFF;");

        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 60px;");
        Label title = ComponentFactory.pageTitle(pageName);
        Label sub = new Label(
                "This section is under development.\n" +
                        "Events Management is fully functional!");
        sub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        sub.setTextAlignment(TextAlignment.CENTER);

        Button goBtn = ComponentFactory.styledButton("📌 Go to Events", "#9BC7B5");
        goBtn.setOnAction(e -> {
            activeMenu = "Events";
            refreshMenuStyles();
            loadEventsView();
        });

        box.getChildren().addAll(icon, title, sub, goBtn);
        contentArea.setCenter(box);
    }

    /**
     * Error view when a module fails to load
     */
    private void loadErrorView(String moduleName, String errorMsg) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #FFFFFF;");

        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 50px;");

        Label title = new Label("Could not load: " + moduleName);
        title.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label error = new Label(errorMsg != null ? errorMsg : "Unknown error");
        error.setStyle("-fx-text-fill: #D62828; -fx-font-size: 13px;");
        error.setWrapText(true);
        error.setMaxWidth(500);

        Label hint = new Label(
                "This module may need additional setup.\n" +
                        "Check the console for detailed error messages.");
        hint.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        hint.setTextAlignment(TextAlignment.CENTER);

        Button backBtn = ComponentFactory.styledButton("📌 Go to Events", "#9BC7B5");
        backBtn.setOnAction(e -> {
            activeMenu = "Events";
            refreshMenuStyles();
            loadEventsView();
        });

        box.getChildren().addAll(icon, title, error, hint, backBtn);
        contentArea.setCenter(box);
    }

    @Override
    public void stop() { DatabaseConnection.closeConnection(); }

    public static void main(String[] args) { launch(args); }
}
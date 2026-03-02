package com.mentalhealth.app.controllers;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.utils.UserSession;
import com.mentalhealth.app.views.ComponentFactory;
import com.mentalhealth.app.views.EventFormView;
import com.mentalhealth.app.views.EventListView;
import com.mentalhealth.app.views.StatisticsView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class EventController {

    private final EventListView listView = new EventListView();
    private BorderPane mainContent;
    private FlowPane eventsGrid;
    private RegistrationController registrationController;
    private final UserSession session = UserSession.getInstance();

    // Tab buttons
    private Button eventsTabBtn;
    private Button statisticsTabBtn;

    public BorderPane getView() {
        mainContent = new BorderPane();
        mainContent.setStyle("-fx-background-color: #FFFFFF;");
        registrationController = new RegistrationController(
                mainContent, this::showEventsList);
        showEventsList();
        return mainContent;
    }

    // =================== TAB BAR ===================

    private HBox buildTabBar() {
        HBox tabBar = new HBox(0);
        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.setPadding(new Insets(0, 0, 20, 0));

        eventsTabBtn = createTabButton("📌 Events", true);

        tabBar.getChildren().add(eventsTabBtn);

        eventsTabBtn.setOnAction(e -> {
            setActiveTab(eventsTabBtn);
            showEventsList();
        });

        // Only admin and psychologist can see statistics
        if (session.canManageEvents()) {
            statisticsTabBtn = createTabButton("📊 Statistics", false);
            statisticsTabBtn.setOnAction(e -> {
                setActiveTab(statisticsTabBtn);
                showStatistics();
            });
            tabBar.getChildren().add(statisticsTabBtn);
        }

        return tabBar;
    }

    private Button createTabButton(String text, boolean isActive) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setPadding(new Insets(10, 25, 10, 25));
        btn.setStyle(isActive ? activeTabStyle() : inactiveTabStyle());

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().equals(activeTabStyle())) {
                btn.setStyle(hoverTabStyle());
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().equals(activeTabStyle())) {
                btn.setStyle(inactiveTabStyle());
            }
        });

        return btn;
    }

    private void setActiveTab(Button activeBtn) {
        eventsTabBtn.setStyle(inactiveTabStyle());
        if (statisticsTabBtn != null) statisticsTabBtn.setStyle(inactiveTabStyle());
        activeBtn.setStyle(activeTabStyle());
    }

    private String activeTabStyle() {
        return "-fx-background-color: #2F5D52; -fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10 10 0 0; -fx-cursor: hand;" +
                "-fx-border-color: #2F5D52; -fx-border-width: 0 0 3 0;";
    }

    private String inactiveTabStyle() {
        return "-fx-background-color: #F1F6F4; -fx-text-fill: #6B7280;" +
                "-fx-font-size: 14px; -fx-background-radius: 10 10 0 0;" +
                "-fx-cursor: hand; -fx-border-color: #DDE5E2; -fx-border-width: 0 0 1 0;";
    }

    private String hoverTabStyle() {
        return "-fx-background-color: #E8F0ED; -fx-text-fill: #2F5D52;" +
                "-fx-font-size: 14px; -fx-background-radius: 10 10 0 0;" +
                "-fx-cursor: hand; -fx-border-color: #9BC7B5; -fx-border-width: 0 0 2 0;";
    }

    // =================== STATISTICS VIEW ===================

    private void showStatistics() {
        VBox container = new VBox(0);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #FFFFFF;");

        Label title = ComponentFactory.pageTitle("📌 Events Management");

        HBox tabBar = buildTabBar();
        setActiveTab(statisticsTabBtn);

        StatisticsView statisticsView = new StatisticsView();
        ScrollPane statsContent = statisticsView.buildStatisticsView();
        statsContent.setPadding(new Insets(0));
        VBox.setVgrow(statsContent, Priority.ALWAYS);

        container.getChildren().addAll(title, tabBar, statsContent);
        mainContent.setCenter(container);
        mainContent.setRight(null);
    }

    // =================== LIST (READ ALL) ===================

    private void showEventsList() {
        VBox container = new VBox(0);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #FFFFFF;");

        // Page title - different for patients
        String titleText = session.isPatient() ? "📌 Browse Events" : "📌 Events Management";
        Label title = ComponentFactory.pageTitle(titleText);

        HBox tabBar = buildTabBar();
        setActiveTab(eventsTabBtn);

        // Controls row
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(0, 0, 20, 0));

        TextField searchField = ComponentFactory.styledTextField("🔍 Search events...");
        searchField.setPrefWidth(250);
        searchField.setStyle(
                "-fx-background-color: #F1F6F4; -fx-text-fill: #1E1E1E;" +
                        "-fx-prompt-text-fill: #9CA3AF; -fx-background-radius: 20;" +
                        "-fx-padding: 10 15; -fx-font-size: 13px;" +
                        "-fx-border-color: #DDE5E2; -fx-border-radius: 20;");

        ComboBox<String> filterType = new ComboBox<>();
        filterType.getItems().addAll(
                "All Types", "WORKSHOP", "GROUP_THERAPY", "SEMINAR", "SOCIAL");
        filterType.setValue("All Types");
        filterType.setStyle(
                "-fx-background-color: #F1F6F4; -fx-font-size: 13px; -fx-background-radius: 10;" +
                        "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(searchField, filterType, spacer);

        // Only admin and psychologist can add events
        if (session.canManageEvents()) {
            Button addBtn = ComponentFactory.styledButton("+ Add Event", "#9BC7B5");
            addBtn.setOnAction(e -> showAddForm());
            controls.getChildren().add(addBtn);
        }

        // Stats bar - only for admin/psychologist
        HBox stats = null;
        if (session.canManageEvents()) {
            stats = listView.buildStatsBar(
                    Event.count(), Event.totalParticipants(),
                    EventRegistration.totalCount(), EventRegistration.totalRevenue());
        }

        // Events grid
        eventsGrid = new FlowPane(20, 20);
        eventsGrid.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(eventsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadCards(null, null);

        // Search and filter listeners
        searchField.textProperty().addListener((obs, o, n) ->
                loadCards(n.isEmpty() ? null : n,
                        filterType.getValue().equals("All Types") ? null : filterType.getValue()));
        filterType.setOnAction(e ->
                loadCards(searchField.getText().isEmpty() ? null : searchField.getText(),
                        filterType.getValue().equals("All Types") ? null : filterType.getValue()));

        container.getChildren().addAll(title, tabBar, controls);
        if (stats != null) container.getChildren().add(stats);
        container.getChildren().add(scrollPane);

        mainContent.setCenter(container);
        mainContent.setRight(null);
    }

    private void loadCards(String keyword, String type) {
        eventsGrid.getChildren().clear();

        List<Event> events;
        if (keyword != null && !keyword.isEmpty()) {
            events = Event.search(keyword);
        } else if (type != null) {
            events = Event.findByType(type);
        } else {
            events = Event.findAll();
        }

        if (events.isEmpty()) {
            eventsGrid.getChildren().add(listView.buildEmptyState());
            return;
        }

        for (Event event : events) {
            int regCount = EventRegistration.countByEvent(event.getId());
            double revenue = EventRegistration.revenueByEvent(event.getId());

            VBox card;
            if (session.isPatient()) {
                // Patient view - simplified card with Register button only
                card = listView.buildCard(event, regCount, revenue,
                        () -> registrationController.showEventDetail(event),
                        null,  // no edit
                        null,  // no delete
                        () -> registrationController.showPatientQuickRegister(event));
            } else {
                // Admin/Psychologist view - full CRUD
                card = listView.buildCard(event, regCount, revenue,
                        () -> registrationController.showEventDetail(event),
                        () -> showEditForm(event),
                        () -> handleDelete(event),
                        () -> registrationController.showRegistrationPanel(event));
            }
            eventsGrid.getChildren().add(card);
        }
    }

    // =================== ADD (CREATE) ===================

    private void showAddForm() {
        if (!session.canManageEvents()) return;

        EventFormView formView = new EventFormView();
        VBox form = formView.buildForm(null,
                () -> {
                    String err = formView.validate();
                    if (err != null) { formView.showError(err); return; }
                    Event event = formView.createEventFromForm();
                    event.save();
                    showEventsList();
                },
                this::showEventsList);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== EDIT (UPDATE) ===================

    private void showEditForm(Event event) {
        if (!session.canManageEvents()) return;

        EventFormView formView = new EventFormView();
        VBox form = formView.buildForm(event,
                () -> {
                    String err = formView.validate();
                    if (err != null) { formView.showError(err); return; }
                    formView.fillEventFromForm(event);
                    event.update();
                    showEventsList();
                },
                this::showEventsList);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== DELETE ===================

    private void handleDelete(Event event) {
        if (!session.canManageEvents()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete \"" + event.getTitle() + "\"?");
        confirm.setContentText("This will also delete all registrations for this event.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            event.delete();
            showEventsList();
        }
    }
}
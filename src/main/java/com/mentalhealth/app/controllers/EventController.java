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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EventController {

    private final EventListView listView = new EventListView();
    private final UserSession session = UserSession.getInstance();

    private BorderPane mainContent;
    private FlowPane eventsGrid;
    private RegistrationController registrationController;

    private Button eventsTabBtn;
    private Button statisticsTabBtn;

    private TextField searchField;
    private ComboBox<String> filterType;

    public BorderPane getView() {
        mainContent = new BorderPane();
        mainContent.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);"
        );

        registrationController = new RegistrationController(mainContent, this::showEventsList);
        showEventsList();

        return mainContent;
    }

    // =================== TAB BAR ===================

    private HBox buildTabBar() {
        HBox tabBar = new HBox(8);
        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.setPadding(new Insets(0, 0, 22, 0));

        eventsTabBtn = createTabButton("Events", true);
        eventsTabBtn.setOnAction(e -> {
            setActiveTab(eventsTabBtn);
            showEventsList();
        });

        tabBar.getChildren().add(eventsTabBtn);

        if (session.canManageEvents()) {
            statisticsTabBtn = createTabButton("Statistics", false);
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
        btn.setPrefHeight(42);
        btn.setPadding(new Insets(10, 24, 10, 24));
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
        if (eventsTabBtn != null) eventsTabBtn.setStyle(inactiveTabStyle());
        if (statisticsTabBtn != null) statisticsTabBtn.setStyle(inactiveTabStyle());
        if (activeBtn != null) activeBtn.setStyle(activeTabStyle());
    }

    private String activeTabStyle() {
        return "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 999;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.28), 14, 0, 0, 5);";
    }

    private String inactiveTabStyle() {
        return "-fx-background-color: white;" +
                "-fx-text-fill: #2D3748;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 999;" +
                "-fx-border-color: #DDE5E2;" +
                "-fx-border-radius: 999;" +
                "-fx-cursor: hand;";
    }

    private String hoverTabStyle() {
        return "-fx-background-color: #F1F8E9;" +
                "-fx-text-fill: #2E7D32;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 999;" +
                "-fx-border-color: #9BC7B5;" +
                "-fx-border-radius: 999;" +
                "-fx-cursor: hand;";
    }

    // =================== STATISTICS VIEW ===================

    private void showStatistics() {
        VBox container = createPageContainer();

        Label title = ComponentFactory.pageTitle("Events Management");
        Label subtitle = subtitle(
                "Track event performance, registrations, participation, and revenue in real time."
        );

        HBox tabBar = buildTabBar();
        setActiveTab(statisticsTabBtn);

        StatisticsView statisticsView = new StatisticsView();
        ScrollPane statsContent = statisticsView.buildStatisticsView();
        statsContent.setPadding(new Insets(0));
        statsContent.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        VBox.setVgrow(statsContent, Priority.ALWAYS);

        container.getChildren().addAll(title, subtitle, tabBar, statsContent);

        mainContent.setCenter(container);
        mainContent.setRight(null);
    }

    // =================== LIST ===================

    private void showEventsList() {
        VBox container = createPageContainer();

        String titleText = session.isPatient() ? "Browse Events" : "Events Management";
        String subtitleText = session.isPatient()
                ? "Explore mental health workshops, therapy groups, seminars, and wellness events."
                : "Create, manage, monitor, and synchronize events across Mentis web and desktop platforms.";

        Label title = ComponentFactory.pageTitle(titleText);
        Label subtitle = subtitle(subtitleText);

        HBox tabBar = buildTabBar();
        setActiveTab(eventsTabBtn);

        HBox controls = buildControls();

        HBox stats = null;
        if (session.canManageEvents()) {
            stats = listView.buildStatsBar(
                    Event.count(),
                    Event.totalParticipants(),
                    EventRegistration.totalCount(),
                    EventRegistration.totalRevenue()
            );
        }

        eventsGrid = new FlowPane(22, 22);
        eventsGrid.setPadding(new Insets(12, 2, 20, 2));
        eventsGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(eventsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadCards(null, null);

        container.getChildren().addAll(title, subtitle, tabBar, controls);
        if (stats != null) {
            container.getChildren().add(stats);
        }
        container.getChildren().add(scrollPane);

        mainContent.setCenter(container);
        mainContent.setRight(null);
    }

    private VBox createPageContainer() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(30, 34, 34, 34));
        container.setStyle("-fx-background-color: transparent;");
        return container;
    }

    private Label subtitle(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(
                "-fx-text-fill: #6C757D;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 0 6 0;"
        );
        return label;
    }

    private HBox buildControls() {
        HBox controls = new HBox(14);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(12, 0, 12, 0));

        searchField = ComponentFactory.styledTextField("Search events by title, description, or location...");
        searchField.setPrefWidth(360);

        filterType = new ComboBox<>();
        filterType.getItems().addAll("All Types", "WORKSHOP", "GROUP_THERAPY", "SEMINAR", "SOCIAL");
        filterType.setValue("All Types");
        filterType.setPrefHeight(44);
        filterType.setStyle(comboBoxStyle());

        Button refreshBtn = ComponentFactory.styledButton("Refresh", "#50C878");
        refreshBtn.setOnAction(e -> loadCards(getSearchKeyword(), getSelectedType()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(searchField, filterType, refreshBtn, spacer);

        if (session.canManageEvents()) {
            Button addBtn = ComponentFactory.styledButton("+ Add Event", "#2E7D32");
            addBtn.setOnAction(e -> showAddForm());
            controls.getChildren().add(addBtn);
        }

        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                loadCards(getSearchKeyword(), getSelectedType())
        );

        filterType.setOnAction(e ->
                loadCards(getSearchKeyword(), getSelectedType())
        );

        return controls;
    }

    private String comboBoxStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #DDE5E2;" +
                "-fx-border-radius: 16;" +
                "-fx-padding: 6 12;" +
                "-fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);";
    }

    private String getSearchKeyword() {
        if (searchField == null || searchField.getText() == null) return null;
        String keyword = searchField.getText().trim();
        return keyword.isEmpty() ? null : keyword;
    }

    private String getSelectedType() {
        if (filterType == null || filterType.getValue() == null) return null;
        return "All Types".equals(filterType.getValue()) ? null : filterType.getValue();
    }

    private void loadCards(String keyword, String type) {
        if (eventsGrid == null) return;

        eventsGrid.getChildren().clear();

        List<Event> events;

        if (keyword != null && !keyword.isEmpty()) {
            events = Event.search(keyword);
        } else if (type != null && !type.isEmpty()) {
            events = Event.findByType(type);
        } else {
            events = Event.findAll();
        }

        if (events.isEmpty()) {
            eventsGrid.getChildren().add(listView.buildEmptyState());
            return;
        }

        for (Event event : events) {
            syncEventParticipants(event);

            int regCount = EventRegistration.countByEvent(event.getId());
            double revenue = EventRegistration.revenueByEvent(event.getId());

            VBox card;

            if (session.isPatient()) {
                card = listView.buildCard(
                        event,
                        regCount,
                        revenue,
                        () -> registrationController.showEventDetail(Event.findById(event.getId())),
                        null,
                        null,
                        () -> registrationController.showPatientQuickRegister(Event.findById(event.getId()))
                );
            } else {
                card = listView.buildCard(
                        event,
                        regCount,
                        revenue,
                        () -> registrationController.showEventDetail(Event.findById(event.getId())),
                        () -> showEditForm(Event.findById(event.getId())),
                        () -> handleDelete(Event.findById(event.getId())),
                        () -> registrationController.showRegistrationPanel(Event.findById(event.getId()))
                );
            }

            eventsGrid.getChildren().add(card);
        }
    }

    private void syncEventParticipants(Event event) {
        if (event == null) return;

        int tickets = EventRegistration.ticketsByEvent(event.getId());

        if (tickets != event.getCurrentParticipants()) {
            event.setCurrentParticipants(tickets);
            event.update();
        }

        if (event.getDateTime() != null && event.getDateTime().isBefore(LocalDateTime.now())) {
            if ("UPCOMING".equalsIgnoreCase(event.getStatus())) {
                event.setStatus("COMPLETED");
                event.update();
            }
        }
    }

    // =================== ADD ===================

    private void showAddForm() {
        if (!session.canManageEvents()) return;

        EventFormView formView = new EventFormView();

        VBox form = formView.buildForm(
                null,
                () -> {
                    String err = formView.validate();
                    if (err != null) {
                        formView.showError(err);
                        return;
                    }

                    Event event = formView.createEventFromForm();
                    event.setCreatedBy(session.getUserId());
                    boolean saved = event.save();

                    if (!saved) {
                        showAlert(Alert.AlertType.ERROR, "Event Not Saved", "Could not save this event. Please check the database fields.");
                        return;
                    }

                    showEventsList();
                },
                this::showEventsList
        );

        showForm(form);
    }

    // =================== EDIT ===================

    private void showEditForm(Event event) {
        if (!session.canManageEvents() || event == null) return;

        EventFormView formView = new EventFormView();

        VBox form = formView.buildForm(
                event,
                () -> {
                    String err = formView.validate();
                    if (err != null) {
                        formView.showError(err);
                        return;
                    }

                    formView.fillEventFromForm(event);
                    boolean updated = event.update();

                    if (!updated) {
                        showAlert(Alert.AlertType.ERROR, "Event Not Updated", "Could not update this event.");
                        return;
                    }

                    showEventsList();
                },
                this::showEventsList
        );

        showForm(form);
    }

    private void showForm(VBox form) {
        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== DELETE ===================

    private void handleDelete(Event event) {
        if (!session.canManageEvents() || event == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete \"" + event.getTitle() + "\"?");
        confirm.setContentText(
                "This will also delete all registrations linked to this event if cascade is enabled.\n\n" +
                        "Continue?"
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = event.delete();

            if (!deleted) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Delete Failed",
                        "Could not delete this event. Check if event_registrations has ON DELETE CASCADE."
                );
                return;
            }

            showEventsList();
        }
    }

    // =================== HELPERS ===================

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
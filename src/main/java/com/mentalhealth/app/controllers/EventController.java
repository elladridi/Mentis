package com.mentalhealth.app.controllers;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.views.ComponentFactory;
import com.mentalhealth.app.views.EventFormView;
import com.mentalhealth.app.views.EventListView;
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

    public BorderPane getView() {
        mainContent = new BorderPane();
        mainContent.setStyle("-fx-background-color: #FFFFFF;");
        registrationController = new RegistrationController(
                mainContent, this::showEventsList);
        showEventsList();
        return mainContent;
    }

    // =================== LIST (READ ALL) ===================

    private void showEventsList() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #FFFFFF;");

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = ComponentFactory.pageTitle("📌 Events Management");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

        Button addBtn = ComponentFactory.styledButton("+ Add Event", "#9BC7B5");
        addBtn.setOnAction(e -> showAddForm());
        header.getChildren().addAll(title, spacer, searchField, filterType, addBtn);

        HBox stats = listView.buildStatsBar(
                Event.count(), Event.totalParticipants(),
                EventRegistration.totalCount(), EventRegistration.totalRevenue());

        eventsGrid = new FlowPane(20, 20);
        eventsGrid.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(eventsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadCards(null, null);

        searchField.textProperty().addListener((obs, o, n) ->
                loadCards(n.isEmpty() ? null : n,
                        filterType.getValue().equals("All Types") ?
                                null : filterType.getValue()));
        filterType.setOnAction(e ->
                loadCards(searchField.getText().isEmpty() ?
                                null : searchField.getText(),
                        filterType.getValue().equals("All Types") ?
                                null : filterType.getValue()));

        container.getChildren().addAll(header, stats, scrollPane);
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

            VBox card = listView.buildCard(event, regCount, revenue,
                    () -> registrationController.showEventDetail(event),
                    () -> showEditForm(event),
                    () -> handleDelete(event),
                    () -> registrationController.showRegistrationPanel(event));
            eventsGrid.getChildren().add(card);
        }
    }

    // =================== ADD (CREATE) ===================

    private void showAddForm() {
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete \"" + event.getTitle() + "\"?");
        confirm.setContentText(
                "This will also delete all registrations for this event.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            event.delete();
            showEventsList();
        }
    }
}
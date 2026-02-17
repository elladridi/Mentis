package com.mentalhealth.app.controllers;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.views.ComponentFactory;
import com.mentalhealth.app.views.EventDetailView;
import com.mentalhealth.app.views.RegistrationFormView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class RegistrationController {

    private final BorderPane mainContent;
    private final Runnable onBackToList;

    public RegistrationController(BorderPane mainContent, Runnable onBackToList) {
        this.mainContent = mainContent;
        this.onBackToList = onBackToList;
    }

    // =================== EVENT DETAIL (READ) ===================

    public void showEventDetail(Event event) {
        EventDetailView detailView = new EventDetailView();

        int regCount = EventRegistration.countByEvent(event.getId());
        int totalTickets = EventRegistration.ticketsByEvent(event.getId());
        double revenue = EventRegistration.revenueByEvent(event.getId());
        List<EventRegistration> registrations =
                EventRegistration.findByEvent(event.getId());

        VBox detail = detailView.buildDetail(event, regCount, totalTickets,
                revenue, registrations,
                onBackToList,
                () -> showAddForm(event),
                reg -> showEditForm(event, reg),
                reg -> handleDelete(event, reg));

        ScrollPane sp = new ScrollPane(detail);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== ADD REGISTRATION (CREATE) ===================

    public void showAddForm(Event event) {
        RegistrationFormView formView = new RegistrationFormView();

        VBox form = formView.buildForm(event, null,
                () -> {
                    String err = formView.validate();
                    if (err != null) { formView.showError(err); return; }

                    EventRegistration reg = formView.createFromForm(event.getId());
                    if (!reg.save()) {
                        formView.showError(
                                "❌ Could not register. Email may already be registered.");
                        return;
                    }
                    event.setCurrentParticipants(
                            event.getCurrentParticipants() + reg.getNumberOfTickets());
                    event.update();
                    showEventDetail(event);
                },
                () -> showEventDetail(event));

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== EDIT REGISTRATION (UPDATE) ===================

    public void showEditForm(Event event, EventRegistration reg) {
        RegistrationFormView formView = new RegistrationFormView();

        VBox form = formView.buildForm(event, reg,
                () -> {
                    String err = formView.validate();
                    if (err != null) { formView.showError(err); return; }

                    formView.fillFromForm(reg);
                    reg.update();
                    showEventDetail(event);
                },
                () -> showEventDetail(event));

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== DELETE REGISTRATION (DELETE) ===================

    public void handleDelete(Event event, EventRegistration reg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Registration");
        confirm.setHeaderText("Delete registration for " + reg.getUserName() + "?");
        confirm.setContentText("Ticket: " + reg.getTicketType() +
                " × " + reg.getNumberOfTickets() +
                (reg.isFreeTicket() ? " (Free)" :
                        String.format(" ($%.2f)", reg.getTotalPrice())));
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reg.isConfirmed()) {
                event.setCurrentParticipants(
                        Math.max(0, event.getCurrentParticipants() -
                                reg.getNumberOfTickets()));
                event.update();
            }
            reg.delete();
            showEventDetail(event);
        }
    }

    // =================== SIDE PANEL ===================

    public void showRegistrationPanel(Event event) {
        EventDetailView detailView = new EventDetailView();

        VBox panel = new VBox(15);
        panel.setPrefWidth(400);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #2F5D52; -fx-border-width: 0 0 0 1;");

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280;" +
                "-fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> mainContent.setRight(null));

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label panelTitle = new Label("🎟 Registrations: " + event.getTitle());
        panelTitle.setStyle(
                "-fx-text-fill: #1E1E1E; -fx-font-size: 16px; -fx-font-weight: bold;");
        panelTitle.setWrapText(true);
        panelTitle.setMaxWidth(300);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(panelTitle, spacer, closeBtn);

        int regCount = EventRegistration.countByEvent(event.getId());
        double revenue = EventRegistration.revenueByEvent(event.getId());

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        Label regLbl = new Label("🎟 " + regCount + " bookings");
        regLbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label revLbl = new Label(String.format("💵 $%.2f", revenue));
        revLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        statsRow.getChildren().addAll(regLbl, revLbl);

        Button addBtn = ComponentFactory.styledButton("+ New Registration", "#9BC7B5");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> showAddForm(event));
        if (!event.isAvailable()) {
            addBtn.setDisable(true);
            addBtn.setText("🔴 SOLD OUT");
        }

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #DDE5E2;");

        panel.getChildren().addAll(topRow, statsRow, addBtn, separator);

        List<EventRegistration> registrations =
                EventRegistration.findByEvent(event.getId());
        if (registrations.isEmpty()) {
            Label noReg = new Label("No registrations yet.");
            noReg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
            panel.getChildren().add(noReg);
        } else {
            for (EventRegistration reg : registrations) {
                panel.getChildren().add(detailView.buildRegistrationCard(reg,
                        r -> showEditForm(event, r),
                        r -> {
                            handleDelete(event, r);
                            showRegistrationPanel(event);
                        }));
            }
        }

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(420);
        scrollPane.setStyle("-fx-background: #F1F6F4; -fx-background-color: #F1F6F4;");
        mainContent.setRight(scrollPane);
    }
}
package com.mentalhealth.app.controllers;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.services.EmailService;
import com.mentalhealth.app.services.PDFService;
import com.mentalhealth.app.services.QRCodeService;
import com.mentalhealth.app.utils.UserSession;
import com.mentalhealth.app.views.ComponentFactory;
import com.mentalhealth.app.views.EventDetailView;
import com.mentalhealth.app.views.RegistrationFormView;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RegistrationController {

    private final BorderPane mainContent;
    private final Runnable onBackToList;
    private final UserSession session = UserSession.getInstance();

    public RegistrationController(BorderPane mainContent, Runnable onBackToList) {
        this.mainContent = mainContent;
        this.onBackToList = onBackToList;
    }

    // =================== EVENT DETAIL ===================

    public void showEventDetail(Event event) {
        if (event == null) return;

        final Event finalEvent = Event.findById(event.getId());

        if (finalEvent == null) return;

        syncEventParticipants(finalEvent);

        EventDetailView detailView = new EventDetailView();

        int regCount = EventRegistration.countByEvent(finalEvent.getId());
        int totalTickets = EventRegistration.ticketsByEvent(finalEvent.getId());
        double revenue = EventRegistration.revenueByEvent(finalEvent.getId());
        List<EventRegistration> registrations = EventRegistration.findByEvent(finalEvent.getId());

        VBox detail = detailView.buildDetail(
                finalEvent,
                regCount,
                totalTickets,
                revenue,
                registrations,
                onBackToList,
                () -> showAddForm(finalEvent),
                reg -> showEditForm(finalEvent, reg),
                reg -> handleDelete(finalEvent, reg)
        );

        if (session.canManageEvents()) {
            HBox exportButtons = new HBox(10);
            exportButtons.setAlignment(Pos.CENTER_LEFT);
            exportButtons.setPadding(new Insets(10, 30, 20, 30));

            Button exportReportBtn = ComponentFactory.styledButton("Export Event Report", "#2E7D32");
            exportReportBtn.setOnAction(e -> exportEventReport(finalEvent, registrations));

            exportButtons.getChildren().add(exportReportBtn);
            detail.getChildren().add(1, exportButtons);
        }

        if (session.isPatient()) {
            EventRegistration myReg = findPatientRegistration(registrations, session.getUserEmail());

            if (myReg != null) {
                VBox ticketCard = buildPatientRegistrationCard(finalEvent, myReg);
                detail.getChildren().add(ticketCard);
            }
        }

        ScrollPane sp = new ScrollPane(detail);
        sp.setFitToWidth(true);
        sp.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    private EventRegistration findPatientRegistration(List<EventRegistration> registrations, String email) {
        if (email == null || email.trim().isEmpty()) return null;

        for (EventRegistration reg : registrations) {
            if (reg.getEmail() != null && reg.getEmail().equalsIgnoreCase(email.trim())) {
                return reg;
            }
        }

        return null;
    }

    // =================== PATIENT TICKET CARD ===================

    private VBox buildPatientRegistrationCard(Event event, EventRegistration reg) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F6FBF7);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 26;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.12), 22, 0, 0, 8);"
        );

        Label title = ComponentFactory.sectionTitle("Your Event Ticket");

        HBox content = new HBox(26);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox qrBox = buildQrBox(event, reg);

        VBox details = new VBox(9);
        details.setAlignment(Pos.TOP_LEFT);

        Label confirmation = label(
                "Confirmation: " + reg.getFormattedConfirmationNumber(),
                "#2E7D32",
                16,
                true
        );

        Label status = label(
                reg.getStatusEmoji() + " Status: " + safe(reg.getStatus(), "N/A"),
                getStatusColor(reg.getStatus()),
                14,
                true
        );

        Label user = label("Name: " + safe(reg.getUserName(), "N/A"), "#2D3748", 13, false);
        Label email = label("Email: " + safe(reg.getEmail(), "N/A"), "#6C757D", 13, false);
        Label ticket = label(
                "Ticket: " + safe(reg.getTicketType(), "STANDARD") +
                        " x " + reg.getNumberOfTickets(),
                "#2D3748",
                13,
                false
        );

        Label price = label(
                reg.isFreeTicket()
                        ? "Total: Free"
                        : String.format("Total: $%.2f", reg.getTotalPrice()),
                "#2E7D32",
                14,
                true
        );

        details.getChildren().addAll(confirmation, status, user, email, ticket, price);
        content.getChildren().addAll(qrBox, details);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button downloadPDFBtn = ComponentFactory.smallButton("Download PDF", "#2E7D32");
        downloadPDFBtn.setOnAction(e -> downloadTicketPdf(event, reg));

        Button saveQRBtn = ComponentFactory.smallButton("Save QR", "#50C878");
        saveQRBtn.setOnAction(e -> saveQrCode(event, reg));

        Button resendEmailBtn = ComponentFactory.smallButton("Resend Email", "#6C757D");
        resendEmailBtn.setOnAction(e -> sendConfirmationEmail(resendEmailBtn, event, reg));

        Button cancelBtn = ComponentFactory.smallButton("Cancel Registration", "#D62828");
        cancelBtn.setOnAction(e -> cancelPatientRegistration(event, reg));

        actions.getChildren().addAll(downloadPDFBtn, saveQRBtn, resendEmailBtn, cancelBtn);

        card.getChildren().addAll(title, content, actions);
        return card;
    }

    private VBox buildQrBox(Event event, EventRegistration reg) {
        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new Insets(18));
        qrBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 20;"
        );

        try {
            Image qrImage = QRCodeService.generateQRCodeFXImage(reg, event);

            if (qrImage != null) {
                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(155);
                qrView.setFitHeight(155);
                qrView.setPreserveRatio(true);
                qrBox.getChildren().add(qrView);
            } else {
                qrBox.getChildren().add(label("QR unavailable", "#9CA3AF", 12, false));
            }

        } catch (Exception e) {
            qrBox.getChildren().add(label("QR unavailable", "#9CA3AF", 12, false));
        }

        qrBox.getChildren().add(label("Present at entrance", "#6C757D", 11, false));
        return qrBox;
    }

    // =================== PATIENT QUICK REGISTER ===================

    public void showPatientQuickRegister(Event event) {
        if (event == null) return;

        event = Event.findById(event.getId());
        if (event == null) return;

        syncEventParticipants(event);

        if (!session.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to register for events.");
            return;
        }

        if (!event.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Event Full", "Sorry, this event is no longer available.");
            return;
        }

        String userEmail = session.getUserEmail();

        if (userEmail != null && !userEmail.trim().isEmpty()) {
            List<EventRegistration> existing = EventRegistration.findByEvent(event.getId());

            for (EventRegistration reg : existing) {
                if (reg.getEmail() != null && reg.getEmail().equalsIgnoreCase(userEmail.trim())) {
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Already Registered",
                            "You are already registered for this event.\n\n" +
                                    "Confirmation: " + reg.getFormattedConfirmationNumber() + "\n" +
                                    "Status: " + reg.getStatus()
                    );
                    return;
                }
            }
        }

        showPatientRegistrationDialog(event);
    }

    private void showPatientRegistrationDialog(Event event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register for Event");
        dialog.setHeaderText(null);

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: white;");

        Label title = label(event.getTitle(), "#1A3C34", 22, true);
        title.setWrapText(true);

        Label subtitle = label("Complete your registration details below.", "#6C757D", 13, false);

        VBox infoBox = new VBox(12);
        infoBox.setPadding(new Insets(18));
        infoBox.setStyle(
                "-fx-background-color: #F6FBF7;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 18;"
        );

        TextField nameField = ComponentFactory.styledTextField("Full name");
        nameField.setText(session.getUserName() != null ? session.getUserName() : "");

        TextField emailField = ComponentFactory.styledTextField("Email");
        emailField.setText(session.getUserEmail() != null ? session.getUserEmail() : "");

        TextField phoneField = ComponentFactory.styledTextField("Phone");
        phoneField.setText(session.getUserPhone() != null ? session.getUserPhone() : "");

        infoBox.getChildren().addAll(
                ComponentFactory.fieldGroup("Name", nameField),
                ComponentFactory.fieldGroup("Email", emailField),
                ComponentFactory.fieldGroup("Phone", phoneField)
        );

        ComboBox<String> ticketType = new ComboBox<>();
        ticketType.getItems().addAll("STANDARD", "VIP", "EARLY_BIRD", "GROUP");
        ticketType.setValue("STANDARD");
        ticketType.setPrefHeight(42);
        ticketType.setStyle(formControlStyle());

        int maxTickets = Math.max(1, Math.min(10, event.getAvailableSpots()));
        Spinner<Integer> ticketSpinner = new Spinner<>(1, maxTickets, 1);
        ticketSpinner.setPrefWidth(120);

        Label priceLabel = label("", "#2E7D32", 15, true);

        Runnable updatePrice = () -> {
            double total = calculatePrice(event, ticketType.getValue(), ticketSpinner.getValue());
            priceLabel.setText(event.isFree() ? "Total: Free" : String.format("Total: $%.2f", total));
        };

        updatePrice.run();
        ticketType.setOnAction(e -> updatePrice.run());
        ticketSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePrice.run());

        TextArea specialRequests = new TextArea();
        specialRequests.setPromptText("Accessibility needs, notes, or special requests...");
        specialRequests.setPrefRowCount(3);
        specialRequests.setWrapText(true);
        specialRequests.setStyle(
                "-fx-control-inner-background: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 14;" +
                        "-fx-font-size: 13px;"
        );

        Label error = label("", "#D62828", 13, true);
        error.setVisible(false);

        VBox ticketBox = new VBox(12);
        ticketBox.setPadding(new Insets(18));
        ticketBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 18;"
        );

        HBox ticketRow = new HBox(14);
        ticketRow.setAlignment(Pos.CENTER_LEFT);
        ticketRow.getChildren().addAll(
                ComponentFactory.fieldGroup("Ticket Type", ticketType),
                ComponentFactory.fieldGroup("Quantity", ticketSpinner)
        );

        ticketBox.getChildren().addAll(ticketRow, priceLabel);

        content.getChildren().addAll(
                title,
                subtitle,
                infoBox,
                ticketBox,
                ComponentFactory.fieldGroup("Special Requests", specialRequests),
                error
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Confirm Registration");
        ok.setStyle(
                "-fx-background-color: #2E7D32;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;"
        );

        ok.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (nameField.getText().trim().isEmpty()) {
                error.setText("Name is required.");
                error.setVisible(true);
                e.consume();
                return;
            }

            if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
                error.setText("A valid email is required.");
                error.setVisible(true);
                e.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            EventRegistration reg = new EventRegistration();

            reg.setEventId(event.getId());
            reg.setUserId(session.getUserId());
            reg.setUserName(nameField.getText().trim());
            reg.setEmail(emailField.getText().trim());
            reg.setPhone(phoneField.getText().trim());
            reg.setTicketType(ticketType.getValue());
            reg.setNumberOfTickets(ticketSpinner.getValue());
            reg.setStatus("CONFIRMED");
            reg.setPaymentMethod(event.isFree() ? "FREE" : "PENDING");
            reg.setSpecialRequests(specialRequests.getText());
            reg.setRegistrationDate(LocalDateTime.now());
            reg.setUpdatedAt(LocalDateTime.now());
            reg.setTotalPrice(calculatePrice(event, ticketType.getValue(), ticketSpinner.getValue()));

            boolean saved = reg.save();

            if (!saved) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration Failed",
                        "Could not complete registration. This email may already be registered for this event."
                );
                return;
            }

            syncEventParticipants(event);
            showRegistrationConfirmation(event, reg);
        }
    }

    // =================== ADMIN ADD REGISTRATION ===================

    public void showAddForm(Event event) {
        if (event == null) return;

        RegistrationFormView formView = new RegistrationFormView();

        VBox form = formView.buildForm(
                event,
                null,
                () -> {
                    String err = formView.validate();

                    if (err != null) {
                        formView.showError(err);
                        return;
                    }

                    EventRegistration reg = formView.createFromForm(event.getId());
                    reg.setUpdatedAt(LocalDateTime.now());

                    if (!reg.save()) {
                        formView.showError("Could not register. Email may already be registered.");
                        return;
                    }

                    syncEventParticipants(event);
                    showRegistrationConfirmation(event, reg);
                },
                () -> showEventDetail(event)
        );

        showForm(form);
    }

    // =================== CONFIRMATION ===================

    private void showRegistrationConfirmation(Event event, EventRegistration reg) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registration Successful");
        dialog.setHeaderText(null);

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(480);

        Label success = label("Registration Confirmed", "#2E7D32", 22, true);
        Label confirmation = label(reg.getFormattedConfirmationNumber(), "#50C878", 16, true);

        VBox details = new VBox(7);
        details.setPadding(new Insets(16));
        details.setStyle(
                "-fx-background-color: #F6FBF7;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 18;"
        );

        details.getChildren().addAll(
                label("Name: " + safe(reg.getUserName(), "N/A"), "#2D3748", 13, false),
                label("Email: " + safe(reg.getEmail(), "N/A"), "#6C757D", 13, false),
                label("Ticket: " + reg.getTicketType() + " x " + reg.getNumberOfTickets(), "#2D3748", 13, false),
                label(reg.isFreeTicket() ? "Total: Free" : String.format("Total: $%.2f", reg.getTotalPrice()), "#2E7D32", 14, true)
        );

        VBox qrBox = buildQrBox(event, reg);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button sendEmail = ComponentFactory.smallButton("Send Email", "#50C878");
        sendEmail.setOnAction(e -> sendConfirmationEmail(sendEmail, event, reg));

        Button downloadPdf = ComponentFactory.smallButton("Download PDF", "#2E7D32");
        downloadPdf.setOnAction(e -> downloadTicketPdf(event, reg));

        Button saveQr = ComponentFactory.smallButton("Save QR", "#6C757D");
        saveQr.setOnAction(e -> saveQrCode(event, reg));

        actions.getChildren().addAll(sendEmail, downloadPdf, saveQr);

        content.getChildren().addAll(success, confirmation, details, qrBox, actions);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();

        showEventDetail(Event.findById(event.getId()));
    }

    // =================== EDIT REGISTRATION ===================

    public void showEditForm(Event event, EventRegistration reg) {
        if (event == null || reg == null) return;

        RegistrationFormView formView = new RegistrationFormView();

        VBox form = formView.buildForm(
                event,
                reg,
                () -> {
                    String err = formView.validate();

                    if (err != null) {
                        formView.showError(err);
                        return;
                    }

                    formView.fillFromForm(reg);
                    reg.setUpdatedAt(LocalDateTime.now());

                    boolean updated = reg.update();

                    if (!updated) {
                        formView.showError("Could not update registration.");
                        return;
                    }

                    syncEventParticipants(event);
                    showEventDetail(Event.findById(event.getId()));
                },
                () -> showEventDetail(event)
        );

        HBox ticketActions = new HBox(10);
        ticketActions.setAlignment(Pos.CENTER_LEFT);
        ticketActions.setPadding(new Insets(10, 0, 0, 0));

        Button viewQR = ComponentFactory.smallButton("View QR", "#50C878");
        viewQR.setOnAction(e -> showQRCodeDialog(event, reg));

        Button pdf = ComponentFactory.smallButton("Download PDF", "#2E7D32");
        pdf.setOnAction(e -> downloadTicketPdf(event, reg));

        Button email = ComponentFactory.smallButton("Resend Email", "#6C757D");
        email.setOnAction(e -> sendConfirmationEmail(email, event, reg));

        ticketActions.getChildren().addAll(viewQR, pdf, email);
        form.getChildren().add(2, ticketActions);

        showForm(form);
    }

    // =================== QR DIALOG ===================

    private void showQRCodeDialog(Event event, EventRegistration reg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR Code - " + safe(reg.getUserName(), "Registration"));

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        VBox qrBox = buildQrBox(event, reg);
        Label confirmation = label(reg.getFormattedConfirmationNumber(), "#2E7D32", 15, true);

        Button save = ComponentFactory.styledButton("Save QR Code", "#50C878");
        save.setOnAction(e -> {
            saveQrCode(event, reg);
            save.setText("Saved");
        });

        content.getChildren().addAll(qrBox, confirmation, save);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // =================== REPORT ===================

    private void exportEventReport(Event event, List<EventRegistration> registrations) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder to save Event Report");

        Stage stage = (Stage) mainContent.getScene().getWindow();
        File dir = chooser.showDialog(stage);

        if (dir == null) return;

        File pdf = PDFService.generateEventReportPDF(event, registrations, dir.getAbsolutePath());

        if (pdf != null) {
            showAlert(Alert.AlertType.INFORMATION, "Report Exported", "Event report saved to:\n" + pdf.getAbsolutePath());
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to generate the event report.");
        }
    }

    // =================== DELETE ===================

    public void handleDelete(Event event, EventRegistration reg) {
        if (event == null || reg == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Registration");
        confirm.setHeaderText("Delete registration for " + reg.getUserName() + "?");
        confirm.setContentText(
                "Ticket: " + reg.getTicketType() +
                        " x " + reg.getNumberOfTickets() +
                        (reg.isFreeTicket() ? " (Free)" : String.format(" ($%.2f)", reg.getTotalPrice()))
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = reg.delete();

            if (!deleted) {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete this registration.");
                return;
            }

            syncEventParticipants(event);
            showEventDetail(Event.findById(event.getId()));
        }
    }

    private void cancelPatientRegistration(Event event, EventRegistration reg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Registration");
        confirm.setHeaderText("Cancel your registration?");
        confirm.setContentText("This action will remove your ticket from the event.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reg.delete()) {
                syncEventParticipants(event);
                showAlert(Alert.AlertType.INFORMATION, "Registration Cancelled", "Your registration has been cancelled.");
                onBackToList.run();
            }
        }
    }

    // =================== SIDE PANEL ===================

    public void showRegistrationPanel(Event event) {
        if (event == null) return;

        final Event finalEvent = Event.findById(event.getId());
        if (finalEvent == null) return;

        syncEventParticipants(finalEvent);

        EventDetailView detailView = new EventDetailView();

        VBox panel = new VBox(16);
        panel.setPrefWidth(430);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F6FBF7);" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-width: 0 0 0 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 18, 0, 0, 6);"
        );

        Button close = new Button("x");
        close.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6C757D;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;"
        );
        close.setOnAction(e -> mainContent.setRight(null));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = label("Registrations", "#1A3C34", 19, true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(title, spacer, close);

        Label eventTitle = label(finalEvent.getTitle(), "#6C757D", 13, false);
        eventTitle.setWrapText(true);

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                miniPill(EventRegistration.countByEvent(finalEvent.getId()) + " bookings", "#E8F5E9", "#2E7D32"),
                miniPill(String.format("$%.2f revenue", EventRegistration.revenueByEvent(finalEvent.getId())), "#FFF7E6", "#F39C12")
        );

        Button add = ComponentFactory.styledButton("+ New Registration", "#2E7D32");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(e -> showAddForm(finalEvent));

        if (!finalEvent.isAvailable()) {
            add.setDisable(true);
            add.setText("Sold Out");
        }

        panel.getChildren().addAll(top, eventTitle, stats, add, new Separator());

        List<EventRegistration> regs = EventRegistration.findByEvent(finalEvent.getId());

        if (regs.isEmpty()) {
            Label empty = label("No registrations yet.", "#9CA3AF", 13, false);
            panel.getChildren().add(empty);
        } else {
            for (EventRegistration reg : regs) {
                panel.getChildren().add(
                        detailView.buildRegistrationCard(
                                reg,
                                r -> showEditForm(finalEvent, r),
                                r -> {
                                    handleDelete(finalEvent, r);
                                    showRegistrationPanel(Event.findById(finalEvent.getId()));
                                }
                        )
                );
            }
        }

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(450);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        mainContent.setRight(scrollPane);
    }

    // =================== HELPERS ===================

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

    private void syncEventParticipants(Event event) {
        if (event == null) return;

        int tickets = EventRegistration.ticketsByEvent(event.getId());

        if (tickets != event.getCurrentParticipants()) {
            event.setCurrentParticipants(tickets);
            event.update();
        }
    }

    private double calculatePrice(Event event, String ticketType, int quantity) {
        if (event == null || event.isFree()) return 0.0;

        double multiplier = switch (ticketType == null ? "STANDARD" : ticketType) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };

        return event.getPrice() * quantity * multiplier;
    }

    private void downloadTicketPdf(Event event, EventRegistration reg) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder to save PDF");

        Stage stage = (Stage) mainContent.getScene().getWindow();
        File dir = chooser.showDialog(stage);

        if (dir == null) return;

        File pdf = PDFService.generateTicketPDF(reg, event, dir.getAbsolutePath());

        if (pdf != null) {
            showAlert(Alert.AlertType.INFORMATION, "PDF Saved", "Ticket saved to:\n" + pdf.getAbsolutePath());
        } else {
            showAlert(Alert.AlertType.ERROR, "PDF Failed", "Could not generate the PDF ticket.");
        }
    }

    private void saveQrCode(Event event, EventRegistration reg) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder to save QR Code");

        Stage stage = (Stage) mainContent.getScene().getWindow();
        File dir = chooser.showDialog(stage);

        if (dir == null) return;

        File qr = QRCodeService.saveQRCodeToFile(reg, event, dir.getAbsolutePath());

        if (qr != null) {
            reg.setQrCodePath(qr.getAbsolutePath());
            reg.update();
            showAlert(Alert.AlertType.INFORMATION, "QR Saved", "QR code saved to:\n" + qr.getAbsolutePath());
        } else {
            showAlert(Alert.AlertType.ERROR, "QR Failed", "Could not save the QR code.");
        }
    }

    private void sendConfirmationEmail(Button button, Event event, EventRegistration reg) {
        button.setDisable(true);
        button.setText("Sending...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return EmailService.sendConfirmationEmail(reg, event);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                button.setText("Sent");
            } else {
                button.setText("Failed");
                button.setDisable(false);
            }
        });

        new Thread(task).start();
    }

    private Label label(String text, String color, int size, boolean bold) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;" +
                        (bold ? "-fx-font-weight: bold;" : "")
        );
        return lbl;
    }

    private Label miniPill(String text, String bg, String color) {
        Label pill = new Label(text);
        pill.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        return pill;
    }

    private String getStatusColor(String status) {
        if (status == null) return "#9CA3AF";

        return switch (status) {
            case "CONFIRMED" -> "#2E7D32";
            case "PENDING" -> "#F39C12";
            case "CANCELLED" -> "#D62828";
            default -> "#9CA3AF";
        };
    }

    private String formControlStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #DDE5E2;" +
                "-fx-border-radius: 14;" +
                "-fx-padding: 8 12;" +
                "-fx-font-size: 13px;";
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
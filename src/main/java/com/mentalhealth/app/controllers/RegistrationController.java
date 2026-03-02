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

    public RegistrationController(BorderPane mainContent, Runnable onBackToList) {
        this.mainContent = mainContent;
        this.onBackToList = onBackToList;
    }

    // =================== EVENT DETAIL (READ) ===================

    public void showEventDetail(Event event) {
        UserSession session = UserSession.getInstance();
        EventDetailView detailView = new EventDetailView();

        int regCount = EventRegistration.countByEvent(event.getId());
        int totalTickets = EventRegistration.ticketsByEvent(event.getId());
        double revenue = EventRegistration.revenueByEvent(event.getId());
        List<EventRegistration> registrations = EventRegistration.findByEvent(event.getId());

        VBox detail = detailView.buildDetail(event, regCount, totalTickets,
                revenue, registrations,
                onBackToList,
                () -> showAddForm(event),
                reg -> showEditForm(event, reg),
                reg -> handleDelete(event, reg));

        // Add export button ONLY for admin/psychologist
        if (session.canManageEvents()) {
            HBox exportButtons = new HBox(10);
            exportButtons.setAlignment(Pos.CENTER_LEFT);
            exportButtons.setPadding(new Insets(10, 30, 20, 30));

            Button exportReportBtn = ComponentFactory.styledButton("📄 Export Event Report (PDF)", "#3E6F64");
            exportReportBtn.setOnAction(e -> exportEventReport(event, registrations));

            exportButtons.getChildren().add(exportReportBtn);
            detail.getChildren().add(1, exportButtons);
        }

        // For PATIENT: show their registration details with QR code
        if (session.isPatient()) {
            EventRegistration myReg = findPatientRegistration(registrations, session.getUserEmail());
            if (myReg != null) {
                VBox myRegCard = buildPatientRegistrationCard(event, myReg);
                detail.getChildren().add(myRegCard);
            }
        }

        ScrollPane sp = new ScrollPane(detail);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== FIND PATIENT REGISTRATION ===================

    private EventRegistration findPatientRegistration(List<EventRegistration> registrations, String email) {
        if (email == null || email.isEmpty()) return null;
        for (EventRegistration reg : registrations) {
            if (email.equals(reg.getEmail())) {
                return reg;
            }
        }
        return null;
    }

    // =================== PATIENT REGISTRATION CARD WITH QR ===================

    private VBox buildPatientRegistrationCard(Event event, EventRegistration reg) {
        VBox card = ComponentFactory.darkCard();
        card.getChildren().add(ComponentFactory.sectionTitle("🎫 Your Ticket"));

        HBox content = new HBox(30);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));

        // QR Code
        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new Insets(15));
        qrBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;");

        try {
            Image qrImage = QRCodeService.generateQRCodeFXImage(reg, event);
            if (qrImage != null) {
                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(150);
                qrView.setFitHeight(150);
                qrBox.getChildren().add(qrView);
            } else {
                Label noQR = new Label("📱 QR Code");
                noQR.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
                qrBox.getChildren().add(noQR);
            }
        } catch (Exception e) {
            System.err.println("Error generating QR: " + e.getMessage());
            Label noQR = new Label("📱 QR Code unavailable");
            noQR.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
            qrBox.getChildren().add(noQR);
        }

        Label qrHint = new Label("Present at entrance");
        qrHint.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        qrBox.getChildren().add(qrHint);

        // Registration details
        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.TOP_LEFT);

        Label confirmLabel = new Label("🎟 Confirmation #: REG-" + String.format("%06d", reg.getId()));
        confirmLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 16px; -fx-font-weight: bold;");

        String statusColor = switch (reg.getStatus()) {
            case "CONFIRMED" -> "#3E6F64";
            case "PENDING" -> "#AFCFC2";
            case "CANCELLED" -> "#D62828";
            default -> "#9CA3AF";
        };
        Label statusLabel = new Label(reg.getStatusEmoji() + " Status: " + reg.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label nameLabel = new Label("👤 " + reg.getUserName());
        nameLabel.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 14px;");

        Label emailLabel = new Label("📧 " + reg.getEmail());
        emailLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        Label ticketLabel = new Label("🎫 " + reg.getTicketType() + " × " + reg.getNumberOfTickets() + " ticket(s)");
        ticketLabel.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 14px;");

        Label priceLabel = new Label(reg.isFreeTicket() ? "🆓 Free" : String.format("💰 Total: $%.2f", reg.getTotalPrice()));
        priceLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 14px; -fx-font-weight: bold;");

        detailsBox.getChildren().addAll(confirmLabel, statusLabel, nameLabel, emailLabel, ticketLabel, priceLabel);

        content.getChildren().addAll(qrBox, detailsBox);
        card.getChildren().add(content);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button downloadPDFBtn = ComponentFactory.smallButton("📄 Download Ticket (PDF)", "#3E6F64");
        downloadPDFBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to save PDF");
            Stage stage = (Stage) mainContent.getScene().getWindow();
            File dir = chooser.showDialog(stage);
            if (dir != null) {
                File pdf = PDFService.generateTicketPDF(reg, event, dir.getAbsolutePath());
                if (pdf != null) {
                    showAlert(Alert.AlertType.INFORMATION, "PDF Saved",
                            "Your ticket has been saved to:\n" + pdf.getAbsolutePath());
                }
            }
        });

        Button saveQRBtn = ComponentFactory.smallButton("💾 Save QR Code", "#9BC7B5");
        saveQRBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to save QR Code");
            Stage stage = (Stage) mainContent.getScene().getWindow();
            File dir = chooser.showDialog(stage);
            if (dir != null) {
                File qrFile = QRCodeService.saveQRCodeToFile(reg, event, dir.getAbsolutePath());
                if (qrFile != null) {
                    showAlert(Alert.AlertType.INFORMATION, "QR Code Saved",
                            "QR code saved to:\n" + qrFile.getAbsolutePath());
                }
            }
        });

        Button resendEmailBtn = ComponentFactory.smallButton("📧 Resend Email", "#6B7280");
        resendEmailBtn.setOnAction(e -> {
            resendEmailBtn.setText("⏳ Sending...");
            resendEmailBtn.setDisable(true);
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return EmailService.sendConfirmationEmail(reg, event);
                }
            };
            task.setOnSucceeded(ev -> {
                if (task.getValue()) {
                    resendEmailBtn.setText("✅ Sent!");
                } else {
                    resendEmailBtn.setText("❌ Failed");
                    resendEmailBtn.setDisable(false);
                }
            });
            new Thread(task).start();
        });

        Button cancelRegBtn = ComponentFactory.smallButton("❌ Cancel Registration", "#D62828");
        cancelRegBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Registration");
            confirm.setHeaderText("Cancel your registration?");
            confirm.setContentText("Are you sure you want to cancel your registration for this event?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                event.setCurrentParticipants(
                        Math.max(0, event.getCurrentParticipants() - reg.getNumberOfTickets()));
                event.update();
                reg.delete();
                showAlert(Alert.AlertType.INFORMATION, "Registration Cancelled",
                        "Your registration has been cancelled.");
                onBackToList.run();
            }
        });

        actions.getChildren().addAll(downloadPDFBtn, saveQRBtn, resendEmailBtn, cancelRegBtn);
        card.getChildren().add(actions);

        return card;
    }

    // =================== PATIENT QUICK REGISTER ===================

    public void showPatientQuickRegister(Event event) {
        UserSession session = UserSession.getInstance();

        // DEBUG
        System.out.println("========== DEBUG: Patient Quick Register ==========");
        System.out.println("UserId: " + session.getUserId());
        System.out.println("UserName: " + session.getUserName());
        System.out.println("UserEmail: '" + session.getUserEmail() + "'");
        System.out.println("UserPhone: '" + session.getUserPhone() + "'");
        System.out.println("UserType: " + session.getUserType());
        System.out.println("IsLoggedIn: " + session.isLoggedIn());
        System.out.println("IsPatient: " + session.isPatient());
        System.out.println("===================================================");

        // Check if user is logged in
        if (!session.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In",
                    "Please log in to register for events.");
            return;
        }

        if (!event.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Event Full",
                    "Sorry, this event is sold out!");
            return;
        }

        // Check if patient is already registered
        String userEmail = session.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            List<EventRegistration> existing = EventRegistration.findByEvent(event.getId());
            for (EventRegistration reg : existing) {
                if (reg.getEmail() != null && reg.getEmail().equalsIgnoreCase(userEmail)) {
                    showAlert(Alert.AlertType.INFORMATION, "Already Registered",
                            "You are already registered for this event!\n\n" +
                                    "🎟 Confirmation #: REG-" + String.format("%06d", reg.getId()) + "\n" +
                                    "📊 Status: " + reg.getStatus() + "\n\n" +
                                    "Click 'View' to see your ticket and QR code.");
                    return;
                }
            }
        }

        // Show registration dialog with pre-filled info
        showPatientRegistrationDialog(event);
    }

    // =================== PATIENT REGISTRATION DIALOG ===================

    private void showPatientRegistrationDialog(Event event) {
        UserSession session = UserSession.getInstance();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register for Event");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(450);

        // Event info
        Label eventTitle = new Label("📌 " + event.getTitle());
        eventTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2F5D52;");
        eventTitle.setWrapText(true);

        // Pre-filled info - but editable
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");

        Label infoTitle = new Label("Your Information");
        infoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E1E1E;");

        // Editable fields pre-filled with user data
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label("👤 Name:");
        nameLbl.setStyle("-fx-text-fill: #6B7280; -fx-min-width: 80;");
        TextField nameField = new TextField(session.getUserName() != null ? session.getUserName() : "");
        nameField.setPrefWidth(250);
        nameRow.getChildren().addAll(nameLbl, nameField);

        HBox emailRow = new HBox(10);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        Label emailLbl = new Label("📧 Email:");
        emailLbl.setStyle("-fx-text-fill: #6B7280; -fx-min-width: 80;");
        TextField emailField = new TextField(session.getUserEmail() != null ? session.getUserEmail() : "");
        emailField.setPrefWidth(250);
        emailRow.getChildren().addAll(emailLbl, emailField);

        HBox phoneRow = new HBox(10);
        phoneRow.setAlignment(Pos.CENTER_LEFT);
        Label phoneLbl = new Label("📱 Phone:");
        phoneLbl.setStyle("-fx-text-fill: #6B7280; -fx-min-width: 80;");
        TextField phoneField = new TextField(session.getUserPhone() != null ? session.getUserPhone() : "");
        phoneField.setPrefWidth(250);
        phoneRow.getChildren().addAll(phoneLbl, phoneField);

        infoBox.getChildren().addAll(infoTitle, nameRow, emailRow, phoneRow);

        // Ticket selection
        VBox ticketBox = new VBox(10);
        ticketBox.setPadding(new Insets(15));
        ticketBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        Label ticketTitle = new Label("🎫 Ticket Details");
        ticketTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E1E1E;");

        HBox ticketTypeRow = new HBox(10);
        ticketTypeRow.setAlignment(Pos.CENTER_LEFT);
        Label ticketTypeLabel = new Label("Type:");
        ticketTypeLabel.setStyle("-fx-text-fill: #6B7280;");
        ComboBox<String> ticketTypeCombo = new ComboBox<>();
        ticketTypeCombo.getItems().addAll("STANDARD", "VIP");
        ticketTypeCombo.setValue("STANDARD");
        ticketTypeRow.getChildren().addAll(ticketTypeLabel, ticketTypeCombo);

        HBox ticketCountRow = new HBox(10);
        ticketCountRow.setAlignment(Pos.CENTER_LEFT);
        Label ticketCountLabel = new Label("Quantity:");
        ticketCountLabel.setStyle("-fx-text-fill: #6B7280;");
        int maxTickets = Math.max(1, Math.min(5, event.getAvailableSpots()));
        Spinner<Integer> ticketSpinner = new Spinner<>(1, maxTickets, 1);
        ticketSpinner.setPrefWidth(80);
        ticketCountRow.getChildren().addAll(ticketCountLabel, ticketSpinner);

        // Price display
        Label priceLabel = new Label();
        priceLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 16px; -fx-font-weight: bold;");

        Runnable updatePrice = () -> {
            int qty = ticketSpinner.getValue();
            double multiplier = "VIP".equals(ticketTypeCombo.getValue()) ? 1.5 : 1.0;
            if (event.isFree()) {
                priceLabel.setText("🆓 Total: Free");
            } else {
                double total = event.getPrice() * qty * multiplier;
                priceLabel.setText(String.format("💰 Total: $%.2f", total));
            }
        };
        updatePrice.run();
        ticketSpinner.valueProperty().addListener((obs, o, n) -> updatePrice.run());
        ticketTypeCombo.setOnAction(e -> updatePrice.run());

        ticketBox.getChildren().addAll(ticketTitle, ticketTypeRow, ticketCountRow, priceLabel);

        // Special requests
        Label specialLabel = new Label("📝 Special Requests (optional):");
        specialLabel.setStyle("-fx-text-fill: #6B7280;");
        TextArea specialRequests = new TextArea();
        specialRequests.setPromptText("Any dietary needs, accessibility requirements, etc.");
        specialRequests.setPrefRowCount(2);
        specialRequests.setStyle("-fx-font-size: 13px;");

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D62828; -fx-font-size: 13px;");
        errorLabel.setVisible(false);

        content.getChildren().addAll(eventTitle, infoBox, ticketBox, specialLabel, specialRequests, errorLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("✅ Confirm Registration");
        okButton.setStyle("-fx-background-color: #2F5D52; -fx-text-fill: white; -fx-font-weight: bold;");

        // Validate before closing
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            // Validate fields
            if (nameField.getText().trim().isEmpty()) {
                errorLabel.setText("❌ Name is required");
                errorLabel.setVisible(true);
                e.consume();
                return;
            }
            if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
                errorLabel.setText("❌ Valid email is required");
                errorLabel.setVisible(true);
                e.consume();
                return;
            }
            errorLabel.setVisible(false);
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Calculate total price
            double multiplier = "VIP".equals(ticketTypeCombo.getValue()) ? 1.5 : 1.0;
            double total = event.isFree() ? 0.0 : event.getPrice() * ticketSpinner.getValue() * multiplier;

            // Create registration
            EventRegistration reg = new EventRegistration();
            reg.setEventId(event.getId());
            reg.setUserName(nameField.getText().trim());
            reg.setEmail(emailField.getText().trim());
            reg.setPhone(phoneField.getText().trim());
            reg.setNumberOfTickets(ticketSpinner.getValue());
            reg.setTicketType(ticketTypeCombo.getValue());
            reg.setStatus("CONFIRMED");
            reg.setPaymentMethod(event.isFree() ? "FREE" : "PENDING");
            reg.setSpecialRequests(specialRequests.getText());
            reg.setRegistrationDate(LocalDateTime.now());
            reg.setTotalPrice(total);

            System.out.println("📌 Saving registration...");
            System.out.println("  Name: " + reg.getUserName());
            System.out.println("  Email: " + reg.getEmail());
            System.out.println("  Phone: " + reg.getPhone());
            System.out.println("  Tickets: " + reg.getNumberOfTickets());
            System.out.println("  Type: " + reg.getTicketType());
            System.out.println("  Total: $" + reg.getTotalPrice());
            System.out.println("  Date: " + reg.getRegistrationDate());

            boolean saved = reg.save();
            System.out.println("  Saved: " + saved + " (ID: " + reg.getId() + ")");

            if (saved) {
                event.setCurrentParticipants(event.getCurrentParticipants() + reg.getNumberOfTickets());
                event.update();

                // Show confirmation with QR code
                showRegistrationConfirmation(event, reg);
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed",
                        "Could not complete your registration.\n" +
                                "This email may already be registered for this event.\n" +
                                "Please try again.");
            }
        }
    }

    // =================== ADD REGISTRATION (CREATE) - Admin/Psychologist ===================

    public void showAddForm(Event event) {
        RegistrationFormView formView = new RegistrationFormView();

        VBox form = formView.buildForm(event, null,
                () -> {
                    String err = formView.validate();
                    if (err != null) { formView.showError(err); return; }

                    EventRegistration reg = formView.createFromForm(event.getId());
                    if (!reg.save()) {
                        formView.showError("❌ Could not register. Email may already be registered.");
                        return;
                    }
                    event.setCurrentParticipants(
                            event.getCurrentParticipants() + reg.getNumberOfTickets());
                    event.update();

                    showRegistrationConfirmation(event, reg);
                },
                () -> showEventDetail(event));

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== REGISTRATION CONFIRMATION DIALOG ===================

    private void showRegistrationConfirmation(Event event, EventRegistration registration) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("✅ Registration Successful!");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(450);

        Label successLabel = new Label("🎉 Registration Confirmed!");
        successLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2F5D52;");

        Label confirmNumber = new Label("Confirmation #: REG-" + String.format("%06d", registration.getId()));
        confirmNumber.setStyle("-fx-font-size: 16px; -fx-text-fill: #3E6F64;");

        // Registration details
        VBox detailBox = new VBox(5);
        detailBox.setPadding(new Insets(10));
        detailBox.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");
        detailBox.getChildren().addAll(
                new Label("👤 " + registration.getUserName()),
                new Label("📧 " + registration.getEmail()),
                new Label("🎫 " + registration.getTicketType() + " × " + registration.getNumberOfTickets()),
                new Label(registration.isFreeTicket() ? "🆓 Free" : String.format("💰 $%.2f", registration.getTotalPrice()))
        );

        // QR Code display
        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new Insets(15));
        qrBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        try {
            Image qrImage = QRCodeService.generateQRCodeFXImage(registration, event);
            if (qrImage != null) {
                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(200);
                qrView.setFitHeight(200);
                qrBox.getChildren().addAll(
                        new Label("📱 Your QR Code"),
                        qrView,
                        ComponentFactory.subText("Present this at the event entrance")
                );
            }
        } catch (Exception e) {
            System.err.println("Error generating QR: " + e.getMessage());
            qrBox.getChildren().add(new Label("📱 QR Code will be sent via email"));
        }

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button sendEmailBtn = ComponentFactory.styledButton("📧 Send Email", "#9BC7B5");
        Button downloadPDFBtn = ComponentFactory.styledButton("📄 Download PDF", "#3E6F64");
        Button saveQRBtn = ComponentFactory.styledButton("💾 Save QR Code", "#6B7280");

        sendEmailBtn.setOnAction(e -> {
            sendEmailBtn.setDisable(true);
            sendEmailBtn.setText("⏳ Sending...");

            Task<Boolean> emailTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return EmailService.sendConfirmationEmail(registration, event);
                }
            };

            emailTask.setOnSucceeded(ev -> {
                if (emailTask.getValue()) {
                    sendEmailBtn.setText("✅ Email Sent!");
                    sendEmailBtn.setStyle("-fx-background-color: #3E6F64; -fx-text-fill: white;" +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10;");
                } else {
                    sendEmailBtn.setText("❌ Failed");
                    sendEmailBtn.setDisable(false);
                }
            });

            new Thread(emailTask).start();
        });

        downloadPDFBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to save PDF");
            File dir = chooser.showDialog(dialog.getOwner());
            if (dir != null) {
                File pdf = PDFService.generateTicketPDF(registration, event, dir.getAbsolutePath());
                if (pdf != null) {
                    downloadPDFBtn.setText("✅ PDF Saved!");
                    showAlert(Alert.AlertType.INFORMATION, "PDF Saved",
                            "Ticket saved to:\n" + pdf.getAbsolutePath());
                }
            }
        });

        saveQRBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to save QR Code");
            File dir = chooser.showDialog(dialog.getOwner());
            if (dir != null) {
                File qrFile = QRCodeService.saveQRCodeToFile(registration, event, dir.getAbsolutePath());
                if (qrFile != null) {
                    saveQRBtn.setText("✅ QR Saved!");
                    showAlert(Alert.AlertType.INFORMATION, "QR Code Saved",
                            "QR code saved to:\n" + qrFile.getAbsolutePath());
                }
            }
        });

        actionButtons.getChildren().addAll(sendEmailBtn, downloadPDFBtn, saveQRBtn);

        content.getChildren().addAll(successLabel, confirmNumber, detailBox, qrBox, actionButtons);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
        showEventDetail(event);
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

        HBox ticketActions = new HBox(10);
        ticketActions.setAlignment(Pos.CENTER_LEFT);
        ticketActions.setPadding(new Insets(10, 0, 0, 0));

        Button viewQRBtn = ComponentFactory.smallButton("📱 View QR", "#9BC7B5");
        viewQRBtn.setOnAction(e -> showQRCodeDialog(event, reg));

        Button downloadPDFBtn = ComponentFactory.smallButton("📄 Download PDF", "#3E6F64");
        downloadPDFBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to save PDF");
            Stage stage = (Stage) mainContent.getScene().getWindow();
            File dir = chooser.showDialog(stage);
            if (dir != null) {
                File pdf = PDFService.generateTicketPDF(reg, event, dir.getAbsolutePath());
                if (pdf != null) {
                    showAlert(Alert.AlertType.INFORMATION, "PDF Saved",
                            "Ticket saved to:\n" + pdf.getAbsolutePath());
                }
            }
        });

        Button resendEmailBtn = ComponentFactory.smallButton("📧 Resend Email", "#6B7280");
        resendEmailBtn.setOnAction(e -> {
            resendEmailBtn.setText("⏳...");
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return EmailService.sendConfirmationEmail(reg, event);
                }
            };
            task.setOnSucceeded(ev -> {
                resendEmailBtn.setText(task.getValue() ? "✅ Sent!" : "❌ Failed");
            });
            new Thread(task).start();
        });

        ticketActions.getChildren().addAll(viewQRBtn, downloadPDFBtn, resendEmailBtn);
        form.getChildren().add(2, ticketActions);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        mainContent.setCenter(sp);
        mainContent.setRight(null);
    }

    // =================== QR CODE DIALOG ===================

    private void showQRCodeDialog(Event event, EventRegistration registration) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR Code - " + registration.getUserName());

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            Image qrImage = QRCodeService.generateQRCodeFXImage(registration, event);
            if (qrImage != null) {
                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(250);
                qrView.setFitHeight(250);
                content.getChildren().add(qrView);
            }
        } catch (Exception e) {
            content.getChildren().add(new Label("Could not generate QR code"));
        }

        Label title = new Label("Confirmation #: REG-" + String.format("%06d", registration.getId()));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2F5D52;");

        Button saveBtn = ComponentFactory.styledButton("💾 Save QR Code", "#9BC7B5");
        saveBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(dialog.getOwner());
            if (dir != null) {
                QRCodeService.saveQRCodeToFile(registration, event, dir.getAbsolutePath());
                saveBtn.setText("✅ Saved!");
            }
        });

        content.getChildren().addAll(title, saveBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // =================== EXPORT EVENT REPORT ===================

    private void exportEventReport(Event event, List<EventRegistration> registrations) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder to save Event Report");
        Stage stage = (Stage) mainContent.getScene().getWindow();
        File dir = chooser.showDialog(stage);

        if (dir != null) {
            File pdf = PDFService.generateEventReportPDF(event, registrations, dir.getAbsolutePath());
            if (pdf != null) {
                showAlert(Alert.AlertType.INFORMATION, "Report Exported",
                        "Event report saved to:\n" + pdf.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Failed to generate the event report.");
            }
        }
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

        List<EventRegistration> registrations = EventRegistration.findByEvent(event.getId());
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

    // =================== HELPER ===================

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
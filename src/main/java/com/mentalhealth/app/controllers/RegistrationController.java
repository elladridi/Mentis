package com.mentalhealth.app.controllers;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.services.EmailService;
import com.mentalhealth.app.services.PDFService;
import com.mentalhealth.app.services.QRCodeService;
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

        // Add action buttons for PDF export
        HBox exportButtons = new HBox(10);
        exportButtons.setAlignment(Pos.CENTER_LEFT);
        exportButtons.setPadding(new Insets(10, 30, 20, 30));

        Button exportReportBtn = ComponentFactory.styledButton("📄 Export Event Report (PDF)", "#3E6F64");
        exportReportBtn.setOnAction(e -> exportEventReport(event, registrations));

        exportButtons.getChildren().add(exportReportBtn);

        // Insert buttons after back button
        detail.getChildren().add(1, exportButtons);

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

                    // Show confirmation dialog with QR code and options
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

        // Success message
        Label successLabel = new Label("🎉 Registration Confirmed!");
        successLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2F5D52;");

        Label confirmNumber = new Label("Confirmation #: REG-" + String.format("%06d", registration.getId()));
        confirmNumber.setStyle("-fx-font-size: 16px; -fx-text-fill: #3E6F64;");

        // QR Code display
        Image qrImage = QRCodeService.generateQRCodeFXImage(registration, event);
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(200);
        qrView.setFitHeight(200);

        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new Insets(15));
        qrBox.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");
        qrBox.getChildren().addAll(
                new Label("📱 Your QR Code"),
                qrView,
                ComponentFactory.subText("Present this at the event entrance")
        );

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button sendEmailBtn = ComponentFactory.styledButton("📧 Send Email", "#9BC7B5");
        Button downloadPDFBtn = ComponentFactory.styledButton("📄 Download PDF", "#3E6F64");
        Button saveQRBtn = ComponentFactory.styledButton("💾 Save QR Code", "#6B7280");

        // Email button action
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

        // PDF button action
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

        // Save QR button action
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

        content.getChildren().addAll(successLabel, confirmNumber, qrBox, actionButtons);

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

        // Add QR code and PDF buttons for existing registrations
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

        // Add to form after title
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

        Image qrImage = QRCodeService.generateQRCodeFXImage(registration, event);
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(250);
        qrView.setFitHeight(250);

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

        content.getChildren().addAll(title, qrView, saveBtn);

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

    // =================== HELPER ===================

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegistrationFormView {

    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private ComboBox<String> ticketTypeBox;
    private Spinner<Integer> ticketCountSpinner;
    private Label totalPriceLabel;
    private ComboBox<String> paymentBox;
    private ComboBox<String> statusBox;
    private TextArea specialReqField;
    private Label errorLabel;
    private double eventPrice;

    public VBox buildForm(Event event, EventRegistration existing,
                          Runnable onSave, Runnable onCancel) {
        boolean isEdit = existing != null;
        this.eventPrice = event.getPrice();

        VBox form = new VBox(18);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF;");
        form.setMaxWidth(700);

        Label formTitle = ComponentFactory.pageTitle(
                isEdit ? "✏️ Edit Registration" : "🎟 New Registration");

        Label eventLabel = new Label("Event: " + event.getTitle() +
                (event.isFree() ? " (FREE)" :
                        String.format(" ($%.2f per ticket)", event.getPrice())));
        eventLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 14px;");

        Label availLabel = new Label("🟢 " + event.getAvailableSpots() + " spots available");
        availLabel.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 13px;");

        nameField = ComponentFactory.styledTextField("Full Name *");
        emailField = ComponentFactory.styledTextField("Email Address *");
        phoneField = ComponentFactory.styledTextField("Phone Number");

        ticketTypeBox = new ComboBox<>();
        ticketTypeBox.getItems().addAll("STANDARD", "VIP", "EARLY_BIRD", "GROUP");
        ticketTypeBox.setValue("STANDARD");
        ticketTypeBox.setStyle("-fx-background-color: #F1F6F4; -fx-pref-width: 320;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        ticketCountSpinner = new Spinner<>(1, 10, 1);
        ticketCountSpinner.setPrefWidth(320);
        ticketCountSpinner.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        totalPriceLabel = new Label(
                event.isFree() ? "Total: FREE" :
                        String.format("Total: $%.2f", event.getPrice()));
        totalPriceLabel.setStyle(
                "-fx-text-fill: #2F5D52; -fx-font-size: 16px; -fx-font-weight: bold;");

        ticketCountSpinner.valueProperty().addListener((obs, o, n) -> updatePrice());
        ticketTypeBox.setOnAction(e -> updatePrice());

        paymentBox = new ComboBox<>();
        paymentBox.getItems().addAll("CREDIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH", "FREE");
        paymentBox.setValue(event.isFree() ? "FREE" : "CREDIT_CARD");
        paymentBox.setStyle("-fx-background-color: #F1F6F4; -fx-pref-width: 320;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("CONFIRMED", "PENDING", "CANCELLED");
        statusBox.setValue("CONFIRMED");
        statusBox.setStyle("-fx-background-color: #F1F6F4; -fx-pref-width: 320;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        specialReqField = ComponentFactory.styledTextArea(
                "Any special requests or notes...");

        errorLabel = ComponentFactory.errorLabel();

        if (isEdit) {
            nameField.setText(existing.getUserName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            ticketTypeBox.setValue(existing.getTicketType());
            ticketCountSpinner.getValueFactory().setValue(existing.getNumberOfTickets());
            paymentBox.setValue(existing.getPaymentMethod());
            statusBox.setValue(existing.getStatus());
            specialReqField.setText(existing.getSpecialRequests());
            updatePrice();
        }

        Button saveBtn = ComponentFactory.styledButton(
                isEdit ? "💾 Update Registration" : "✅ Complete Registration",
                isEdit ? "#3E6F64" : "#9BC7B5");
        saveBtn.setOnAction(e -> onSave.run());

        Button cancelBtn = ComponentFactory.styledButton("← Back", "#6B7280");
        cancelBtn.setOnAction(e -> onCancel.run());

        HBox buttons = new HBox(15, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        form.getChildren().addAll(formTitle, eventLabel, availLabel,
                ComponentFactory.fieldGroup("Full Name *", nameField),
                ComponentFactory.fieldGroup("Email *", emailField),
                ComponentFactory.fieldGroup("Phone", phoneField),
                ComponentFactory.fieldGroup("Ticket Type", ticketTypeBox),
                ComponentFactory.fieldGroup("Number of Tickets", ticketCountSpinner),
                totalPriceLabel,
                ComponentFactory.fieldGroup("Payment Method", paymentBox),
                ComponentFactory.fieldGroup("Status", statusBox),
                ComponentFactory.fieldGroup("Special Requests", specialReqField),
                errorLabel, buttons);

        return form;
    }

    private void updatePrice() {
        int count = ticketCountSpinner.getValue();
        double multiplier = switch (ticketTypeBox.getValue()) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };
        double total = eventPrice * count * multiplier;
        totalPriceLabel.setText(
                total == 0 ? "Total: FREE" : String.format("Total: $%.2f", total));
    }

    public String validate() {
        if (nameField.getText().trim().isEmpty()) return "❌ Name is required!";
        String email = emailField.getText().trim();
        if (email.isEmpty()) return "❌ Email is required!";
        if (!email.contains("@") || !email.contains("."))
            return "❌ Invalid email format!";
        return null;
    }

    public void showError(String msg) { errorLabel.setText(msg); }

    public EventRegistration createFromForm(int eventId) {
        int count = ticketCountSpinner.getValue();
        double multiplier = switch (ticketTypeBox.getValue()) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };
        double total = eventPrice * count * multiplier;

        EventRegistration reg = new EventRegistration(eventId,
                nameField.getText().trim(), emailField.getText().trim(),
                phoneField.getText().trim(), ticketTypeBox.getValue(),
                count, total, paymentBox.getValue());
        reg.setStatus(statusBox.getValue());
        reg.setSpecialRequests(specialReqField.getText().trim());
        return reg;
    }

    public void fillFromForm(EventRegistration reg) {
        reg.setUserName(nameField.getText().trim());
        reg.setEmail(emailField.getText().trim());
        reg.setPhone(phoneField.getText().trim());
        reg.setTicketType(ticketTypeBox.getValue());
        reg.setNumberOfTickets(ticketCountSpinner.getValue());
        double multiplier = switch (ticketTypeBox.getValue()) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };
        reg.setTotalPrice(eventPrice * ticketCountSpinner.getValue() * multiplier);
        reg.setStatus(statusBox.getValue());
        reg.setPaymentMethod(paymentBox.getValue());
        reg.setSpecialRequests(specialReqField.getText().trim());
    }
}
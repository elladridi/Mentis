package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;

public class RegistrationFormView {

    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#2E7D32";
    private static final String INK = "#1A3C34";
    private static final String TEXT = "#2D3748";
    private static final String MUTED = "#6C757D";
    private static final String BORDER = "#DDE5E2";
    private static final String RED = "#D62828";
    private static final String ORANGE = "#F39C12";
    private static final String BLUE = "#4FACFE";
    private static final String PURPLE = "#9B5DE5";

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
        this.eventPrice = event != null ? event.getPrice() : 0.0;

        VBox page = new VBox(20);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: transparent;");

        VBox hero = buildHero(event, isEdit);
        VBox formCard = buildFormCard(event, existing, isEdit, onSave, onCancel);

        page.getChildren().addAll(hero, formCard);
        return page;
    }

    private VBox buildHero(Event event, boolean isEdit) {
        VBox hero = new VBox(10);
        hero.setPadding(new Insets(26));
        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        (isEdit ? BLUE : PRIMARY) + ", " + PRIMARY_DARK + ");" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.20), 24, 0, 0, 8);"
        );

        Label title = new Label(isEdit ? "Edit Registration" : "New Registration");
        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 30px;" +
                        "-fx-font-weight: 900;"
        );

        Label subtitle = new Label(
                event != null
                        ? "Event: " + event.getTitle()
                        : "Complete registration details"
        );
        subtitle.setWrapText(true);
        subtitle.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.88);" +
                        "-fx-font-size: 14px;"
        );

        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER_LEFT);

        if (event != null) {
            badges.getChildren().addAll(
                    whiteBadge(event.isFree() ? "FREE" : String.format("$%.2f / ticket", event.getPrice())),
                    whiteBadge(event.getAvailableSpots() + " spots available"),
                    whiteBadge(event.getEventType() != null ? event.getEventType() : "EVENT")
            );
        }

        hero.getChildren().addAll(title, subtitle, badges);
        return hero;
    }

    private VBox buildFormCard(Event event, EventRegistration existing,
                               boolean isEdit, Runnable onSave, Runnable onCancel) {

        VBox card = new VBox(20);
        card.setPadding(new Insets(26));
        card.setMaxWidth(780);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 20, 0, 0, 8);"
        );

        Label sectionTitle = sectionTitle("Participant Information");

        nameField = ComponentFactory.styledTextField("Full name");
        emailField = ComponentFactory.styledTextField("Email address");
        phoneField = ComponentFactory.styledTextField("Phone number");

        GridPane personalGrid = new GridPane();
        personalGrid.setHgap(16);
        personalGrid.setVgap(16);

        personalGrid.add(ComponentFactory.fieldGroup("Full Name *", nameField), 0, 0);
        personalGrid.add(ComponentFactory.fieldGroup("Email *", emailField), 1, 0);
        personalGrid.add(ComponentFactory.fieldGroup("Phone", phoneField), 0, 1);

        Label ticketSection = sectionTitle("Ticket Details");

        ticketTypeBox = new ComboBox<>();
        ticketTypeBox.getItems().addAll("STANDARD", "VIP", "EARLY_BIRD", "GROUP");
        ticketTypeBox.setValue("STANDARD");
        ticketTypeBox.setPrefWidth(320);
        ticketTypeBox.setPrefHeight(44);
        ticketTypeBox.setStyle(controlStyle());

        int availableSpots = event != null ? Math.max(1, event.getAvailableSpots()) : 10;
        int maxTickets = Math.min(10, availableSpots);
        ticketCountSpinner = new Spinner<>(1, maxTickets, 1);
        ticketCountSpinner.setPrefWidth(320);
        ticketCountSpinner.setPrefHeight(44);
        ticketCountSpinner.setStyle(controlStyle());

        paymentBox = new ComboBox<>();
        paymentBox.getItems().addAll("CREDIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH", "FREE", "PENDING");
        paymentBox.setValue(event != null && event.isFree() ? "FREE" : "CREDIT_CARD");
        paymentBox.setPrefWidth(320);
        paymentBox.setPrefHeight(44);
        paymentBox.setStyle(controlStyle());

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("CONFIRMED", "PENDING", "CANCELLED");
        statusBox.setValue("CONFIRMED");
        statusBox.setPrefWidth(320);
        statusBox.setPrefHeight(44);
        statusBox.setStyle(controlStyle());

        GridPane ticketGrid = new GridPane();
        ticketGrid.setHgap(16);
        ticketGrid.setVgap(16);

        ticketGrid.add(ComponentFactory.fieldGroup("Ticket Type", ticketTypeBox), 0, 0);
        ticketGrid.add(ComponentFactory.fieldGroup("Number of Tickets", ticketCountSpinner), 1, 0);
        ticketGrid.add(ComponentFactory.fieldGroup("Payment Method", paymentBox), 0, 1);
        ticketGrid.add(ComponentFactory.fieldGroup("Status", statusBox), 1, 1);

        VBox priceCard = buildPriceCard(event);

        specialReqField = ComponentFactory.styledTextArea("Accessibility needs, notes, or special requests...");
        specialReqField.setPrefWidth(680);
        specialReqField.setPrefRowCount(4);

        errorLabel = ComponentFactory.errorLabel();
        errorLabel.setVisible(false);

        ticketCountSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePrice());
        ticketTypeBox.setOnAction(e -> updatePrice());

        if (isEdit) {
            fillFields(existing);
        } else {
            updatePrice();
        }

        Button saveBtn = ComponentFactory.styledButton(
                isEdit ? "Update Registration" : "Complete Registration",
                isEdit ? BLUE : PRIMARY_DARK
        );
        saveBtn.setOnAction(e -> onSave.run());

        Button cancelBtn = ComponentFactory.styledButton("Back", "#6C757D");
        cancelBtn.setOnAction(e -> onCancel.run());

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.getChildren().addAll(saveBtn, cancelBtn);

        card.getChildren().addAll(
                sectionTitle,
                personalGrid,
                divider(),
                ticketSection,
                ticketGrid,
                priceCard,
                ComponentFactory.fieldGroup("Special Requests", specialReqField),
                errorLabel,
                buttons
        );

        return card;
    }

    private VBox buildPriceCard(Event event) {
        VBox priceCard = new VBox(6);
        priceCard.setPadding(new Insets(18));
        priceCard.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 20;"
        );

        Label label = new Label("Calculated Total");
        label.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        totalPriceLabel = new Label();
        totalPriceLabel.setStyle(
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: 900;"
        );

        Label hint = new Label(
                event != null && event.isFree()
                        ? "This is a free event."
                        : "Ticket multiplier is applied automatically."
        );
        hint.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        priceCard.getChildren().addAll(label, totalPriceLabel, hint);
        return priceCard;
    }

    private void fillFields(EventRegistration existing) {
        if (existing == null) return;

        nameField.setText(existing.getUserName());
        emailField.setText(existing.getEmail());
        phoneField.setText(existing.getPhone());

        ticketTypeBox.setValue(existing.getTicketType() != null ? existing.getTicketType() : "STANDARD");

        int ticketCount = Math.max(1, existing.getNumberOfTickets());
        ticketCountSpinner.getValueFactory().setValue(ticketCount);

        paymentBox.setValue(existing.getPaymentMethod() != null ? existing.getPaymentMethod() : "CREDIT_CARD");
        statusBox.setValue(existing.getStatus() != null ? existing.getStatus() : "CONFIRMED");

        specialReqField.setText(existing.getSpecialRequests());

        updatePrice();
    }

    private void updatePrice() {
        if (ticketCountSpinner == null || ticketTypeBox == null || totalPriceLabel == null) return;

        int count = ticketCountSpinner.getValue();
        double total = eventPrice * count * getTicketMultiplier(ticketTypeBox.getValue());

        if (total == 0) {
            totalPriceLabel.setText("FREE");
        } else {
            totalPriceLabel.setText(String.format("$%.2f", total));
        }
    }

    public String validate() {
        hideError();

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return "Name is required.";
        }

        if (nameField.getText().trim().length() < 2) {
            return "Name must contain at least 2 characters.";
        }

        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (email.isEmpty()) {
            return "Email is required.";
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Invalid email format.";
        }

        if (ticketCountSpinner.getValue() == null || ticketCountSpinner.getValue() < 1) {
            return "Number of tickets must be at least 1.";
        }

        return null;
    }

    public void showError(String msg) {
        if (errorLabel == null) return;

        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        if (errorLabel == null) return;

        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public EventRegistration createFromForm(int eventId) {
        EventRegistration reg = new EventRegistration();

        reg.setEventId(eventId);
        reg.setUserName(nameField.getText().trim());
        reg.setEmail(emailField.getText().trim());
        reg.setPhone(phoneField.getText() != null ? phoneField.getText().trim() : null);
        reg.setTicketType(ticketTypeBox.getValue());
        reg.setNumberOfTickets(ticketCountSpinner.getValue());
        reg.setTotalPrice(calculateTotal());
        reg.setPaymentMethod(paymentBox.getValue());
        reg.setStatus(statusBox.getValue());
        reg.setSpecialRequests(specialReqField.getText() != null ? specialReqField.getText().trim() : null);
        reg.setRegistrationDate(LocalDateTime.now());
        reg.setUpdatedAt(LocalDateTime.now());

        return reg;
    }

    public void fillFromForm(EventRegistration reg) {
        if (reg == null) return;

        reg.setUserName(nameField.getText().trim());
        reg.setEmail(emailField.getText().trim());
        reg.setPhone(phoneField.getText() != null ? phoneField.getText().trim() : null);
        reg.setTicketType(ticketTypeBox.getValue());
        reg.setNumberOfTickets(ticketCountSpinner.getValue());
        reg.setTotalPrice(calculateTotal());
        reg.setPaymentMethod(paymentBox.getValue());
        reg.setStatus(statusBox.getValue());
        reg.setSpecialRequests(specialReqField.getText() != null ? specialReqField.getText().trim() : null);
        reg.setUpdatedAt(LocalDateTime.now());
    }

    private double calculateTotal() {
        int count = ticketCountSpinner.getValue();
        return eventPrice * count * getTicketMultiplier(ticketTypeBox.getValue());
    }

    private double getTicketMultiplier(String type) {
        if (type == null) return 1.0;

        return switch (type) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: 900;"
        );
        return label;
    }

    private Label whiteBadge(String text) {
        Label badge = new Label(text);
        badge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 7 14;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        return badge;
    }

    private Separator divider() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + BORDER + ";");
        return separator;
    }

    private String controlStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #DDE5E2;" +
                "-fx-border-radius: 16;" +
                "-fx-padding: 6 12;" +
                "-fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);";
    }
}
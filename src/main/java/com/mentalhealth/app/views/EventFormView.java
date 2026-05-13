package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventFormView {

    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#2E7D32";
    private static final String INK = "#1A3C34";
    private static final String TEXT = "#2D3748";
    private static final String MUTED = "#6C757D";
    private static final String BORDER = "#DDE5E2";
    private static final String RED = "#D62828";
    private static final String BLUE = "#4FACFE";
    private static final String ORANGE = "#F39C12";

    private TextField titleField;
    private TextArea descField;
    private DatePicker datePicker;
    private ComboBox<String> hourBox;
    private ComboBox<String> minBox;
    private TextField locationField;
    private TextField maxPartField;
    private TextField priceField;
    private ComboBox<String> typeBox;
    private ComboBox<String> statusBox;
    private Label errorLabel;

    public VBox buildForm(Event existing, Runnable onSave, Runnable onCancel) {

        boolean isEdit = existing != null;

        VBox page = new VBox(20);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: transparent;");

        VBox hero = buildHero(existing, isEdit);
        VBox formCard = buildFormCard(existing, isEdit, onSave, onCancel);

        page.getChildren().addAll(hero, formCard);

        return page;
    }

    // =================== HERO ===================

    private VBox buildHero(Event existing, boolean isEdit) {

        VBox hero = new VBox(10);
        hero.setPadding(new Insets(28));

        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        (isEdit ? BLUE : PRIMARY) + ", " + PRIMARY_DARK + ");" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.20), 24, 0, 0, 8);"
        );

        Label title = new Label(
                isEdit ? "Edit Event" : "Create New Event"
        );

        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 30px;" +
                        "-fx-font-weight: 900;"
        );

        Label subtitle = new Label(
                isEdit
                        ? "Update event details, availability, and registration settings."
                        : "Create a new wellness event, seminar, workshop, or therapy session."
        );

        subtitle.setWrapText(true);

        subtitle.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.88);" +
                        "-fx-font-size: 14px;"
        );

        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER_LEFT);

        if (existing != null) {

            badges.getChildren().addAll(
                    whiteBadge(existing.getEventType() != null ? existing.getEventType() : "EVENT"),
                    whiteBadge(existing.getStatus() != null ? existing.getStatus() : "UPCOMING"),
                    whiteBadge(existing.isFree()
                            ? "FREE EVENT"
                            : String.format("$%.2f", existing.getPrice()))
            );
        }

        hero.getChildren().addAll(title, subtitle, badges);

        return hero;
    }

    // =================== FORM CARD ===================

    private VBox buildFormCard(Event existing,
                               boolean isEdit,
                               Runnable onSave,
                               Runnable onCancel) {

        VBox card = new VBox(20);

        card.setPadding(new Insets(28));
        card.setMaxWidth(820);

        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 20, 0, 0, 8);"
        );

        Label generalTitle = sectionTitle("General Information");

        // =================== FIELDS ===================

        titleField = ComponentFactory.styledTextField("Event title");
        descField = ComponentFactory.styledTextArea("Describe the event, goals, activities, and audience...");
        descField.setPrefRowCount(5);

        locationField = ComponentFactory.styledTextField(
                "Physical location or online meeting link"
        );

        maxPartField = ComponentFactory.styledTextField("Maximum participants");
        priceField = ComponentFactory.styledTextField("0 for free");

        // =================== DATE ===================

        datePicker = new DatePicker();
        datePicker.setPrefWidth(320);
        datePicker.setStyle(controlStyle());

        hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(String.format("%02d", i));
        }

        hourBox.setValue("10");
        hourBox.setPrefWidth(120);
        hourBox.setStyle(controlStyle());

        minBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 15) {
            minBox.getItems().add(String.format("%02d", i));
        }

        minBox.setValue("00");
        minBox.setPrefWidth(120);
        minBox.setStyle(controlStyle());

        HBox timeRow = new HBox(10);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label colon = new Label(":");
        colon.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        timeRow.getChildren().addAll(hourBox, colon, minBox);

        // =================== COMBOS ===================

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "WORKSHOP",
                "GROUP_THERAPY",
                "SEMINAR",
                "SOCIAL"
        );

        typeBox.setValue("WORKSHOP");
        typeBox.setPrefWidth(320);
        typeBox.setStyle(controlStyle());

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll(
                "UPCOMING",
                "ONGOING",
                "COMPLETED",
                "CANCELLED"
        );

        statusBox.setValue("UPCOMING");
        statusBox.setPrefWidth(320);
        statusBox.setStyle(controlStyle());

        // =================== GRID ===================

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(ComponentFactory.fieldGroup("Event Title *", titleField), 0, 0, 2, 1);
        grid.add(ComponentFactory.fieldGroup("Date *", datePicker), 0, 1);
        grid.add(ComponentFactory.fieldGroup("Time *", timeRow), 1, 1);
        grid.add(ComponentFactory.fieldGroup("Location", locationField), 0, 2, 2, 1);
        grid.add(ComponentFactory.fieldGroup("Max Participants *", maxPartField), 0, 3);
        grid.add(ComponentFactory.fieldGroup("Price", priceField), 1, 3);
        grid.add(ComponentFactory.fieldGroup("Event Type", typeBox), 0, 4);
        grid.add(ComponentFactory.fieldGroup("Status", statusBox), 1, 4);

        VBox priceCard = buildPricePreview();

        // =================== ERROR ===================

        errorLabel = ComponentFactory.errorLabel();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // =================== PREFILL ===================

        if (isEdit && existing != null) {

            titleField.setText(existing.getTitle());
            descField.setText(existing.getDescription());

            if (existing.getDateTime() != null) {

                datePicker.setValue(existing.getDateTime().toLocalDate());

                hourBox.setValue(
                        String.format("%02d", existing.getDateTime().getHour())
                );

                minBox.setValue(
                        String.format("%02d", existing.getDateTime().getMinute())
                );
            }

            locationField.setText(existing.getLocation());

            maxPartField.setText(
                    String.valueOf(existing.getMaxParticipants())
            );

            priceField.setText(
                    String.valueOf(existing.getPrice())
            );

            typeBox.setValue(existing.getEventType());
            statusBox.setValue(existing.getStatus());

        } else {

            datePicker.setValue(LocalDate.now().plusDays(7));
            priceField.setText("0");
        }

        priceField.textProperty().addListener((obs, oldValue, newValue) -> {
            updatePricePreview();
        });

        updatePricePreview();

        // =================== BUTTONS ===================

        Button saveBtn = ComponentFactory.styledButton(
                isEdit ? "Update Event" : "Create Event",
                isEdit ? BLUE : PRIMARY_DARK
        );

        saveBtn.setOnAction(e -> onSave.run());

        Button cancelBtn = ComponentFactory.styledButton(
                "Back",
                "#6C757D"
        );

        cancelBtn.setOnAction(e -> onCancel.run());

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.getChildren().addAll(saveBtn, cancelBtn);

        // =================== FINAL ===================

        card.getChildren().addAll(
                generalTitle,
                grid,
                ComponentFactory.fieldGroup("Description", descField),
                priceCard,
                errorLabel,
                buttons
        );

        return card;
    }

    // =================== PRICE PREVIEW ===================

    private VBox buildPricePreview() {

        VBox card = new VBox(6);

        card.setPadding(new Insets(18));

        card.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 20;"
        );

        Label title = new Label("Event Pricing");

        title.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        Label preview = new Label();
        preview.setId("price-preview");

        preview.setStyle(
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: 900;"
        );

        Label hint = new Label(
                "Set the ticket price. Use 0 for free events."
        );

        hint.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        card.getChildren().addAll(title, preview, hint);

        return card;
    }

    private void updatePricePreview() {

        if (priceField == null) return;

        try {

            double price = Double.parseDouble(priceField.getText().trim());

            VBox root = (VBox) priceField.getScene().lookup("#price-preview");

        } catch (Exception ignored) {}
    }

    // =================== VALIDATION ===================

    public String validate() {

        hideError();

        if (titleField.getText() == null ||
                titleField.getText().trim().isEmpty()) {

            return "Title is required.";
        }

        if (titleField.getText().trim().length() < 3) {
            return "Title must contain at least 3 characters.";
        }

        if (datePicker.getValue() == null) {
            return "Date is required.";
        }

        try {

            int max = Integer.parseInt(
                    maxPartField.getText().trim()
            );

            if (max <= 0) {
                return "Maximum participants must be greater than 0.";
            }

        } catch (Exception e) {
            return "Maximum participants must be a valid number.";
        }

        try {

            double price = Double.parseDouble(
                    priceField.getText().trim()
            );

            if (price < 0) {
                return "Price cannot be negative.";
            }

        } catch (Exception e) {
            return "Price must be a valid number.";
        }

        return null;
    }

    // =================== ERRORS ===================

    public void showError(String msg) {

        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    // =================== FORM -> EVENT ===================

    public void fillEventFromForm(Event event) {

        if (event == null) return;

        event.setTitle(titleField.getText().trim());

        event.setDescription(
                descField.getText() != null
                        ? descField.getText().trim()
                        : ""
        );

        event.setDateTime(
                LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.of(
                                Integer.parseInt(hourBox.getValue()),
                                Integer.parseInt(minBox.getValue())
                        )
                )
        );

        event.setLocation(
                locationField.getText() != null
                        ? locationField.getText().trim()
                        : ""
        );

        event.setMaxParticipants(
                Integer.parseInt(maxPartField.getText().trim())
        );

        event.setPrice(
                Double.parseDouble(priceField.getText().trim())
        );

        event.setEventType(typeBox.getValue());
        event.setStatus(statusBox.getValue());

        event.setUpdatedAt(LocalDateTime.now());
    }

    public Event createEventFromForm() {

        Event event = new Event(
                titleField.getText().trim(),

                descField.getText() != null
                        ? descField.getText().trim()
                        : "",

                LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.of(
                                Integer.parseInt(hourBox.getValue()),
                                Integer.parseInt(minBox.getValue())
                        )
                ),

                locationField.getText() != null
                        ? locationField.getText().trim()
                        : "",

                Integer.parseInt(maxPartField.getText().trim()),

                typeBox.getValue(),

                Double.parseDouble(priceField.getText().trim())
        );

        event.setStatus(statusBox.getValue());
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        return event;
    }

    // =================== HELPERS ===================

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

    private String controlStyle() {

        return "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 16;" +
                "-fx-padding: 6 12;" +
                "-fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);";
    }
}
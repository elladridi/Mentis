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

        VBox form = new VBox(18);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF;");
        form.setMaxWidth(700);

        Label formTitle = ComponentFactory.pageTitle(
                isEdit ? "✏️ Edit Event" : "➕ Create New Event");

        titleField = ComponentFactory.styledTextField("Event Title *");
        descField = ComponentFactory.styledTextArea("Event Description");

        datePicker = new DatePicker();
        datePicker.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");
        datePicker.setPrefWidth(320);

        hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) hourBox.getItems().add(String.format("%02d", i));
        hourBox.setValue("10");
        hourBox.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        minBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 15) minBox.getItems().add(String.format("%02d", i));
        minBox.setValue("00");
        minBox.setStyle("-fx-background-color: #F1F6F4;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        HBox timeRow = new HBox(10);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Label timeLbl = new Label("Time:");
        timeLbl.setStyle("-fx-text-fill: #1E1E1E;");
        Label colonLbl = new Label(":");
        colonLbl.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 16px;");
        timeRow.getChildren().addAll(timeLbl, hourBox, colonLbl, minBox);

        locationField = ComponentFactory.styledTextField("Location");
        maxPartField = ComponentFactory.styledTextField("Max Participants *");
        priceField = ComponentFactory.styledTextField("Price (0 for free)");

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("WORKSHOP", "GROUP_THERAPY", "SEMINAR", "SOCIAL");
        typeBox.setValue("WORKSHOP");
        typeBox.setStyle("-fx-background-color: #F1F6F4; -fx-pref-width: 320;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("UPCOMING", "ONGOING", "COMPLETED", "CANCELLED");
        statusBox.setValue("UPCOMING");
        statusBox.setStyle("-fx-background-color: #F1F6F4; -fx-pref-width: 320;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        errorLabel = ComponentFactory.errorLabel();

        if (isEdit) {
            titleField.setText(existing.getTitle());
            descField.setText(existing.getDescription());
            datePicker.setValue(existing.getDateTime().toLocalDate());
            hourBox.setValue(String.format("%02d", existing.getDateTime().getHour()));
            minBox.setValue(String.format("%02d", existing.getDateTime().getMinute()));
            locationField.setText(existing.getLocation());
            maxPartField.setText(String.valueOf(existing.getMaxParticipants()));
            priceField.setText(String.valueOf(existing.getPrice()));
            typeBox.setValue(existing.getEventType());
            statusBox.setValue(existing.getStatus());
        } else {
            datePicker.setValue(LocalDate.now().plusDays(7));
            priceField.setText("0");
        }

        Button saveBtn = ComponentFactory.styledButton(
                isEdit ? "💾 Update Event" : "✅ Create Event",
                isEdit ? "#3E6F64" : "#9BC7B5");
        saveBtn.setOnAction(e -> onSave.run());

        Button cancelBtn = ComponentFactory.styledButton("← Back to Events", "#6B7280");
        cancelBtn.setOnAction(e -> onCancel.run());

        HBox buttons = new HBox(15, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        form.getChildren().addAll(formTitle,
                ComponentFactory.fieldGroup("Title *", titleField),
                ComponentFactory.fieldGroup("Description", descField),
                ComponentFactory.fieldGroup("Date *", datePicker),
                timeRow,
                ComponentFactory.fieldGroup("Location", locationField),
                ComponentFactory.fieldGroup("Max Participants *", maxPartField),
                ComponentFactory.fieldGroup("Price ($)", priceField),
                ComponentFactory.fieldGroup("Event Type", typeBox),
                ComponentFactory.fieldGroup("Status", statusBox),
                errorLabel, buttons);

        return form;
    }

    public String validate() {
        if (titleField.getText().trim().isEmpty()) return "❌ Title is required!";
        if (datePicker.getValue() == null) return "❌ Date is required!";
        try {
            int mp = Integer.parseInt(maxPartField.getText().trim());
            if (mp <= 0) return "❌ Max participants must be positive!";
        } catch (NumberFormatException e) {
            return "❌ Max participants must be a number!";
        }
        try {
            double pr = Double.parseDouble(priceField.getText().trim());
            if (pr < 0) return "❌ Price cannot be negative!";
        } catch (NumberFormatException e) {
            return "❌ Price must be a number!";
        }
        return null;
    }

    public void showError(String msg) { errorLabel.setText(msg); }

    public void fillEventFromForm(Event event) {
        event.setTitle(titleField.getText().trim());
        event.setDescription(descField.getText().trim());
        event.setDateTime(LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.of(Integer.parseInt(hourBox.getValue()),
                        Integer.parseInt(minBox.getValue()))));
        event.setLocation(locationField.getText().trim());
        event.setMaxParticipants(Integer.parseInt(maxPartField.getText().trim()));
        event.setPrice(Double.parseDouble(priceField.getText().trim()));
        event.setEventType(typeBox.getValue());
        event.setStatus(statusBox.getValue());
    }

    public Event createEventFromForm() {
        return new Event(
                titleField.getText().trim(),
                descField.getText().trim(),
                LocalDateTime.of(datePicker.getValue(),
                        LocalTime.of(Integer.parseInt(hourBox.getValue()),
                                Integer.parseInt(minBox.getValue()))),
                locationField.getText().trim(),
                Integer.parseInt(maxPartField.getText().trim()),
                typeBox.getValue(),
                Double.parseDouble(priceField.getText().trim()));
    }
}
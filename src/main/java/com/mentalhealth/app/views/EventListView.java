package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.utils.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class EventListView {

    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#2E7D32";
    private static final String INK = "#1A3C34";
    private static final String TEXT = "#2D3748";
    private static final String MUTED = "#6C757D";
    private static final String SOFT = "#F1F8E9";
    private static final String BORDER = "#DDE5E2";
    private static final String RED = "#D62828";
    private static final String ORANGE = "#F39C12";
    private static final String BLUE = "#4FACFE";
    private static final String PURPLE = "#9B5DE5";

    public VBox buildCard(Event event, int regCount, double revenue,
                          Runnable onView, Runnable onEdit,
                          Runnable onDelete, Runnable onRegister) {

        VBox card = new VBox(16);
        card.setPrefWidth(370);
        card.setMinHeight(360);
        card.setPadding(new Insets(0));
        card.setStyle(ComponentFactory.cardStyle());
        card.setOnMouseEntered(e -> card.setStyle(ComponentFactory.cardHoverStyle()));
        card.setOnMouseExited(e -> card.setStyle(ComponentFactory.cardStyle()));

        VBox header = buildCardHeader(event);
        VBox body = buildCardBody(event, regCount, revenue);
        HBox actions = buildActions(event, onView, onEdit, onDelete, onRegister);

        card.getChildren().addAll(header, body, actions);
        return card;
    }

    private VBox buildCardHeader(Event event) {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        getTypeColor(event.getEventType()) + ", " + PRIMARY_DARK + ");" +
                        "-fx-background-radius: 22 22 0 0;"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(getTypeEmoji(event.getEventType()) + " " + safe(event.getEventType(), "EVENT"));
        typeBadge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 13;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(getStatusEmoji(event.getStatus()) + " " + safe(event.getStatus(), "UPCOMING"));
        status.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 13;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        top.getChildren().addAll(typeBadge, spacer, status);

        Label title = new Label(event.getTitle());
        title.setWrapText(true);
        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;"
        );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm");
        Label date = new Label("📅 " + (event.getDateTime() != null ? event.getDateTime().format(dtf) : "No date"));
        date.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.90);" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(top, title, date);
        return header;
    }

    private VBox buildCardBody(Event event, int regCount, double revenue) {
        VBox body = new VBox(13);
        body.setPadding(new Insets(18, 20, 8, 20));

        String desc = event.getDescription() != null ? event.getDescription() : "";
        if (desc.length() > 115) {
            desc = desc.substring(0, 115) + "...";
        }

        Label description = new Label(desc.isEmpty() ? "No description available." : desc);
        description.setWrapText(true);
        description.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-line-spacing: 3;"
        );

        HBox locationRow = iconInfo("📍", safe(event.getLocation(), "No location"));
        HBox capacityPrice = new HBox(12);
        capacityPrice.setAlignment(Pos.CENTER_LEFT);

        Label capacity = pill("👥 " + event.getCurrentParticipants() + "/" + event.getMaxParticipants(), "#E8F5E9", PRIMARY_DARK);
        Label price = pill(event.isFree() ? "Free" : String.format("$%.2f", event.getPrice()), "#EEF2FF", PURPLE);

        capacityPrice.getChildren().addAll(capacity, price);

        ProgressBar progress = new ProgressBar(calculateProgress(event));
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setPrefHeight(8);
        progress.setStyle(
                "-fx-accent: " + getCapacityColor(event) + ";" +
                        "-fx-control-inner-background: #E9ECEF;"
        );

        Label availability = new Label(
                event.isAvailable()
                        ? "🟢 " + event.getAvailableSpots() + " spots available"
                        : "🔴 Sold out"
        );
        availability.setStyle(
                "-fx-text-fill: " + (event.isAvailable() ? PRIMARY_DARK : RED) + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        body.getChildren().addAll(description, locationRow, capacityPrice, progress, availability);

        if (UserSession.getInstance().canManageEvents()) {
            HBox management = new HBox(10);
            management.setAlignment(Pos.CENTER_LEFT);
            management.getChildren().addAll(
                    pill("🎟 " + regCount + " bookings", "#E8F5E9", PRIMARY_DARK),
                    pill(String.format("💵 $%.2f", revenue), "#FFF7E6", ORANGE)
            );
            body.getChildren().add(management);
        }

        return body;
    }

    private HBox buildActions(Event event,
                              Runnable onView,
                              Runnable onEdit,
                              Runnable onDelete,
                              Runnable onRegister) {

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 20, 20, 20));

        Button viewBtn = ComponentFactory.smallButton("View", PRIMARY);
        viewBtn.setOnAction(e -> onView.run());
        actions.getChildren().add(viewBtn);

        if (onEdit != null) {
            Button editBtn = ComponentFactory.smallButton("Edit", BLUE);
            editBtn.setOnAction(e -> onEdit.run());
            actions.getChildren().add(editBtn);
        }

        if (onDelete != null) {
            Button deleteBtn = ComponentFactory.smallButton("Delete", RED);
            deleteBtn.setOnAction(e -> onDelete.run());
            actions.getChildren().add(deleteBtn);
        }

        if (onRegister != null) {
            String text = UserSession.getInstance().isPatient()
                    ? (event.isAvailable() ? "Register Now" : "Sold Out")
                    : "Registrations";

            Button registerBtn = ComponentFactory.smallButton(
                    text,
                    event.isAvailable() ? PRIMARY_DARK : "#9CA3AF"
            );

            if (!event.isAvailable() && UserSession.getInstance().isPatient()) {
                registerBtn.setDisable(true);
            }

            registerBtn.setOnAction(e -> onRegister.run());
            actions.getChildren().add(registerBtn);
        }

        return actions;
    }

    public HBox buildStatsBar(int eventCount, int participants,
                              int regCount, double totalRevenue) {

        HBox stats = new HBox(18);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(18));
        stats.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F6FBF7);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 18, 0, 0, 6);"
        );

        VBox events = statCard("📅", "Events", String.valueOf(eventCount), PRIMARY);
        VBox people = statCard("👥", "Participants", String.valueOf(participants), BLUE);
        VBox bookings = statCard("🎟", "Bookings", String.valueOf(regCount), PURPLE);
        VBox revenue = statCard("💵", "Revenue", String.format("$%.0f", totalRevenue), ORANGE);

        HBox.setHgrow(events, Priority.ALWAYS);
        HBox.setHgrow(people, Priority.ALWAYS);
        HBox.setHgrow(bookings, Priority.ALWAYS);
        HBox.setHgrow(revenue, Priority.ALWAYS);

        stats.getChildren().addAll(events, people, bookings, revenue);
        return stats;
    }

    private VBox statCard(String icon, String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-radius: 18;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );

        Label labelText = new Label(label);
        labelText.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        card.getChildren().addAll(iconLabel, valueLabel, labelText);
        return card;
    }

    public VBox buildEmptyState() {
        VBox empty = new VBox(12);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(50));
        empty.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F1F8E9);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 24;"
        );

        Label icon = new Label("🌿");
        icon.setStyle("-fx-font-size: 44px;");

        String message = UserSession.getInstance().isPatient()
                ? "No events available at the moment."
                : "No events found. Start by creating your first event.";

        Label title = new Label(message);
        title.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("Events will appear here once they are created.");
        subtitle.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 13px;"
        );

        empty.getChildren().addAll(icon, title, subtitle);
        return empty;
    }

    private HBox iconInfo(String icon, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }

    private Label pill(String text, String bg, String color) {
        Label pill = new Label(text);
        pill.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        return pill;
    }

    private double calculateProgress(Event event) {
        if (event.getMaxParticipants() <= 0) return 0;
        return Math.min(1.0, (double) event.getCurrentParticipants() / event.getMaxParticipants());
    }

    private String getCapacityColor(Event event) {
        double progress = calculateProgress(event);
        if (progress >= 0.9) return RED;
        if (progress >= 0.65) return ORANGE;
        return PRIMARY;
    }

    private String getTypeColor(String type) {
        if (type == null) return PRIMARY;
        return switch (type) {
            case "WORKSHOP" -> BLUE;
            case "GROUP_THERAPY" -> PRIMARY;
            case "SEMINAR" -> PURPLE;
            case "SOCIAL" -> ORANGE;
            default -> PRIMARY;
        };
    }

    private String getTypeEmoji(String type) {
        if (type == null) return "📅";
        return switch (type) {
            case "WORKSHOP" -> "🛠";
            case "GROUP_THERAPY" -> "👥";
            case "SEMINAR" -> "🎓";
            case "SOCIAL" -> "🎉";
            default -> "📅";
        };
    }

    private String getStatusEmoji(String status) {
        if (status == null) return "⚪";
        return switch (status) {
            case "UPCOMING" -> "🟢";
            case "ONGOING" -> "🔵";
            case "COMPLETED" -> "⚫";
            case "CANCELLED" -> "🔴";
            default -> "⚪";
        };
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
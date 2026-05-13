package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.services.GoogleMapsService;
import com.mentalhealth.app.utils.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class EventDetailView {

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

    public VBox buildDetail(Event event, int regCount, int totalTickets,
                            double revenue, List<EventRegistration> registrations,
                            Runnable onBack, Runnable onAddReg,
                            Consumer<EventRegistration> onEditReg,
                            Consumer<EventRegistration> onDeleteReg) {

        UserSession session = UserSession.getInstance();

        VBox detail = new VBox(22);
        detail.setPadding(new Insets(30, 34, 34, 34));
        detail.setStyle("-fx-background-color: transparent;");

        Button backBtn = ComponentFactory.styledButton("Back to Events", "#6C757D");
        backBtn.setOnAction(e -> onBack.run());

        VBox hero = buildHero(event);
        VBox infoCard = buildInfoCard(event);
        VBox mapCard = buildMapCard(event);

        detail.getChildren().addAll(backBtn, hero, infoCard, mapCard);

        if (session.isPatient()) {
            detail.getChildren().add(buildPatientStatusCard(event, registrations));
        }

        if (session.canManageEvents()) {
            VBox summaryCard = buildSummaryCard(event, regCount, totalTickets, revenue, onAddReg);
            VBox registrationsCard = buildRegistrationsList(registrations, onEditReg, onDeleteReg);

            detail.getChildren().addAll(summaryCard, registrationsCard);
        }

        return detail;
    }

    // =================== HERO ===================

    private VBox buildHero(Event event) {
        VBox hero = new VBox(14);
        hero.setPadding(new Insets(26));
        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        getTypeColor(event.getEventType()) + ", " + PRIMARY_DARK + ");" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.20), 24, 0, 0, 8);"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = whiteBadge(getTypeEmoji(event.getEventType()) + " " + safe(event.getEventType(), "EVENT"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = whiteBadge(getStatusEmoji(event.getStatus()) + " " + safe(event.getStatus(), "UPCOMING"));

        top.getChildren().addAll(typeBadge, spacer, statusBadge);

        Label title = new Label(safe(event.getTitle(), "Untitled Event"));
        title.setWrapText(true);
        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 32px;" +
                        "-fx-font-weight: 900;"
        );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        Label date = new Label("Date: " + (event.getDateTime() != null ? event.getDateTime().format(dtf) : "No date"));
        date.setWrapText(true);
        date.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.90);" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Label location = new Label("Location: " + safe(event.getLocation(), "No location"));
        location.setWrapText(true);
        location.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.82);" +
                        "-fx-font-size: 13px;"
        );

        hero.getChildren().addAll(top, title, date, location);
        return hero;
    }

    // =================== INFO CARD ===================

    private VBox buildInfoCard(Event event) {
        VBox card = modernCard();
        card.getChildren().add(sectionTitle("Event Overview"));

        HBox stats = new HBox(14);
        stats.setAlignment(Pos.CENTER_LEFT);

        stats.getChildren().addAll(
                metricCard("Capacity", event.getCurrentParticipants() + "/" + event.getMaxParticipants(), PRIMARY),
                metricCard("Available", String.valueOf(event.getAvailableSpots()), BLUE),
                metricCard("Price", event.isFree() ? "FREE" : String.format("$%.2f", event.getPrice()), PURPLE),
                metricCard("Occupancy", String.format("%.0f%%", event.getOccupancyPercentage()), ORANGE)
        );

        ProgressBar occupancy = new ProgressBar(calculateProgress(event));
        occupancy.setMaxWidth(Double.MAX_VALUE);
        occupancy.setPrefHeight(9);
        occupancy.setStyle(
                "-fx-accent: " + getCapacityColor(event) + ";" +
                        "-fx-control-inner-background: #E9ECEF;"
        );

        Label descriptionTitle = label("Description", INK, 15, true);
        Label description = label(
                safe(event.getDescription(), "No description available."),
                MUTED,
                13,
                false
        );
        description.setWrapText(true);

        VBox details = new VBox(9);
        details.getChildren().addAll(
                detailLine("Type", safe(event.getEventType(), "N/A")),
                detailLine("Status", safe(event.getStatus(), "N/A")),
                detailLine("Location", safe(event.getLocation(), "N/A")),
                detailLine("Created At", event.getCreatedAt() != null ? event.getCreatedAt().toString() : "N/A"),
                detailLine("Updated At", event.getUpdatedAt() != null ? event.getUpdatedAt().toString() : "N/A")
        );

        card.getChildren().addAll(stats, occupancy, descriptionTitle, description, details);
        return card;
    }

    // =================== PATIENT STATUS ===================

    private VBox buildPatientStatusCard(Event event, List<EventRegistration> registrations) {
        VBox card = modernCard();
        UserSession session = UserSession.getInstance();

        EventRegistration myRegistration = null;

        for (EventRegistration reg : registrations) {
            if (reg.getEmail() != null &&
                    session.getUserEmail() != null &&
                    reg.getEmail().equalsIgnoreCase(session.getUserEmail())) {
                myRegistration = reg;
                break;
            }
        }

        if (myRegistration != null) {
            card.getChildren().add(sectionTitle("You Are Registered"));

            VBox inner = softPanel("#E8F5E9");

            Label confirm = label(
                    "Confirmation: " + myRegistration.getFormattedConfirmationNumber(),
                    PRIMARY_DARK,
                    16,
                    true
            );

            Label status = label(
                    myRegistration.getStatusEmoji() + " Status: " + safe(myRegistration.getStatus(), "N/A"),
                    getStatusColor(myRegistration.getStatus()),
                    14,
                    true
            );

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

            inner.getChildren().addAll(
                    confirm,
                    status,
                    label("Ticket: " + myRegistration.getTicketType() + " x " + myRegistration.getNumberOfTickets(), TEXT, 13, false),
                    label("Registered on: " + (myRegistration.getRegistrationDate() != null ? myRegistration.getRegistrationDate().format(dtf) : "N/A"), MUTED, 13, false),
                    label("Your QR code and ticket details are available in your ticket section.", MUTED, 12, false)
            );

            card.getChildren().add(inner);
        } else {
            card.getChildren().add(sectionTitle("Registration"));

            VBox inner = softPanel(event.isAvailable() ? "#F1F8E9" : "#FFEBEE");

            if (event.isAvailable()) {
                inner.getChildren().addAll(
                        label(event.getAvailableSpots() + " spots available", PRIMARY_DARK, 17, true),
                        label(event.isFree() ? "This event is free." : String.format("Price: $%.2f", event.getPrice()), TEXT, 13, false),
                        label("Go back to the events list and click Register Now to join.", MUTED, 13, false)
                );
            } else {
                inner.getChildren().addAll(
                        label("Sold Out", RED, 18, true),
                        label("This event has reached maximum capacity.", MUTED, 13, false)
                );
            }

            card.getChildren().add(inner);
        }

        return card;
    }

    // =================== SUMMARY ===================

    private VBox buildSummaryCard(Event event, int regCount, int totalTickets, double revenue, Runnable onAddReg) {
        VBox card = modernCard();
        card.getChildren().add(sectionTitle("Registration Summary"));

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                metricCard("Bookings", String.valueOf(regCount), PRIMARY),
                metricCard("Tickets", String.valueOf(totalTickets), BLUE),
                metricCard("Revenue", String.format("$%.2f", revenue), ORANGE),
                metricCard("Available", String.valueOf(event.getAvailableSpots()), PURPLE)
        );

        Button addRegBtn = ComponentFactory.styledButton("+ New Registration", PRIMARY_DARK);
        addRegBtn.setOnAction(e -> onAddReg.run());

        if (!event.isAvailable()) {
            addRegBtn.setDisable(true);
            addRegBtn.setText("Sold Out");
        }

        card.getChildren().addAll(row, addRegBtn);
        return card;
    }

    private VBox buildRegistrationsList(List<EventRegistration> registrations,
                                        Consumer<EventRegistration> onEditReg,
                                        Consumer<EventRegistration> onDeleteReg) {

        VBox card = modernCard();
        card.getChildren().add(sectionTitle("All Registrations (" + registrations.size() + ")"));

        if (registrations.isEmpty()) {
            VBox empty = softPanel("#F6FBF7");
            empty.setAlignment(Pos.CENTER);
            empty.getChildren().addAll(
                    label("No registrations yet.", INK, 15, true),
                    label("Registrations will appear here once users book the event.", MUTED, 13, false)
            );
            card.getChildren().add(empty);
        } else {
            for (EventRegistration reg : registrations) {
                card.getChildren().add(buildRegistrationCard(reg, onEditReg, onDeleteReg));
            }
        }

        return card;
    }

    // =================== MAP ===================

    private VBox buildMapCard(Event event) {
        VBox mapCard = modernCard();

        String location = event.getLocation();

        if (isOnlineEvent(location)) {
            return buildOnlineEventCard(mapCard, location);
        }

        return buildPhysicalEventCard(mapCard, event);
    }

    private boolean isOnlineEvent(String location) {
        if (location == null || location.trim().isEmpty()) {
            return false;
        }

        String value = location.toLowerCase();

        return value.contains("online") ||
                value.contains("en ligne") ||
                value.contains("virtual") ||
                value.contains("zoom") ||
                value.contains("teams") ||
                value.contains("meet") ||
                value.contains("webinar") ||
                value.contains("webex") ||
                value.contains("discord") ||
                value.contains("skype");
    }

    private VBox buildOnlineEventCard(VBox mapCard, String location) {
        mapCard.getChildren().add(sectionTitle("Online Event"));

        VBox onlineInfo = softPanel("#F1F8E9");
        onlineInfo.setAlignment(Pos.CENTER);

        Label icon = new Label("🌐");
        icon.setStyle("-fx-font-size: 46px;");

        Label text = label("This is an online event", PRIMARY_DARK, 17, true);
        Label locationText = label(safe(location, "Meeting link will be provided."), MUTED, 13, false);
        locationText.setWrapText(true);
        locationText.setMaxWidth(420);

        Label hint = label("Registered users should check their email for access details.", MUTED, 12, false);

        onlineInfo.getChildren().addAll(icon, text, locationText, hint);
        mapCard.getChildren().add(onlineInfo);

        if (containsUrl(location)) {
            Button joinBtn = ComponentFactory.styledButton("Join Meeting", PRIMARY_DARK);
            joinBtn.setOnAction(e -> openUrl(extractUrl(location)));
            mapCard.getChildren().add(joinBtn);
        }

        return mapCard;
    }

    private VBox buildPhysicalEventCard(VBox mapCard, Event event) {
        mapCard.getChildren().add(sectionTitle("Event Location"));

        Label locationLabel = label(safe(event.getLocation(), "No location specified."), MUTED, 13, false);
        locationLabel.setWrapText(true);
        mapCard.getChildren().add(locationLabel);

        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            VBox noLocation = softPanel("#F6FBF7");
            noLocation.setAlignment(Pos.CENTER);
            noLocation.getChildren().addAll(
                    label("No location specified", INK, 15, true),
                    label("Add a location to enable map preview.", MUTED, 13, false)
            );
            mapCard.getChildren().add(noLocation);
            return mapCard;
        }

        StackPane mapContainer = new StackPane();
        mapContainer.setPrefSize(520, 260);
        mapContainer.setMaxWidth(560);
        mapContainer.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 20;"
        );
        mapContainer.setAlignment(Pos.CENTER);

        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(48, 48);
        mapContainer.getChildren().add(loading);

        if (GoogleMapsService.isConfigured()) {
            loadMapImage(mapContainer, event.getLocation());
        } else {
            showMapPlaceholder(mapContainer);
        }

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button openMaps = ComponentFactory.smallButton("Open in Maps", PRIMARY_DARK);
        openMaps.setOnAction(e -> openUrl(GoogleMapsService.getSearchUrl(event.getLocation())));

        Button directions = ComponentFactory.smallButton("Directions", PRIMARY);
        directions.setOnAction(e -> openUrl(GoogleMapsService.getDirectionsUrl(event.getLocation())));

        buttons.getChildren().addAll(openMaps, directions);

        mapCard.getChildren().addAll(mapContainer, buttons);
        return mapCard;
    }

    private void loadMapImage(StackPane mapContainer, String location) {
        Image mapImage = GoogleMapsService.getMapImage(location, 520, 260, 15);

        if (mapImage == null) {
            showMapPlaceholder(mapContainer);
            return;
        }

        ImageView mapView = new ImageView();
        mapView.setFitWidth(520);
        mapView.setFitHeight(260);
        mapView.setPreserveRatio(false);
        mapView.setStyle("-fx-background-radius: 20;");

        mapImage.progressProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 1.0 && !mapImage.isError()) {
                mapView.setImage(mapImage);
                mapContainer.getChildren().clear();
                mapContainer.getChildren().add(mapView);
            }
        });

        mapImage.errorProperty().addListener((obs, oldValue, error) -> {
            if (error) {
                showMapPlaceholder(mapContainer);
            }
        });

        if (mapImage.getProgress() >= 1.0 && !mapImage.isError()) {
            mapView.setImage(mapImage);
            mapContainer.getChildren().clear();
            mapContainer.getChildren().add(mapView);
        }
    }

    private void showMapPlaceholder(StackPane mapContainer) {
        mapContainer.getChildren().clear();

        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);

        Label icon = new Label("🗺");
        icon.setStyle("-fx-font-size: 42px;");

        placeholder.getChildren().addAll(
                icon,
                label("Map preview not available", INK, 15, true),
                label("Use the buttons below to open the location externally.", MUTED, 12, false)
        );

        mapContainer.getChildren().add(placeholder);
    }

    // =================== REGISTRATION CARD ===================

    public VBox buildRegistrationCard(EventRegistration reg,
                                      Consumer<EventRegistration> onEdit,
                                      Consumer<EventRegistration> onDelete) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.06), 10, 0, 0, 3);"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label user = label("User: " + safe(reg.getUserName(), "Unknown"), INK, 14, true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = miniPill(
                reg.getStatusEmoji() + " " + safe(reg.getStatus(), "N/A"),
                getStatusBg(reg.getStatus()),
                getStatusColor(reg.getStatus())
        );

        top.getChildren().addAll(user, spacer, status);

        Label confirmation = label("Confirmation: " + reg.getFormattedConfirmationNumber(), PRIMARY_DARK, 13, true);

        HBox contact = new HBox(18);
        contact.setAlignment(Pos.CENTER_LEFT);
        contact.getChildren().addAll(
                label("Email: " + safe(reg.getEmail(), "N/A"), MUTED, 12, false),
                label("Phone: " + safe(reg.getPhone(), "N/A"), MUTED, 12, false)
        );

        HBox ticket = new HBox(12);
        ticket.setAlignment(Pos.CENTER_LEFT);
        ticket.getChildren().addAll(
                miniPill("Ticket: " + safe(reg.getTicketType(), "STANDARD"), "#E8F5E9", PRIMARY_DARK),
                miniPill("Qty: " + reg.getNumberOfTickets(), "#EAF5FF", BLUE),
                miniPill(reg.isFreeTicket() ? "Free" : String.format("$%.2f", reg.getTotalPrice()), "#FFF7E6", ORANGE)
        );

        Label payment = label("Payment: " + safe(reg.getPaymentMethod(), "N/A"), MUTED, 12, false);

        card.getChildren().addAll(top, confirmation, contact, ticket, payment);

        if (reg.getSpecialRequests() != null && !reg.getSpecialRequests().trim().isEmpty()) {
            Label requests = label("Notes: " + reg.getSpecialRequests(), MUTED, 12, false);
            requests.setWrapText(true);
            card.getChildren().add(requests);
        }

        if (reg.getRegistrationDate() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            card.getChildren().add(label("Registered on: " + reg.getRegistrationDate().format(dtf), MUTED, 11, false));
        }

        if (onEdit != null && onDelete != null) {
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button edit = ComponentFactory.smallButton("Edit", BLUE);
            edit.setOnAction(e -> onEdit.accept(reg));

            Button delete = ComponentFactory.smallButton("Delete", RED);
            delete.setOnAction(e -> onDelete.accept(reg));

            actions.getChildren().addAll(edit, delete);
            card.getChildren().add(actions);
        }

        return card;
    }

    // =================== HELPERS ===================

    private VBox modernCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 26;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 18, 0, 0, 6);"
        );
        return card;
    }

    private VBox softPanel(String bg) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18));
        panel.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #DDE5E2;" +
                        "-fx-border-radius: 18;"
        );
        return panel;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: 900;"
        );
        return label;
    }

    private VBox metricCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-radius: 18;"
        );

        Label valueLabel = label(value, color, 22, true);
        Label nameLabel = label(label, MUTED, 12, false);

        card.getChildren().addAll(valueLabel, nameLabel);
        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    private HBox detailLine(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        Label name = label(label + ":", PRIMARY_DARK, 13, true);
        name.setMinWidth(110);

        Label val = label(value, TEXT, 13, false);
        val.setWrapText(true);

        row.getChildren().addAll(name, val);
        return row;
    }

    private Label label(String text, String color, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;" +
                        (bold ? "-fx-font-weight: bold;" : "")
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

    private Label miniPill(String text, String bg, String color) {
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

    private String getStatusColor(String status) {
        if (status == null) return "#9CA3AF";

        return switch (status) {
            case "CONFIRMED", "UPCOMING" -> PRIMARY_DARK;
            case "PENDING", "ONGOING" -> ORANGE;
            case "CANCELLED" -> RED;
            case "COMPLETED" -> MUTED;
            default -> "#9CA3AF";
        };
    }

    private String getStatusBg(String status) {
        if (status == null) return "#F3F4F6";

        return switch (status) {
            case "CONFIRMED", "UPCOMING" -> "#E8F5E9";
            case "PENDING", "ONGOING" -> "#FFF7E6";
            case "CANCELLED" -> "#FFECEC";
            case "COMPLETED" -> "#F3F4F6";
            default -> "#F3F4F6";
        };
    }

    private boolean containsUrl(String text) {
        return text != null &&
                (text.contains("http") || text.contains("www.") || text.contains(".com"));
    }

    private String extractUrl(String text) {
        if (text == null) return "";

        String[] words = text.split("\\s+");

        for (String word : words) {
            if (word.contains("http") || word.contains("www.")) {
                if (!word.startsWith("http")) {
                    return "https://" + word;
                }
                return word;
            }
        }

        return text;
    }

    private void openUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) return;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Could not open URL: " + e.getMessage());
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
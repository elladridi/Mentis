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

    public VBox buildDetail(Event event, int regCount, int totalTickets,
                            double revenue, List<EventRegistration> registrations,
                            Runnable onBack, Runnable onAddReg,
                            Consumer<EventRegistration> onEditReg,
                            Consumer<EventRegistration> onDeleteReg) {

        UserSession session = UserSession.getInstance();

        VBox detail = new VBox(20);
        detail.setPadding(new Insets(30));
        detail.setStyle("-fx-background-color: #FFFFFF;");

        Button backBtn = ComponentFactory.styledButton("← Back to Events", "#6B7280");
        backBtn.setOnAction(e -> onBack.run());

        Label title = ComponentFactory.pageTitle(event.getTitle());
        title.setWrapText(true);

        // Info card
        VBox infoCard = ComponentFactory.darkCard();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        infoCard.getChildren().addAll(
                ComponentFactory.detailRow("📅 Date & Time", event.getDateTime().format(dtf)),
                ComponentFactory.detailRow("📍 Location", event.getLocation()),
                ComponentFactory.detailRow("🏷 Type", event.getEventType()),
                ComponentFactory.detailRow("📊 Status", event.getStatus()),
                ComponentFactory.detailRow("👥 Capacity",
                        event.getCurrentParticipants() + " / " + event.getMaxParticipants() +
                                " (" + event.getAvailableSpots() + " spots left)"),
                ComponentFactory.detailRow("💰 Price",
                        event.isFree() ? "Free" : String.format("$%.2f", event.getPrice())),
                ComponentFactory.detailRow("📝 Description",
                        event.getDescription() != null ? event.getDescription() : "N/A"));

        // Map card (or online event card)
        VBox mapCard = buildMapCard(event);

        // Add basic cards
        detail.getChildren().addAll(backBtn, title, infoCard, mapCard);

        // ===== PATIENT VIEW =====
        if (session.isPatient()) {
            // Check if patient is already registered
            VBox patientStatusCard = buildPatientStatusCard(event, registrations);
            detail.getChildren().add(patientStatusCard);
        }

        // ===== ADMIN/PSYCHOLOGIST VIEW =====
        if (session.canManageEvents()) {
            // Registration summary
            VBox summaryCard = ComponentFactory.darkCard();
            summaryCard.getChildren().add(ComponentFactory.sectionTitle("🎟 Registration Summary"));

            HBox summaryRow = new HBox(30);
            summaryRow.setAlignment(Pos.CENTER_LEFT);
            summaryRow.getChildren().addAll(
                    ComponentFactory.statItem("🎟 Bookings", String.valueOf(regCount)),
                    ComponentFactory.verticalSeparator(),
                    ComponentFactory.statItem("🎫 Tickets", String.valueOf(totalTickets)),
                    ComponentFactory.verticalSeparator(),
                    ComponentFactory.statItem("💵 Revenue", String.format("$%.2f", revenue)),
                    ComponentFactory.verticalSeparator(),
                    ComponentFactory.statItem("🟢 Available",
                            String.valueOf(event.getAvailableSpots())));
            summaryCard.getChildren().add(summaryRow);

            Button addRegBtn = ComponentFactory.styledButton("+ New Registration", "#9BC7B5");
            addRegBtn.setOnAction(e -> onAddReg.run());
            if (!event.isAvailable()) {
                addRegBtn.setDisable(true);
                addRegBtn.setText("🔴 SOLD OUT");
            }
            summaryCard.getChildren().add(addRegBtn);

            detail.getChildren().add(summaryCard);

            // Registration list - ONLY for admin/psychologist
            VBox regCard = ComponentFactory.darkCard();
            regCard.getChildren().add(ComponentFactory.sectionTitle("📋 All Registrations (" + registrations.size() + ")"));

            if (registrations.isEmpty()) {
                Label noReg = new Label("No registrations yet.");
                noReg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
                regCard.getChildren().add(noReg);
            } else {
                for (EventRegistration reg : registrations) {
                    regCard.getChildren().add(buildRegistrationCard(reg, onEditReg, onDeleteReg));
                }
            }

            detail.getChildren().add(regCard);
        }

        return detail;
    }

    // ========== PATIENT STATUS CARD ==========
    private VBox buildPatientStatusCard(Event event, List<EventRegistration> registrations) {
        UserSession session = UserSession.getInstance();
        VBox statusCard = ComponentFactory.darkCard();

        // Check if patient is registered
        EventRegistration myRegistration = null;
        for (EventRegistration reg : registrations) {
            if (reg.getEmail() != null && reg.getEmail().equals(session.getUserEmail())) {
                myRegistration = reg;
                break;
            }
        }

        if (myRegistration != null) {
            // Patient is registered
            statusCard.getChildren().add(ComponentFactory.sectionTitle("✅ You're Registered!"));

            VBox regInfo = new VBox(10);
            regInfo.setPadding(new Insets(15));
            regInfo.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 10;");

            Label confirmLabel = new Label("🎟 Confirmation #: REG-" + String.format("%06d", myRegistration.getId()));
            confirmLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label statusLabel = new Label(myRegistration.getStatusEmoji() + " Status: " + myRegistration.getStatus());
            statusLabel.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 14px;");

            Label ticketLabel = new Label("🎫 " + myRegistration.getTicketType() + " × " + myRegistration.getNumberOfTickets());
            ticketLabel.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 14px;");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            Label dateLabel = new Label("📅 Registered on: " +
                    (myRegistration.getRegistrationDate() != null ?
                            myRegistration.getRegistrationDate().format(dtf) : "N/A"));
            dateLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

            regInfo.getChildren().addAll(confirmLabel, statusLabel, ticketLabel, dateLabel);
            statusCard.getChildren().add(regInfo);

            // Hint
            Label hint = new Label("📧 Check your email for the QR code and ticket details");
            hint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
            hint.setPadding(new Insets(10, 0, 0, 0));
            statusCard.getChildren().add(hint);

        } else {
            // Patient is NOT registered
            statusCard.getChildren().add(ComponentFactory.sectionTitle("🎟 Registration"));

            if (event.isAvailable()) {
                VBox availInfo = new VBox(10);
                availInfo.setAlignment(Pos.CENTER);
                availInfo.setPadding(new Insets(20));
                availInfo.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");

                Label spotsLabel = new Label("🟢 " + event.getAvailableSpots() + " spots available");
                spotsLabel.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 16px; -fx-font-weight: bold;");

                Label priceLabel = new Label(event.isFree() ? "🆓 This event is FREE!" :
                        String.format("💰 Price: $%.2f", event.getPrice()));
                priceLabel.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 14px;");

                Label infoLabel = new Label("Click 'Register Now' from the events list to sign up!");
                infoLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

                availInfo.getChildren().addAll(spotsLabel, priceLabel, infoLabel);
                statusCard.getChildren().add(availInfo);
            } else {
                VBox soldOutInfo = new VBox(10);
                soldOutInfo.setAlignment(Pos.CENTER);
                soldOutInfo.setPadding(new Insets(20));
                soldOutInfo.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 10;");

                Label soldOutLabel = new Label("🔴 SOLD OUT");
                soldOutLabel.setStyle("-fx-text-fill: #D62828; -fx-font-size: 18px; -fx-font-weight: bold;");

                Label infoLabel = new Label("This event has reached maximum capacity.");
                infoLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

                soldOutInfo.getChildren().addAll(soldOutLabel, infoLabel);
                statusCard.getChildren().add(soldOutInfo);
            }
        }

        return statusCard;
    }

    // ========== BUILD MAP CARD ==========
    private VBox buildMapCard(Event event) {
        VBox mapCard = ComponentFactory.darkCard();

        String location = event.getLocation();
        if (isOnlineEvent(location)) {
            return buildOnlineEventCard(mapCard, location);
        }

        return buildPhysicalEventCard(mapCard, event);
    }

    // ========== CHECK IF ONLINE EVENT ==========
    private boolean isOnlineEvent(String location) {
        if (location == null || location.trim().isEmpty()) {
            return false;
        }

        String locationLower = location.toLowerCase();
        return locationLower.contains("online") ||
                locationLower.contains("en ligne") ||
                locationLower.contains("virtual") ||
                locationLower.contains("zoom") ||
                locationLower.contains("teams") ||
                locationLower.contains("meet") ||
                locationLower.contains("webinar") ||
                locationLower.contains("webex") ||
                locationLower.contains("discord") ||
                locationLower.contains("skype");
    }

    // ========== ONLINE EVENT CARD ==========
    private VBox buildOnlineEventCard(VBox mapCard, String location) {
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label mapTitle = ComponentFactory.sectionTitle("💻 Online Event");
        titleRow.getChildren().add(mapTitle);
        mapCard.getChildren().add(titleRow);

        VBox onlineInfo = new VBox(10);
        onlineInfo.setAlignment(Pos.CENTER);
        onlineInfo.setPadding(new Insets(30));
        onlineInfo.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");

        Label icon = new Label("🌐");
        icon.setStyle("-fx-font-size: 50px;");

        Label text = new Label("This is an online event");
        text.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label locationText = new Label(location != null ? location : "Link will be provided");
        locationText.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        locationText.setWrapText(true);
        locationText.setMaxWidth(350);

        Label hint = new Label("📧 Check your email for the meeting link after registration");
        hint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        hint.setWrapText(true);

        onlineInfo.getChildren().addAll(icon, text, locationText, hint);
        mapCard.getChildren().add(onlineInfo);

        if (location != null && containsUrl(location)) {
            HBox buttonRow = new HBox(10);
            buttonRow.setAlignment(Pos.CENTER);
            buttonRow.setPadding(new Insets(10, 0, 0, 0));

            Button joinBtn = ComponentFactory.styledButton("🔗 Join Meeting", "#2F5D52");
            joinBtn.setOnAction(e -> {
                try {
                    String url = extractUrl(location);
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    System.err.println("Could not open link: " + ex.getMessage());
                }
            });
            buttonRow.getChildren().add(joinBtn);
            mapCard.getChildren().add(buttonRow);
        }

        return mapCard;
    }

    // ========== PHYSICAL EVENT CARD (WITH MAP) ==========
    private VBox buildPhysicalEventCard(VBox mapCard, Event event) {
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label mapTitle = ComponentFactory.sectionTitle("📍 Event Location");
        titleRow.getChildren().add(mapTitle);
        mapCard.getChildren().add(titleRow);

        Label locationLabel = new Label(event.getLocation());
        locationLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        locationLabel.setWrapText(true);
        mapCard.getChildren().add(locationLabel);

        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            Label noLocation = new Label("📍 No location specified");
            noLocation.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
            mapCard.getChildren().add(noLocation);
            return mapCard;
        }

        StackPane mapContainer = new StackPane();
        mapContainer.setPrefSize(400, 250);
        mapContainer.setMaxWidth(500);
        mapContainer.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;");
        mapContainer.setAlignment(Pos.CENTER);

        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(50, 50);
        mapContainer.getChildren().add(loading);

        if (GoogleMapsService.isConfigured()) {
            loadMapImage(mapContainer, event.getLocation());
        } else {
            showMapPlaceholder(mapContainer);
        }

        mapCard.getChildren().add(mapContainer);

        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);
        buttonsRow.setPadding(new Insets(10, 0, 0, 0));

        Button openMapsBtn = ComponentFactory.smallButton("🗺️ Open in Google Maps", "#2F5D52");
        openMapsBtn.setOnAction(e -> {
            try {
                String url = GoogleMapsService.getSearchUrl(event.getLocation());
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                System.err.println("Could not open browser: " + ex.getMessage());
            }
        });

        Button directionsBtn = ComponentFactory.smallButton("🧭 Get Directions", "#3E6F64");
        directionsBtn.setOnAction(e -> {
            try {
                String url = GoogleMapsService.getDirectionsUrl(event.getLocation());
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                System.err.println("Could not open browser: " + ex.getMessage());
            }
        });

        buttonsRow.getChildren().addAll(openMapsBtn, directionsBtn);
        mapCard.getChildren().add(buttonsRow);

        return mapCard;
    }

    // ========== LOAD MAP IMAGE ==========
    private void loadMapImage(StackPane mapContainer, String location) {
        Image mapImage = GoogleMapsService.getMapImage(location, 400, 250, 15);

        if (mapImage != null) {
            ImageView mapView = new ImageView();
            mapView.setFitWidth(400);
            mapView.setFitHeight(250);
            mapView.setPreserveRatio(true);
            mapView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

            mapImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !mapImage.isError()) {
                    mapView.setImage(mapImage);
                    mapContainer.getChildren().clear();
                    mapContainer.getChildren().add(mapView);
                }
            });

            mapImage.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    System.err.println("❌ Map load error: " + mapImage.getException());
                    mapContainer.getChildren().clear();
                    VBox errorBox = new VBox(5);
                    errorBox.setAlignment(Pos.CENTER);
                    Label errorIcon = new Label("🗺️");
                    errorIcon.setStyle("-fx-font-size: 30px;");
                    Label errorLabel = new Label("Could not load map");
                    errorLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
                    errorBox.getChildren().addAll(errorIcon, errorLabel);
                    mapContainer.getChildren().add(errorBox);
                }
            });

            if (mapImage.getProgress() >= 1.0 && !mapImage.isError()) {
                mapView.setImage(mapImage);
                mapContainer.getChildren().clear();
                mapContainer.getChildren().add(mapView);
            }
        } else {
            showMapPlaceholder(mapContainer);
        }
    }

    // ========== MAP PLACEHOLDER ==========
    private void showMapPlaceholder(StackPane mapContainer) {
        mapContainer.getChildren().clear();
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        Label icon = new Label("🗺️");
        icon.setStyle("-fx-font-size: 40px;");
        Label text = new Label("Map preview not available");
        text.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
        Label hint = new Label("Click buttons below to view location");
        hint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        placeholder.getChildren().addAll(icon, text, hint);
        mapContainer.getChildren().add(placeholder);
    }

    // ========== URL HELPERS ==========
    private boolean containsUrl(String text) {
        return text != null && (text.contains("http") || text.contains("www.") || text.contains(".com"));
    }

    private String extractUrl(String text) {
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

    // ========== REGISTRATION CARD (for admin/psychologist only) ==========
    public VBox buildRegistrationCard(EventRegistration reg,
                                      Consumer<EventRegistration> onEdit,
                                      Consumer<EventRegistration> onDelete) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label userLbl = new Label("👤 " + reg.getUserName());
        userLbl.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 14px; -fx-font-weight: bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label dateLbl = ComponentFactory.subText(
                reg.getRegistrationDate() != null ?
                        reg.getRegistrationDate().format(
                                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "");
        topRow.getChildren().addAll(userLbl, sp, dateLbl);

        String statusColor = switch (reg.getStatus()) {
            case "CONFIRMED" -> "#3E6F64";
            case "PENDING" -> "#AFCFC2";
            case "CANCELLED" -> "#D62828";
            default -> "#9CA3AF";
        };
        Label statusLbl = new Label(reg.getStatusEmoji() + " " + reg.getStatus());
        statusLbl.setStyle("-fx-text-fill:" + statusColor +
                "; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox contactRow = new HBox(20);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getChildren().addAll(
                ComponentFactory.infoText("📧 " + reg.getEmail()),
                ComponentFactory.infoText("📱 " +
                        (reg.getPhone() != null ? reg.getPhone() : "N/A")));

        HBox ticketRow = new HBox(20);
        ticketRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLbl = new Label("🎫 " + reg.getTicketType());
        typeLbl.setStyle("-fx-text-fill:" +
                ("VIP".equals(reg.getTicketType()) ? "#2F5D52" : "#3E6F64") +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label countLbl = ComponentFactory.infoText("× " + reg.getNumberOfTickets() + " ticket(s)");
        Label priceLbl = new Label(
                reg.isFreeTicket() ? "🆓 Free" :
                        String.format("💰 $%.2f", reg.getTotalPrice()));
        priceLbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 12px; -fx-font-weight: bold;");
        ticketRow.getChildren().addAll(typeLbl, countLbl, priceLbl);

        HBox payRow = new HBox(20);
        payRow.setAlignment(Pos.CENTER_LEFT);
        payRow.getChildren().add(ComponentFactory.infoText("💳 " +
                (reg.getPaymentMethod() != null ? reg.getPaymentMethod() : "N/A")));
        if (reg.getSpecialRequests() != null && !reg.getSpecialRequests().isEmpty()) {
            Label reqLbl = new Label("📝 " + reg.getSpecialRequests());
            reqLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
            reqLbl.setWrapText(true);
            payRow.getChildren().add(reqLbl);
        }

        card.getChildren().addAll(topRow, statusLbl, contactRow, ticketRow, payRow);

        // Action buttons - only for admin/psychologist
        if (onEdit != null && onDelete != null) {
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button editBtn = ComponentFactory.smallButton("✏ Edit", "#3E6F64");
            editBtn.setOnAction(e -> onEdit.accept(reg));
            Button delBtn = ComponentFactory.smallButton("🗑 Delete", "#D62828");
            delBtn.setOnAction(e -> onDelete.accept(reg));
            actions.getChildren().addAll(editBtn, delBtn);
            card.getChildren().add(actions);
        }

        return card;
    }
}
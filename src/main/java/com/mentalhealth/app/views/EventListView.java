package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.utils.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class EventListView {



    public VBox buildCard(Event event, int regCount, double revenue,
                          Runnable onView, Runnable onEdit,
                          Runnable onDelete, Runnable onRegister) {

        VBox card = new VBox(12);
        card.setPrefWidth(360);
        card.setPadding(new Insets(20));
        card.setStyle(ComponentFactory.cardStyle());
        card.setOnMouseEntered(e -> card.setStyle(ComponentFactory.cardHoverStyle()));
        card.setOnMouseExited(e -> card.setStyle(ComponentFactory.cardStyle()));

        // Top row - type badge and status
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label(event.getEventType());
        typeBadge.setStyle("-fx-background-color: #AFCFC2; -fx-text-fill: #2F5D52;" +
                "-fx-padding: 4 12; -fx-background-radius: 12;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        String sc = switch (event.getStatus()) {
            case "UPCOMING" -> "#3E6F64";
            case "ONGOING" -> "#9BC7B5";
            case "COMPLETED" -> "#9CA3AF";
            case "CANCELLED" -> "#D62828";
            default -> "#9CA3AF";
        };
        Label statusLbl = new Label(event.getStatus());
        statusLbl.setStyle("-fx-text-fill:" + sc + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        topRow.getChildren().addAll(typeBadge, sp, statusLbl);

        // Title
        Label titleLbl = new Label(event.getTitle());
        titleLbl.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLbl.setWrapText(true);

        // Description
        String dt = event.getDescription() != null ?
                (event.getDescription().length() > 100 ?
                        event.getDescription().substring(0, 100) + "..." :
                        event.getDescription()) : "";
        Label descLbl = new Label(dt);
        descLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        descLbl.setWrapText(true);

        // Date and location
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm");
        Label dateLbl = ComponentFactory.infoText("📅 " + event.getDateTime().format(dtf));
        Label locLbl = ComponentFactory.infoText("📍 " + event.getLocation());

        // Capacity and price
        HBox capPrice = new HBox(15);
        capPrice.setAlignment(Pos.CENTER_LEFT);
        Label capLbl = ComponentFactory.infoText(
                "👥 " + event.getCurrentParticipants() + "/" + event.getMaxParticipants());
        Label priceLbl = new Label(
                event.isFree() ? "🆓 Free" : String.format("💰 $%.2f", event.getPrice()));
        priceLbl.setStyle("-fx-text-fill:" +
                (event.isFree() ? "#3E6F64" : "#2F5D52") +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");
        capPrice.getChildren().addAll(capLbl, priceLbl);

        // Registration info - only show for admin/psychologist
        HBox regRow = new HBox(15);
        regRow.setAlignment(Pos.CENTER_LEFT);
        if (UserSession.getInstance().canManageEvents()) {
            Label regLbl = new Label("🎟 " + regCount + " registrations");
            regLbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label revLbl = new Label(String.format("💵 $%.2f revenue", revenue));
            revLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            regRow.getChildren().addAll(regLbl, revLbl);
        }

        // Availability
        Label availLbl;
        if (event.isAvailable()) {
            availLbl = new Label("🟢 " + event.getAvailableSpots() + " spots available");
            availLbl.setStyle("-fx-text-fill: #3E6F64; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            availLbl = new Label("🔴 SOLD OUT");
            availLbl.setStyle("-fx-text-fill: #D62828; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #DDE5E2;");

        // Action buttons - role-based
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        // View button - always visible
        Button viewBtn = ComponentFactory.smallButton("👁 View", "#9BC7B5");
        viewBtn.setOnAction(e -> onView.run());
        actions.getChildren().add(viewBtn);

        // Edit button - only for admin/psychologist
        if (onEdit != null) {
            Button editBtn = ComponentFactory.smallButton("✏ Edit", "#3E6F64");
            editBtn.setOnAction(e -> onEdit.run());
            actions.getChildren().add(editBtn);
        }

        // Delete button - only for admin/psychologist
        if (onDelete != null) {
            Button delBtn = ComponentFactory.smallButton("🗑 Delete", "#D62828");
            delBtn.setOnAction(e -> onDelete.run());
            actions.getChildren().add(delBtn);
        }

        // Register button
        if (onRegister != null) {
            String regBtnText;
            String regBtnColor;
            if (UserSession.getInstance().isPatient()) {
                regBtnText = event.isAvailable() ? "🎟 Register Now" : "🔴 Sold Out";
                regBtnColor = event.isAvailable() ? "#2F5D52" : "#9CA3AF";
            } else {
                regBtnText = "🎟 Register";
                regBtnColor = "#2F5D52";
            }

            Button regBtn = ComponentFactory.smallButton(regBtnText, regBtnColor);
            if (!event.isAvailable() && UserSession.getInstance().isPatient()) {
                regBtn.setDisable(true);
            }
            regBtn.setOnAction(e -> onRegister.run());
            actions.getChildren().add(regBtn);
        }

        // Build the card
        card.getChildren().addAll(topRow, titleLbl, descLbl, dateLbl, locLbl, capPrice);

        // Only add registration row if it has content
        if (!regRow.getChildren().isEmpty()) {
            card.getChildren().add(regRow);
        }

        card.getChildren().addAll(availLbl, sep, actions);
        return card;
    }

    public HBox buildStatsBar(int eventCount, int participants,
                              int regCount, double totalRevenue) {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(15));
        stats.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 12;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 12;");
        stats.getChildren().addAll(
                ComponentFactory.statItem("📅 Events", String.valueOf(eventCount)),
                ComponentFactory.verticalSeparator(),
                ComponentFactory.statItem("👥 Participants", String.valueOf(participants)),
                ComponentFactory.verticalSeparator(),
                ComponentFactory.statItem("🎟 Bookings", String.valueOf(regCount)),
                ComponentFactory.verticalSeparator(),
                ComponentFactory.statItem("💵 Revenue", String.format("$%.0f", totalRevenue))
        );
        return stats;
    }

    public Label buildEmptyState() {
        UserSession session = UserSession.getInstance();
        String message;
        if (session.isPatient()) {
            message = "No events available at the moment. Check back later!";
        } else {
            message = "No events found. Click '+ Add Event' to create one!";
        }
        Label empty = new Label(message);
        empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 16px;");
        return empty;
    }
}
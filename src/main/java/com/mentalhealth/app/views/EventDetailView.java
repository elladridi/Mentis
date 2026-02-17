package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class EventDetailView {

    public VBox buildDetail(Event event, int regCount, int totalTickets,
                            double revenue, List<EventRegistration> registrations,
                            Runnable onBack, Runnable onAddReg,
                            Consumer<EventRegistration> onEditReg,
                            Consumer<EventRegistration> onDeleteReg) {

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

        // Registration list
        VBox regCard = ComponentFactory.darkCard();
        regCard.getChildren().add(ComponentFactory.sectionTitle("📋 All Registrations"));

        if (registrations.isEmpty()) {
            Label noReg = new Label("No registrations yet. Be the first to sign up!");
            noReg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
            regCard.getChildren().add(noReg);
        } else {
            for (EventRegistration reg : registrations) {
                regCard.getChildren().add(buildRegistrationCard(reg, onEditReg, onDeleteReg));
            }
        }

        detail.getChildren().addAll(backBtn, title, infoCard, summaryCard, regCard);
        return detail;
    }

    public VBox buildRegistrationCard(EventRegistration reg,
                                      Consumer<EventRegistration> onEdit,
                                      Consumer<EventRegistration> onDelete) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 10;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        // Name + date
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

        // Status
        String statusColor = switch (reg.getStatus()) {
            case "CONFIRMED" -> "#3E6F64";
            case "PENDING" -> "#AFCFC2";
            case "CANCELLED" -> "#D62828";
            default -> "#9CA3AF";
        };
        Label statusLbl = new Label(reg.getStatusEmoji() + " " + reg.getStatus());
        statusLbl.setStyle("-fx-text-fill:" + statusColor +
                "; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Contact
        HBox contactRow = new HBox(20);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getChildren().addAll(
                ComponentFactory.infoText("📧 " + reg.getEmail()),
                ComponentFactory.infoText("📱 " +
                        (reg.getPhone() != null ? reg.getPhone() : "N/A")));

        // Ticket details
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

        // Payment + requests
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

        // Buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = ComponentFactory.smallButton("✏ Edit", "#3E6F64");
        editBtn.setOnAction(e -> onEdit.accept(reg));
        Button delBtn = ComponentFactory.smallButton("🗑 Delete", "#D62828");
        delBtn.setOnAction(e -> onDelete.accept(reg));
        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(topRow, statusLbl, contactRow, ticketRow, payRow, actions);
        return card;
    }
}
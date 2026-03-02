package com.mentalhealth.app.views;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ComponentFactory {

    public static Button styledButton(String text, String color) {
        Button btn = new Button(text);
        String base = "-fx-background-color:" + color + "; -fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-padding: 10 20; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                        "-fx-padding: 10 20; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    public static Button smallButton(String text, String color) {
        Button btn = new Button(text);
        String base = "-fx-background-color:" + color + "; -fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-padding: 6 12; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 6 12; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    public static TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(320);
        tf.setStyle("-fx-background-color: #F1F6F4; -fx-text-fill: #1E1E1E;" +
                "-fx-prompt-text-fill: #9CA3AF; -fx-background-radius: 10;" +
                "-fx-padding: 10 15; -fx-font-size: 13px;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");
        return tf;
    }

    public static TextArea styledTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefWidth(320);
        ta.setPrefRowCount(4);
        ta.setWrapText(true);
        ta.setStyle("-fx-control-inner-background: #F1F6F4; -fx-text-fill: #1E1E1E;" +
                "-fx-prompt-text-fill: #9CA3AF; -fx-background-radius: 10;" +
                "-fx-font-size: 13px;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");
        return ta;
    }

    public static Label pageTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 28px; -fx-font-weight: bold;");
        return lbl;
    }

    public static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 18px; -fx-font-weight: bold;");
        return lbl;
    }

    public static Label subText(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        return lbl;
    }

    public static Label infoText(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        return lbl;
    }

    public static Label errorLabel() {
        Label lbl = new Label();
        lbl.setStyle("-fx-text-fill: #D62828; -fx-font-size: 13px;");
        return lbl;
    }

    public static VBox fieldGroup(String label, Node field) {
        VBox group = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 13px; -fx-font-weight: bold;");
        group.getChildren().addAll(lbl, field);
        return group;
    }

    public static HBox detailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-min-width: 140;");
        Label val = new Label(value != null ? value : "N/A");
        val.setStyle("-fx-text-fill: #1E1E1E; -fx-font-size: 13px;");
        val.setWrapText(true);
        row.getChildren().addAll(lbl, val);
        return row;
    }

    public static VBox statItem(String label, String value) {
        VBox item = new VBox(4);
        item.setAlignment(Pos.CENTER);
        item.setPadding(new Insets(0, 15, 0, 15));
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: #2F5D52; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        item.getChildren().addAll(valLbl, nameLbl);
        return item;
    }

    public static Separator verticalSeparator() {
        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #DDE5E2;");
        return sep;
    }

    public static VBox darkCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");
        return card;
    }

    public static String cardStyle() {
        return "-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.08), 10, 0, 0, 4);";
    }

    public static String cardHoverStyle() {
        return "-fx-background-color: #F1F6F4; -fx-background-radius: 15;" +
                "-fx-border-color: #9BC7B5; -fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(155,199,181,0.3), 15, 0, 0, 4);";
    }
}
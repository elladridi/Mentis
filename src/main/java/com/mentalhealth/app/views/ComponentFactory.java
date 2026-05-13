package com.mentalhealth.app.views;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ComponentFactory {

    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#2E7D32";
    private static final String INK = "#1A3C34";
    private static final String TEXT = "#2D3748";
    private static final String MUTED = "#6C757D";
    private static final String BORDER = "#DDE5E2";
    private static final String RED = "#D62828";

    // =================== BUTTONS ===================

    public static Button styledButton(String text, String color) {

        Button btn = new Button(text);

        String base =
                "-fx-background-color: linear-gradient(to right, " + color + ", derive(" + color + ", -12%));" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 14, 0, 0, 5);";

        String hover =
                "-fx-background-color: linear-gradient(to right, derive(" + color + ", 12%), " + color + ");" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.03;" +
                        "-fx-scale-y: 1.03;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 7);";

        btn.setStyle(base);

        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));

        return btn;
    }

    public static Button smallButton(String text, String color) {

        Button btn = new Button(text);

        String base =
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);";

        String hover =
                "-fx-background-color: derive(" + color + ", 15%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.03;" +
                        "-fx-scale-y: 1.03;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 5);";

        btn.setStyle(base);

        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));

        return btn;
    }

    // =================== INPUTS ===================

    public static TextField styledTextField(String prompt) {

        TextField tf = new TextField();

        tf.setPromptText(prompt);
        tf.setPrefWidth(340);

        applyTextFieldStyle(tf, false);

        tf.focusedProperty().addListener((obs, oldVal, focused) ->
                applyTextFieldStyle(tf, focused));

        return tf;
    }

    private static void applyTextFieldStyle(TextField tf, boolean focused) {

        tf.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #1E1E1E;" +
                        "-fx-prompt-text-fill: #9CA3AF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 12 18;" +
                        "-fx-font-size: 13px;" +
                        "-fx-border-color: " + (focused ? "#9BC7B5" : BORDER) + ";" +
                        "-fx-border-width: " + (focused ? "2" : "1") + ";" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, " +
                        (focused
                                ? "rgba(155,199,181,0.30)"
                                : "rgba(0,0,0,0.05)") +
                        ", " +
                        (focused ? "14" : "8") +
                        ", 0, 0, " +
                        (focused ? "3" : "2") +
                        ");"
        );
    }

    public static TextArea styledTextArea(String prompt) {

        TextArea ta = new TextArea();

        ta.setPromptText(prompt);
        ta.setPrefWidth(320);
        ta.setPrefRowCount(4);
        ta.setWrapText(true);

        ta.setStyle(
                "-fx-control-inner-background: white;" +
                        "-fx-text-fill: #1E1E1E;" +
                        "-fx-prompt-text-fill: #9CA3AF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-background-color: white;" +
                        "-fx-padding: 10 14;" +
                        "-fx-font-size: 13px;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        return ta;
    }

    // =================== LABELS ===================

    public static Label pageTitle(String text) {

        Label lbl = new Label(text);

        lbl.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 30px;" +
                        "-fx-font-weight: 900;"
        );

        return lbl;
    }

    public static Label sectionTitle(String text) {

        Label lbl = new Label(text);

        lbl.setStyle(
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: 900;"
        );

        return lbl;
    }

    public static Label subText(String text) {

        Label lbl = new Label(text);

        lbl.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        return lbl;
    }

    public static Label infoText(String text) {

        Label lbl = new Label(text);

        lbl.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 13px;"
        );

        return lbl;
    }

    public static Label errorLabel() {

        Label lbl = new Label();

        lbl.setStyle(
                "-fx-text-fill: " + RED + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        return lbl;
    }

    // =================== FORM GROUPS ===================

    public static VBox fieldGroup(String label, Node field) {

        VBox group = new VBox(6);

        Label lbl = new Label(label);

        lbl.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 800;"
        );

        group.getChildren().addAll(lbl, field);

        return group;
    }

    // =================== DETAILS ===================

    public static HBox detailRow(String label, String value) {

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label lbl = new Label(label + ":");

        lbl.setStyle(
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-min-width: 140;"
        );

        Label val = new Label(value != null ? value : "N/A");

        val.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 13px;"
        );

        val.setWrapText(true);

        row.getChildren().addAll(lbl, val);

        return row;
    }

    // =================== STATS ===================

    public static VBox statItem(String label, String value) {

        VBox item = new VBox(4);

        item.setAlignment(Pos.CENTER);
        item.setPadding(new Insets(0, 15, 0, 15));

        Label valLbl = new Label(value);

        valLbl.setStyle(
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: 900;"
        );

        Label nameLbl = new Label(label);

        nameLbl.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        item.getChildren().addAll(valLbl, nameLbl);

        return item;
    }

    // =================== SEPARATORS ===================

    public static Separator verticalSeparator() {

        Separator sep = new Separator();

        sep.setOrientation(Orientation.VERTICAL);

        sep.setStyle(
                "-fx-background-color: " + BORDER + ";"
        );

        return sep;
    }

    // =================== CARDS ===================

    public static VBox darkCard() {

        VBox card = new VBox(12);

        card.setPadding(new Insets(22));

        card.setStyle(cardStyle());

        return card;
    }

    public static String cardStyle() {

        return
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: rgba(221,229,226,0.92);" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.10), 18, 0, 0, 6);";
    }

    public static String cardHoverStyle() {

        return
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #EEF7F3);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #9BC7B5;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(155,199,181,0.35), 24, 0, 0, 8);";
    }

    // =================== PILLS ===================

    public static Label pill(String text, String bg, String color) {

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
}
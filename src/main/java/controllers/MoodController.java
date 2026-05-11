package controllers;

import com.mentalhealth.app.services.HuggingFaceService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Mood;
import services.MoodService;
import services.QuoteService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MoodController {
    @FXML private StackPane mainContainer;
    @FXML private TableView<Mood> tableMoods;
    @FXML private TableColumn<Mood, String> colFeeling;
    @FXML private TableColumn<Mood, String> colNote;
    @FXML private TableColumn<Mood, LocalDateTime> colDate;
    @FXML private TextField inputFeeling;
    @FXML private TextField inputNote;
    @FXML private ComboBox<String> comboEmoji;
    @FXML private ImageView moodImageView;
    @FXML private Label artDescriptionLabel;
    @FXML private Button btnGenerateArt;

    private final MoodService ms = new MoodService();
    private final HuggingFaceService hfService = new HuggingFaceService();
    private Mood selectedMood = null;

    @FXML
    public void initialize() {
        if (colFeeling != null) {
            colFeeling.setCellValueFactory(new PropertyValueFactory<>("feeling"));
            colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            refreshTable();
        }
        Platform.runLater(() -> {
            try {
                if (mainContainer != null) {
                    injectFloatingButtons();
                    Node content = mainContainer.getChildren().get(0);
                    if (content instanceof VBox) injectQuoteCardAtTop((VBox) content);
                    else if (content instanceof HBox && !((HBox)content).getChildren().isEmpty() && ((HBox)content).getChildren().get(0) instanceof VBox)
                        injectQuoteCardAtTop((VBox)((HBox)content).getChildren().get(0));
                }
            } catch (Exception e) { System.out.println("Init: " + e.getMessage()); }
        });
        if (comboEmoji != null) {
            comboEmoji.setItems(FXCollections.observableArrayList("😊 Heureux", "😐 Neutre", "😔 Triste", "😠 Énervé", "😴 Fatigué", "🚀 Motivé"));
            comboEmoji.setOnAction(event -> { if (comboEmoji.getValue() != null) inputFeeling.setText(comboEmoji.getValue().split(" ")[1]); });
        }
    }

    private void injectFloatingButtons() {
        Button btnChart = new Button("📈");
        btnChart.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 24; -fx-pref-width: 60; -fx-pref-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        Button btnAI = new Button("🤖");
        btnAI.setStyle("-fx-background-color: #3f51b5; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 24; -fx-pref-width: 60; -fx-pref-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        Button btnArt = new Button("🎨");
        btnArt.setStyle("-fx-background-color: #e91e63; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 24; -fx-pref-width: 60; -fx-pref-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        AnchorPane overlay = new AnchorPane();
        overlay.setPickOnBounds(false);
        AnchorPane.setBottomAnchor(btnChart, 30.0); AnchorPane.setRightAnchor(btnChart, 30.0);
        AnchorPane.setBottomAnchor(btnAI, 100.0); AnchorPane.setRightAnchor(btnAI, 30.0);
        AnchorPane.setBottomAnchor(btnArt, 170.0); AnchorPane.setRightAnchor(btnArt, 30.0);
        overlay.getChildren().addAll(btnChart, btnAI, btnArt);
        mainContainer.getChildren().add(overlay);
        btnChart.setOnAction(e -> openMoodChartPopup());
        btnAI.setOnAction(e -> openAIScanner());
        btnArt.setOnAction(e -> generateMoodImage(btnArt));
    }

    @FXML public void handleGenerateArt() {
        generateMoodImage(btnGenerateArt);
    }

    private void generateMoodImage(Button triggerBtn) {
        String emotion = (inputFeeling.getText() != null && !inputFeeling.getText().isEmpty()) ? inputFeeling.getText() : "neutral";
        String note = inputNote.getText();
        triggerBtn.setDisable(true);
        triggerBtn.setText("⏳");
        new Thread(() -> {
            try {
                String prompt = hfService.generateImagePrompt(emotion, note);
                byte[] imageBytes = hfService.generateImage(prompt);
                final String exp = getArtisticExplanation(emotion);
                Platform.runLater(() -> {
                    if (moodImageView != null) {
                        System.out.println("Image reçue, taille : " + imageBytes.length + " octets");
                        Image img = new Image(new ByteArrayInputStream(imageBytes));
                        if (img.isError()) {
                            System.err.println("Erreur de chargement JavaFX Image : " + img.getException().getMessage());
                        } else {
                            moodImageView.setImage(img);
                        }
                    }
                    if (artDescriptionLabel != null) {
                        artDescriptionLabel.setText(exp);
                        artDescriptionLabel.setWrapText(true);
                    }
                    triggerBtn.setDisable(false);
                    triggerBtn.setText("🎨");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    triggerBtn.setDisable(false);
                    triggerBtn.setText("🎨");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur Art Lab");
                    alert.setHeaderText("La génération d'image a échoué");
                    alert.setContentText("Détails: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private String getArtisticExplanation(String emotion) {
        switch (emotion.toLowerCase()) {
            case "heureux": case "happy": return "Vibrant palette of golden yellows - pure joy.";
            case "triste": case "sad": return "Melancholic blues and pearl grey tones.";
            case "énervé": case "angry": return "Scarlet reds with charcoal blacks.";
            default: return "Abstract interpretation of your state of mind.";
        }
    }

    @FXML public void handleSave() throws SQLException {
        if (inputFeeling.getText().isEmpty()) return;
        ms.addMood(new Mood(inputFeeling.getText(), inputNote.getText(), LocalDateTime.now()));
        refreshTable(); clearInputs();
    }

    @FXML public void handleUpdate() throws SQLException {
        if (selectedMood == null) return;
        ms.updateMoodById(selectedMood.getId(), new Mood(inputFeeling.getText(), inputNote.getText(), LocalDateTime.now()));
        refreshTable(); clearInputs(); selectedMood = null;
    }

    @FXML public void handleDelete() throws SQLException {
        if (selectedMood == null) return;
        ms.deleteMoodById(selectedMood.getId());
        refreshTable(); clearInputs(); selectedMood = null;
    }

    private void clearInputs() { inputFeeling.clear(); inputNote.clear(); if (comboEmoji != null) comboEmoji.setValue(null); }

    @FXML public void loadSelectedMood() {
        selectedMood = tableMoods.getSelectionModel().getSelectedItem();
        if (selectedMood != null) { inputFeeling.setText(selectedMood.getFeeling()); inputNote.setText(selectedMood.getNote()); }
    }

    public void refreshTable() {
        try { if (tableMoods != null) tableMoods.setItems(FXCollections.observableArrayList(ms.getAllMoods())); } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void openAIScanner() {
        try {
            java.net.URL url = getClass().getResource("/resources z/ai_scanner.html");
            if (url != null) java.awt.Desktop.getDesktop().browse(url.toURI());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openMoodChartPopup() {
        LineChart<String, Number> lineChart = new LineChart<>(new javafx.scene.chart.CategoryAxis(), new javafx.scene.chart.NumberAxis());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try { for (Mood m : ms.getAllMoods()) series.getData().add(new XYChart.Data<>(m.getDate().toLocalDate().toString(), 3)); } catch (SQLException e) {}
        lineChart.getData().add(series);
        Stage popup = new Stage();
        popup.setTitle("Mood Statistics");
        popup.setScene(new Scene(lineChart, 800, 600));
        popup.show();
    }

    private void injectQuoteCardAtTop(VBox targetVBox) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(45, 62, 80, 0.9); -fx-background-radius: 15; -fx-padding: 10;");
        Label qLabel = new Label("Loading quote...");
        qLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(qLabel);
        targetVBox.getChildren().add(0, card);
        new Thread(() -> Platform.runLater(() -> qLabel.setText(QuoteService.getDailyQuote()))).start();
    }

    @FXML public void openReflect(ActionEvent event) throws IOException {
        Stage s = new Stage();
        s.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/ReflectView.fxml"))));
        s.show();
    }

    @FXML public void backToHome(ActionEvent event) throws IOException {
        ((Node) event.getSource()).getScene().setRoot(FXMLLoader.load(getClass().getResource("/fxml/HomeView.fxml")));
    }
}
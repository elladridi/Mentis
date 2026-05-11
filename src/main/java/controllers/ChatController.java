package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.concurrent.Task;
import services.GeminiService;

public class ChatController {

    @FXML private VBox chatBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;

    @FXML
    public void initialize() {
        addMessage("Mentis AI", "Bonjour ! Comment puis-je vous aider aujourd'hui ? ", false);
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText();
        if (message == null || message.trim().isEmpty()) return;

        addMessage("Vous", message, true);
        messageInput.clear();

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return GeminiService.getGoalAdvice(message);
            }
        };

        aiTask.setOnSucceeded(e -> {
            addMessage("Mentis AI", GeminiService.parseResponse(aiTask.getValue()), false);
        });

        aiTask.setOnFailed(e -> {
            addMessage("Mentis AI", "Erreur : Impossible de contacter l'IA.", false);
        });

        new Thread(aiTask).start();
    }

    private void addMessage(String sender, String message, boolean isUser) {
        HBox container = new HBox();
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setStyle("-fx-background-radius: 15; -fx-padding: 10;");
        if (isUser) {
            container.setAlignment(Pos.CENTER_RIGHT);
            label.setStyle(label.getStyle() + "-fx-background-color: #0084FF; -fx-text-fill: white;");
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
            label.setStyle(label.getStyle() + "-fx-background-color: #E4E6EB; -fx-text-fill: black;");
        }
        container.getChildren().add(label);
        chatBox.getChildren().add(container);
    }
}

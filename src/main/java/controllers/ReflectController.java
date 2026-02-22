package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class ReflectController {

    @FXML private ComboBox<String> qSleep, qStress, qSocial;
    @FXML private Label resultLabel, summaryTitle;

    @FXML
    private void handleAnalysis() {
        if (qSleep.getValue() == null || qStress.getValue() == null || qSocial.getValue() == null) {
            resultLabel.setText("Please answer all questions first!");
            return;
        }

        int score = 0;

        // Question 1 Analysis
        if (qSleep.getValue().contains("Excellent")) score += 3;
        else if (qSleep.getValue().contains("Good")) score += 2;
        else if (qSleep.getValue().contains("Average")) score += 1;

        // Question 2 Analysis
        if (qStress.getValue().contains("Low")) score += 3;
        else if (qStress.getValue().contains("Moderate")) score += 1;

        // Question 3 Analysis
        if (qSocial.getValue().contains("High")) score += 2;
        else if (qSocial.getValue().contains("Neutral")) score += 1;

        displayResult(score);
    }

    private void displayResult(int score) {
        summaryTitle.setVisible(true);
        String message;

        if (score >= 7) {
            message = "SUMMARY: You are in a high-vibration state! \nADVICE: This is a great time to tackle your most challenging goals. Keep this momentum!";
        } else if (score >= 4) {
            message = "SUMMARY: You are balanced but cautious. \nADVICE: Take some time to breathe. A short walk or a 5-minute meditation would be perfect for you right now.";
        } else {
            message = "SUMMARY: You seem to be struggling today. \nADVICE: It's okay not to be okay. Prioritize rest, stay hydrated, and try to avoid heavy tasks today.";
        }

        resultLabel.setText(message);
    }
}
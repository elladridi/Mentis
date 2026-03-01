package services;

import javafx.scene.control.Alert;
import java.awt.Desktop;
import java.net.URI;

public class VideoCallService {

    public String generateMeetingLink(int sessionId, int patientId, int psychologistId) {
        // Create a unique room name using session and user IDs
        String roomName = String.format("mentis-%d-%d-%d",
                sessionId,
                patientId,
                psychologistId
        );

        // Jitsi Meet link with good default settings
        String baseUrl = "https://meet.jit.si/";
        String options = "#config.startWithAudioMuted=false&config.startWithVideoMuted=false&userInfo.displayName='Patient'";

        return baseUrl + roomName + options;
    }

    public void joinMeeting(String meetingLink) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(meetingLink));
            } else {
                showAlert("Cannot open browser automatically. Please use this link:\n" + meetingLink);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening meeting: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Video Call");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

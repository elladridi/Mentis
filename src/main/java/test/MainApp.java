package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ON CHANGE ICI : On charge LoginView au lieu de HomeView
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeView.fxml"));

        primaryStage.setTitle("MENTIS - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
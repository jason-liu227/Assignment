package com.example.ca1bloodcellanalyser;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ApplicationStart extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Load the FXML file that contains the TabPane.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Set up the main stage with the loaded scene.
            stage.setScene(scene);
            stage.setTitle("Analyser");
            stage.show();
        } catch (Exception e) {
            showErrorDialog("Failed to load application", "There was an error loading the main view.");
        }
    }

    // Display an alert if there is an error loading the FXML file
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);  // Launches the JavaFX application.
    }
}
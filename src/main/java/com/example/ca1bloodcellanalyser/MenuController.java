package com.example.ca1bloodcellanalyser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;

import java.io.IOException;

public class MenuController {
    @FXML private Tab tab1;

    @FXML
    public void initialize() {
        loadTabContent(tab1, "BloodCellConverter-view.fxml");
    }

    private void loadTabContent(Tab tab, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node content = loader.load();
            tab.setContent(content);
        } catch (Exception e) {
            showErrorDialog("Failed to load tab", "There was an error loading " + fxmlFile + ".");
        }
    }

    // Error dialog to notify the user of any loading issues
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
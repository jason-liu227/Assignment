package com.example.ca1bloodcellanalyser;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class BloodCellController {

    @FXML
    private ImageView iv;
    @FXML
    private Button selectBTN;
    @FXML
    private Button convertBTN;
    @FXML
    private ProgressBar pbar;
    @FXML
    private Canvas canvas;
    @FXML
    private CheckBox filterNoiseCheckbox;
    private Image image;
    private PixelReader preader;
    private PixelWriter pwriter;
    private File filePath = null;
    private WritableImage wimage;

    @FXML
    private Label resultLabel;

    @FXML
    private void selectImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        filePath = fc.showOpenDialog(new Stage());
        if (filePath != null) {
            try {
                image = new Image(filePath.toURI().toURL().toExternalForm());
                iv.setImage(image);
            } catch (MalformedURLException ex) {
                Logger.getLogger(BloodCellController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void convertToCellFilter(ActionEvent event) {
        pbar.progressProperty().unbind();
        pbar.progressProperty().bind(RGBToCellFilter.progressProperty());
        pbar.setVisible(true);
        RGBToCellFilter.restart();
    }

    private final Service<Void> RGBToCellFilter = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    image = new Image(filePath.toURI().toURL().toExternalForm());
                    preader = image.getPixelReader();
                    wimage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
                    pwriter = wimage.getPixelWriter();
                    int count = 0;
                    for (int i = 0; i < (int) image.getHeight(); i++) {
                        for (int j = 0; j < (int) image.getWidth(); j++) {
                            count++;
                            Color col = preader.getColor(j, i);
                            if (col.getRed() > .6 && col.getGreen() < .75 && col.getBlue() < .75) {
                                pwriter.setColor(j, i, Color.RED); // Red blood cells
                            } else if (col.getRed() > .25 && col.getBlue() < .75) {
                                pwriter.setColor(j, i, Color.PURPLE); // White blood cell nucleus
                            } else {
                                pwriter.setColor(j, i, Color.WHITE);// Background remains white
                            }
                            updateProgress(count, image.getHeight() * image.getWidth());
                        }
                    }
                    return null;
                }
            };
        }
    };


    @FXML
    private void processImage() {
        if (wimage == null) {
            resultLabel.setText("Please select and convert an image first.");
            return;
        }

        int width = (int) wimage.getWidth();
        int height = (int) wimage.getHeight();

        boolean filterNoise = filterNoiseCheckbox.isSelected();

        // Always first make a UnionFind for RED
        CellUnionFind redUnionFind = new CellUnionFind(width, height, Color.RED, 1);
        Map<Integer, List<int[]>> redComponents = redUnionFind.segmentImage(wimage);

        if (filterNoise) {
            // Remove small red noise directly from the image
            PixelWriter pwriter = wimage.getPixelWriter();
            for (Map.Entry<Integer, List<int[]>> entry : redComponents.entrySet()) {
                if (entry.getValue().size() < 100) {
                    for (int[] pixel : entry.getValue()) {
                        pwriter.setColor(pixel[0], pixel[1], Color.WHITE); // Paint it white
                    }
                }
            }
        }

        // After removing noise, reprocess clean image
        redUnionFind = new CellUnionFind(width, height, Color.RED, 100); // Rebuild only big red
        CellUnionFind purpleUnionFind = new CellUnionFind(width, height, Color.PURPLE, 100);

        redComponents = redUnionFind.segmentImage(wimage);
        Map<Integer, List<int[]>> purpleComponents = purpleUnionFind.segmentImage(wimage);

        resultLabel.setText("Found " + redComponents.size() + " red cells and " + purpleComponents.size() + " purple cells.");

        // Draw results
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawBoundingBoxes(redComponents, redUnionFind, Color.GREEN, "R");
        drawBoundingBoxes(purpleComponents, purpleUnionFind, Color.RED, "P");

        // Update ImageView with cleaned image
        iv.setImage(wimage);
    }

    private void drawBoundingBoxes(Map<Integer, List<int[]>> components, CellUnionFind unionFind, Color color, String labelPrefix) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);
        gc.setStroke(color);
        gc.setFill(Color.BLACK);
        int counter = 1;

        for (Integer id : components.keySet()) {
            Rectangle rect = unionFind.getBoundingRect(id);

            gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            gc.fillText(labelPrefix + counter, rect.getX() + 5, rect.getY() + 15);

            counter++;
        }
    }

    @FXML
    public void initialize() {
        pbar.setVisible(false);
        RGBToCellFilter.setOnSucceeded((WorkerStateEvent we) -> {
            iv.setImage(wimage);
            pbar.setVisible(false);
        });
    }
}

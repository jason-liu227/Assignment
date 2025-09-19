module com.example.ca1bloodcellanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.junit.jupiter.api;


    opens com.example.ca1bloodcellanalyser to javafx.fxml;
    exports com.example.ca1bloodcellanalyser;
}
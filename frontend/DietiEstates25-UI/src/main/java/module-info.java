module dietiestates25ui {
    requires javafx.fxml;

    requires okhttp3;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires javafx.web;
    requires java.net.http;
    requires org.slf4j;

    opens dietiestates25ui.controller to javafx.fxml;
    opens dietiestates25ui.model to com.fasterxml.jackson.databind;

    exports dietiestates25ui;
    exports dietiestates25ui.dto to com.fasterxml.jackson.databind;
    opens dietiestates25ui.handler to javafx.fxml;
}
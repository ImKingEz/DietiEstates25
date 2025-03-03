module com.dietiestates25ui {
    requires javafx.fxml;

    requires okhttp3;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires javafx.web;
    requires java.net.http;
    requires org.slf4j;
    requires DietiEstates25.Shared.Library;
    requires jdk.jsobject;
    requires annotations;

    requires java.datatransfer;
    requires java.desktop;
    requires org.apache.commons.validator;
    requires org.apache.commons.csv;
    requires kernel;
    requires layout;
    requires io;

    opens com.dietiestates25ui.controller to javafx.fxml;
    opens com.dietiestates25ui.model to com.fasterxml.jackson.databind;

    exports com.dietiestates25ui;
    exports com.dietiestates25ui.dto to com.fasterxml.jackson.databind;
    opens com.dietiestates25ui.handler to javafx.fxml;
}
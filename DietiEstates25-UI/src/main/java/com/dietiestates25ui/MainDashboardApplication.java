package com.dietiestates25ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainDashboardApplication.class.getResource("/com/dietiestates25ui/view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(getClass().getResource("/com/dietiestates25ui/styles/login-style.css").toExternalForm());

        stage.setTitle("DietiEstates25");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}

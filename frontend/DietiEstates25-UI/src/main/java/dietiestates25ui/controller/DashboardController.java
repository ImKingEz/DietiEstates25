package dietiestates25ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane primaryAnchorPane;

    private String token;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    public void setToken(String token) {
        this.token = token;
        displayToken();
    }

    private void displayToken() {
        if(token != null && !token.isBlank()){
            welcomeLabel.setText("Welcome, you have logged in successfully. Your token is: " + token);
        }
    }

}
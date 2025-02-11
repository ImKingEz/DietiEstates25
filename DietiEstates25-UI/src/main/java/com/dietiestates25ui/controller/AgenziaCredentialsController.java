package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AgenziaCredentialsController extends AbstractController implements Initializable {

    AgenziaImmobiliare agenzia;

    @FXML
    private TextField emailTextField;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private Button indietroButton;

    @FXML
    private ImageView logo;

    @FXML
    private HBox passwordHBox;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button confermaButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private TextField passwordTextField;

    private boolean passwordVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        emailTextField.setText(agenzia.getEmail());

        updateConfermaButton();

        confermaButton.setOnAction(event -> registraAgenzia());

        indietroButton.setOnAction(event -> openRegisterAgenziaPage());

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void passwordTextFieldInitializer() {
        passwordTextField = new TextField();
        passwordTextField.setPromptText(passwordPasswordField.getPromptText());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.setVisible(false);
        passwordTextField.prefWidthProperty().bind(passwordPasswordField.widthProperty());
        passwordTextField.textProperty().bindBidirectional(passwordPasswordField.textProperty());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordHBox.getChildren().remove(passwordPasswordField);
            passwordHBox.getChildren().add(1, passwordTextField);

            passwordTextField.setVisible(true);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_open.png"))));
        } else {
            passwordHBox.getChildren().remove(passwordTextField);
            passwordHBox.getChildren().add(1, passwordPasswordField);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_closed.png"))));
            passwordTextField.setVisible(false);
        }
    }

    private void openRegisterAgenziaPage() {
        loadScene("/com/dietiestates25ui/view/register-agenzia-view.fxml",
                stageRegisterAgenzia -> {}, indietroButton, "/com/dietiestates25ui/styles/register-style.css");
    }

    private void registraAgenzia() {
        Platform.runLater(() -> logo.getParent().requestFocus());

        agenzia.setPassword(passwordPasswordField.getText().trim());
        //TODO
    }

    private void updateConfermaButton() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        confermaButton.setDisable(!FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }

    public void setAgenzia(AgenziaImmobiliare agenzia) {
        this.agenzia = agenzia;
    }
}

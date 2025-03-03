package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class AreaAmministrativaController extends AbstractController implements Initializable {

    @FXML
    private Button creaAccountAgenteButton;

    @FXML
    private Button creaAccountAmministratoreButton;

    @FXML
    private Button homepageButton;

    @FXML
    private Button tornaLoginButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        Platform.runLater(this::searchUserNameAndUpdateProfileHBox);

        tornaLoginButton.setOnAction(event -> openLoginAmministratorePage());

        homepageButton.setOnAction(event -> openHomepage(homepageButton));

        creaAccountAgenteButton.setOnAction(event -> openRegisterAgentePage());

        creaAccountAmministratoreButton.setOnAction(event -> openRegisterAmministratorePage());
    }

    private void openRegisterAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/register-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                    RegisterAmministratoreController controller = fxmlLoader.getController();
                    controller.setAmministratore(amministratore);
                    controller.setStage(stage);
                }, creaAccountAmministratoreButton, "/com/dietiestates25ui/styles/register-amministratore-style.css");
    }

    private void openRegisterAgentePage() {
        loadScene("/com/dietiestates25ui/view/register-agente-view.fxml",
                (fxmlLoader, stage) -> {
                    RegisterAgenteController controller = fxmlLoader.getController();
                    controller.setAmministratore(amministratore);
                    controller.setStage(stage);
                }, creaAccountAgenteButton, "/com/dietiestates25ui/styles/register-agente-style.css");
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                }, tornaLoginButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }
}

package com.dietiestates25ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectRoleController extends AbstractController implements Initializable {

    @FXML
    private Button amministratoreButton;

    @FXML
    private Button agenteImmobiliareButton;

    @FXML
    private Button tornaIndietroButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        amministratoreButton.setOnAction(event -> openLoginAmministratorePage());

        agenteImmobiliareButton.setOnAction(event -> openLoginAgenteImmobiliarePage());

        tornaIndietroButton.setOnAction(event -> openLoginPage());
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml",
                (fxmlLoader, stage) -> {}, tornaIndietroButton, "/com/dietiestates25ui/styles/login-style.css");
    }

    private void openLoginAgenteImmobiliarePage() {
        loadScene("/com/dietiestates25ui/view/login-agente-immobiliare-view.fxml",
                (fxmlLoader, stage) -> {}, agenteImmobiliareButton, "/com/dietiestates25ui/styles/login-agente-immobiliare-style.css");
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {}, amministratoreButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }
}

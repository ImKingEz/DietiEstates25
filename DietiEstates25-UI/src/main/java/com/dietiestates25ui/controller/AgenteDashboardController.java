package com.dietiestates25ui.controller;

import com.dietiestates25ui.model.AgenteImmobiliare;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class AgenteDashboardController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AgenteDashboardController.class);

    @FXML
    private Button caricaImmobileButton;

    @FXML
    private Button statisticheButton;

    @FXML
    private Button homepageButton;

    @FXML
    private Button tornaLoginButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        Platform.runLater(this::searchUserNameAndUpdateProfileHBox);

        caricaImmobileButton.setOnAction(event -> openInserimentoDatiInserzionePage());

        tornaLoginButton.setOnAction(event -> openLoginPage());

        statisticheButton.setOnAction(event -> openStatisticheInserzioniPage());

        homepageButton.setOnAction(event -> openHomepage(homepageButton));

        tornaLoginButton.setOnAction(event -> openLoginPage());
    }

    private void openStatisticheInserzioniPage() {
        loadScene("/com/dietiestates25ui/view/statistiche-inserzioni-view.fxml",
                (fxmlLoader, stage) -> {
                    StatisticheInserzioniController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setAgente(agente);
                }, statisticheButton, "/com/dietiestates25ui/styles/statistiche-inserzioni-style.css");
    }

    private void openInserimentoDatiInserzionePage() {
        loadScene("/com/dietiestates25ui/view/inserimento-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    InserimentoInserzioneController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setAgente(agente);
                }, caricaImmobileButton, "/com/dietiestates25ui/styles/inserimento-inserzione-style.css");
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml",
                (fxmlLoader, stage) -> {}, tornaLoginButton, "/com/dietiestates25ui/styles/login-style.css");
    }
}
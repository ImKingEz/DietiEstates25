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
    private Button offerteButton;

    @FXML
    private Button inserzioniButton;

    @FXML
    private Button statisticheButton;

    @FXML
    private Button homepageButton;

    @FXML
    private Button tornaLoginButton;

    private String token;

    private AgenteImmobiliare agente;

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        caricaImmobileButton.setOnAction(event -> openInserimentoDatiInserzionePage());

        tornaLoginButton.setOnAction(event -> openLoginPage());

//        offerteButton.setOnAction(event -> {
//            // Azione per "Offerte"
//            System.out.println("Offerte cliccato!");
//        });
//
//        inserzioniButton.setOnAction(event -> {
//            // Azione per "Inserzioni caricate"
//            System.out.println("Inserzioni caricate cliccato!");
//        });
//
//        statisticheButton.setOnAction(event -> {
//            // Azione per "Statistiche inserzioni"
//            System.out.println("Statistiche inserzioni cliccato!");
//        });
//
//        homepageButton.setOnAction(event -> {
//            // Azione per "Vai alla Homepage"
//            System.out.println("Vai alla Homepage cliccato!");
//        });
//
//        tornaLoginButton.setOnAction(event -> {
//            // Azione per "Torna all'area login"
//            System.out.println("Torna all'area login cliccato!");
//        });
    }

    public void setAgente(AgenteImmobiliare agente) {
        this.agente = agente;
    }

    private void openInserimentoDatiInserzionePage() {
        loadScene("/com/dietiestates25ui/view/inserimento-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    InserimentoInserzioneController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setToken(token);
                    controller.setAgente(agente);
                }, caricaImmobileButton, "/com/dietiestates25ui/styles/inserimento-inserzione-style.css");
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml",
                (fxmlLoader, stage) -> {}, tornaLoginButton, "/com/dietiestates25ui/styles/login-style.css");
    }
}
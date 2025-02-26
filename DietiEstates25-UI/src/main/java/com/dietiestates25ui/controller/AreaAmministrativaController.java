package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AgenziaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

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
    private HBox profileHBox;

    @FXML
    private Button tornaLoginButton;

    private Amministratore amministratore;

    private AgenziaService agenziaService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        agenziaService = new AgenziaService();
        Platform.runLater(this::updateProfileHBox);

        tornaLoginButton.setOnAction(event -> openLoginAmministratorePage());

        homepageButton.setOnAction(event -> openHomepage(token, homepageButton));

        creaAccountAgenteButton.setOnAction(event -> openRegisterAgentePage());

        creaAccountAmministratoreButton.setOnAction(event -> openRegisterAmministratorePage());
    }

    private void openRegisterAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/register-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                    RegisterAmministratoreController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setToken(token);
                    controller.setAmministratore(amministratore);
                }, creaAccountAmministratoreButton, "/com/dietiestates25ui/styles/register-amministratore-style.css");
    }

    private void openRegisterAgentePage() {
        loadScene("/com/dietiestates25ui/view/register-agente-view.fxml",
                (fxmlLoader, stage) -> {
                    RegisterAgenteController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setToken(token);
                    controller.setAmministratore(amministratore);
                }, creaAccountAgenteButton, "/com/dietiestates25ui/styles/register-agente-style.css");
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                }, tornaLoginButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    public void setToken(String token) {
        this.token = token;
    }

    private void updateProfileHBox() {
        try {
            AgenziaDTO agenziaDTO = agenziaService.getAgenziaDetails(amministratore.getIdAgenzia(), token);
            Text name = new Text(agenziaDTO.getNome());
            name.getStyleClass().add("profileName");
            profileHBox.getChildren().addFirst(name);
        } catch (GenericServiceException e) {
            logAndShowDetailsError(e);
        }
    }

    private void logAndShowDetailsError(Exception e) {
        logger.error("Errore durante il recupero dei dettagli dell'agenzia: {}", e.getMessage(), e);
        showPopup(POPUP_ERROR_TITLE, "Errore durante il recupero dei dettagli dell'agenzia.", ERROR_ICON);
    }

    public void setAmministratore(Amministratore admin) {
        this.amministratore = admin;
    }
}

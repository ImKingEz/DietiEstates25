package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AgenziaService;
import com.dietiestates25ui.service.AmministratoreService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
    private ImageView logo;

    @FXML
    private HBox profileHBox;

    @FXML
    private Button tornaLoginButton;

    Amministratore amministratore;

    AgenziaService agenziaService;

    String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        agenziaService = new AgenziaService();
        Platform.runLater(this::updateProfileHBox);

        tornaLoginButton.setOnAction(event -> openLoginAmministratorePage());

        homepageButton.setOnAction(event -> openHomePage());

        creaAccountAgenteButton.setOnAction(event -> openRegisterAgentePage());
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

    private void openHomePage() {
        loadScene("/com/dietiestates25ui/view/homepage-view.fxml",
                (fxmlLoader, stage) -> {
                }, homepageButton, "/com/dietiestates25ui/styles/homepage-style.css");
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                }, tornaLoginButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
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

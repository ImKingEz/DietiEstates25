package com.dietiestates25ui.handler;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.MainApplication;
import com.dietiestates25ui.controller.AbstractController;
import com.dietiestates25ui.controller.AccountCompletionController;
import com.dietiestates25ui.controller.TokenManager;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.UtenteService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OAuth2Handler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Handler.class);

    public static final String OAUTH_ERROR_URL = "oauth2/error?error=";
    public static final String OAUTH_FIRSTLOGIN_URL = "oauth2/firstlogin?token=";
    public static final String OAUTH_SUCCESS_URL = "oauth2/success?token=";
    private final AbstractController controller;
    private final Button button;

    public OAuth2Handler(AbstractController controller, Button button) {
        this.controller = controller;
        this.button = button;
    }

    public void handleOAuthRedirect(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        if (url.contains(OAUTH_SUCCESS_URL)) {
            handleOAuthSuccess(url, button);
        } else if (url.contains(OAUTH_FIRSTLOGIN_URL)) {
            handleOAuthFirstLogin(url);
        } else if (url.contains(OAUTH_ERROR_URL)) {
            handleOAuthError(url);
        }
    }

    private void handleOAuthSuccess(String url, Button button) {
        String token = parseTokenFromURL(url);
        if (isValidToken(token)) {
            TokenManager.getInstance().setToken(token);

            Utente utente = findAndSetUtente(token);

            TokenManager.getInstance().setLoggedInUser(utente);
            controller.openHomepage(button);
        }
    }

    private Utente findAndSetUtente(String token) {
        UtenteService utenteService = new UtenteService();
        UtenteDTO utenteDTO = null;
        try {
            utenteDTO = utenteService.getUtenteDetails(token);
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dei dettagli dell'utente", e);
            controller.showPopup(AbstractController.POPUP_ERROR_TITLE, "Errore durante il recupero dei dettagli dell'utente", AbstractController.ERROR_ICON);
        }
        if (utenteDTO == null) {
            Utente utente = new Utente();
            utente.setNome("");
            utente.setCognome("");
            utente.setEmail("");
            return utente;
        } else {
            return convertUtenteDTO(utenteDTO);
        }
    }

    private static Utente convertUtenteDTO(UtenteDTO utenteDTO) {
        Utente utente = new Utente();
        utente.setNome(utenteDTO.getNome());
        utente.setCognome(utenteDTO.getCognome());
        utente.setEmail(utenteDTO.getEmail());
        return utente;
    }

    private void handleOAuthFirstLogin(String url) {
        String token = parseTokenFromURL(url);
        if (isValidToken(token)) {
            openAccountCompletion(token);
        }
    }

    private void handleOAuthError(String url) {
        String error = parseErrorFromURL(url);
        if (isValidError(error)) {
            logAndShowOAuthError(error);
        }
    }

    private boolean isValidToken(String token) {
        return token != null && !token.isBlank();
    }

    private boolean isValidError(String error) {
        return error != null && !error.isBlank();
    }

    private void logAndShowOAuthError(String error) {
        logger.error("Errore durante il login OAuth2: {}", error);
        controller.showPopup("Errore durante il login", error, AbstractController.ERROR_ICON);
    }

    private void openAccountCompletion(String token) {
        try {
            TokenManager.getInstance().setToken(token);

            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/dietiestates25ui/view/account-completion-view.fxml"));
            Scene sceneAccountCompletion = new Scene(fxmlLoader.load());
            Stage stageAccountCompletion = controller.getCurrentStage();

            stageAccountCompletion.setWidth(stageAccountCompletion.getWidth());
            stageAccountCompletion.setHeight(stageAccountCompletion.getHeight());

            stageAccountCompletion.setX(stageAccountCompletion.getX());
            stageAccountCompletion.setY(stageAccountCompletion.getY());

            sceneAccountCompletion.getStylesheets().add(getClass().getResource("/com/dietiestates25ui/styles/account-completion-style.css").toExternalForm());
            AccountCompletionController accountCompletionController = fxmlLoader.getController();
            accountCompletionController.setStage(stageAccountCompletion);
            accountCompletionController.loadUserDetails();
            stageAccountCompletion.setTitle(AbstractController.APP_TITLE);
            stageAccountCompletion.setScene(sceneAccountCompletion);
            stageAccountCompletion.show();
        } catch (IOException e) {
            logger.error("Errore durante l'apertura della pagina di completamento account: {}", e.getMessage());
            controller.showPopup(AbstractController.POPUP_ERROR_TITLE, "Errore durante il reindirizzamento alla pagina di completamento account", AbstractController.ERROR_ICON);
        }
    }

    private String parseTokenFromURL(String url) {
        try {
            if (url != null && (url.contains(OAUTH_SUCCESS_URL) || url.contains(OAUTH_FIRSTLOGIN_URL))) {
                String token = url.substring(url.indexOf("oauth2/"));
                token = token.substring(token.indexOf("token=") + 6);
                if (token.contains(" ")) {
                    return token.substring(0, token.indexOf(" "));
                }
                return token;
            }
        } catch (Exception e) {
            logger.error("Errore durante il parsing del token dall'URL", e);
            return null;
        }
        return null;
    }

    private String parseErrorFromURL(String url) {
        try {
            if (url != null && url.contains(OAUTH_ERROR_URL)) {
                String error = url.substring(url.indexOf(OAUTH_ERROR_URL));
                error = error.substring(OAUTH_ERROR_URL.length());
                if (error.contains(" ")) {
                    return error.substring(0, error.indexOf(" "));
                }
                return error;
            }
        } catch (Exception e) {
            logger.error("Errore durante il parsing dell'errore dall'URL", e);
            return null;
        }
        return null;
    }
}
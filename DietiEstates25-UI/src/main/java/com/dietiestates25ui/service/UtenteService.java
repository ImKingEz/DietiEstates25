package com.dietiestates25ui.service;

import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Map;

public class UtenteService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);
    private static final String BASE_URL = "http://localhost:8080/api/users";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    public void registraUtente(Utente user) throws GenericServiceException {
        UtenteDTO utenteDTO = executeAndHandle("/register", "POST", user, null, UtenteDTO.class);
        if(utenteDTO != null){
            logger.info("Utente registrato con successo: {}", user.getEmail());
        }
    }

    public String loginUtente(Utente user) throws GenericServiceException {
        LoginResponse loginResponse = executeAndHandle("/login", "POST", Map.of("email", user.getEmail(), "password", user.getPassword()), null, LoginResponse.class);
        if (loginResponse != null) {
            logger.info("Login effettuato con successo per l'utente: {}", user.getEmail());
            return loginResponse.getToken();
        }
        return null;
    }

    public void updateUtente(Utente user, String token) throws GenericServiceException {
        Map<String, String> updateData = null;
        if (user.getCitta() != null) {
            updateData = Map.of("nome", user.getNome(), "cognome", user.getCognome(), "citta", user.getCitta());
        } else {
            updateData = Map.of("nome", user.getNome(), "cognome", user.getCognome());
        }
        UtenteDTO utenteDTO = executeAndHandle("/update", "PUT", updateData, token, UtenteDTO.class);
        if (utenteDTO != null) {
            logger.info("Utente aggiornato con successo: {}", user.getEmail());
        }
    }

    public UtenteDTO getUtenteDetails(String token) throws GenericServiceException {
        return executeAndHandle("/me", "GET", null, token, UtenteDTO.class);
    }

    @Override
    protected void handleErrorResponse(int statusCode, HttpResponse<String> response) throws AuthenticationException, ApiClientException, ServiceUnavailableException, ResourceNotFoundException {
        switch (statusCode) {
            case 401:
                throw new AuthenticationException("Credenziali non valide. Controlla email e password.");
            case 404:
                throw new ResourceNotFoundException("Utente non trovato.");
            case 409:
                logEmailAlreadyInUse(response);
                throw new AuthenticationException("Email già in uso. Inserisci un'altra email.");
            default:
                if (statusCode >= 400 && statusCode < 500) {
                    logClientError(statusCode, response.body());
                    throw new ApiClientException("Errore del client: " + statusCode);
                } else if (statusCode >= 500) {
                    logServerError(statusCode, response.body());
                    throw new ServiceUnavailableException("Errore del server.");
                }
        }
    }
}
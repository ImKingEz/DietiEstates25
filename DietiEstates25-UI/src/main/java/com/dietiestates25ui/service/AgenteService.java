package com.dietiestates25ui.service;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenteImmobiliare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;

public class AgenteService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AgenteService.class);
    private static final String BASE_URL = "http://localhost:8080/api/agenti";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    public void registraAgente(AgenteImmobiliare agente, String token) throws GenericServiceException {
        validateAgente(agente);
        AgenteDTO agenteDTO = executeAndHandle("/register", "POST", agente, token, AgenteDTO.class);
        if(agenteDTO != null){
            logger.info("Agente registrato con successo: {}", agente.getEmail());
        }
    }

    private void validateAgente(AgenteImmobiliare agente) {
        if (!registerFieldAreNull(agente) && !registerFieldAreEmpty(agente)) {
            throw new IllegalArgumentException("I campi obbligatori non possono essere vuoti.");
        }
        if (!agente.getNome().matches("^[a-zA-Z ]+$") || !agente.getCognome().matches("^[a-zA-Z ]+$")) {
            throw new IllegalArgumentException("Il nome e il cognome possono contenere solo lettere e spazi.");
        }
        if (!agente.getDataDiNascita().isEqual(LocalDate.now().minusYears(18)) && agente.getDataDiNascita().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Deve avere almeno 18 anni per registrarlo.");
        }
        if (!agente.getSesso().equalsIgnoreCase("Maschio") || !agente.getSesso().equalsIgnoreCase("Femmina") || !agente.getSesso().equalsIgnoreCase("Non Binario")) {
            throw new IllegalArgumentException("Il sesso può essere solo Maschio, Femmina o Non Binario.");
        }
        if (!FormValidator.isValidEmail(agente.getEmail())) {
            throw new IllegalArgumentException("Email non valida.");
        }
        if (!FormValidator.isValidPassword(agente.getPassword())) {
            throw new IllegalArgumentException("La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero.");
        }
        if (agente.getIdAgenzia() <= 0) {
            throw new IllegalArgumentException("L'agente deve essere associato a un'agenzia.");
        }
    }

    private boolean registerFieldAreEmpty(AgenteImmobiliare agente) {
        return agente.getNome().isEmpty() || agente.getCognome().isEmpty() || agente.getEmail().isEmpty() || agente.getPassword().isEmpty() || agente.getDataDiNascita().toString().isEmpty() || agente.getSesso().isEmpty();
    }

    private static boolean registerFieldAreNull(AgenteImmobiliare agente) {
        return agente.getNome() == null || agente.getCognome() == null || agente.getEmail() == null || agente.getPassword() == null || agente.getDataDiNascita() == null || agente.getSesso() == null;
    }

    public void updateAgente(AgenteImmobiliare agente, String token) throws GenericServiceException {
        AgenteDTO agenteDTO = executeAndHandle("/update", "PUT", agente, token, AgenteDTO.class);
        if(agenteDTO != null){
            logger.info("Agente aggiornato con successo: {}", agente.getEmail());
        }
    }

    public String loginAgente(AgenteImmobiliare agente) throws GenericServiceException {
        LoginResponse loginResponse = executeAndHandle("/login", "POST", Map.of("email", agente.getEmail(), "password", agente.getPassword()), null, LoginResponse.class);
        if (loginResponse != null) {
            logger.info("Login effettuato con successo per l'agente: {}", agente.getEmail());
            return loginResponse.getToken();
        }
        return null;
    }

    public AgenteDTO getAgenteDetails(String token) throws GenericServiceException {
        return executeAndHandle("/me", "GET", null, token, AgenteDTO.class);
    }

    public AgenteDTO getAgenteDetails(long idAgente, String token) throws GenericServiceException {
        return executeAndHandle("/" + idAgente, "GET", null, token, AgenteDTO.class);
    }

    @Override
    protected void handleErrorResponse(int statusCode, HttpResponse<String> response) throws AuthenticationException, ApiClientException, ServiceUnavailableException, ResourceNotFoundException {
        switch (statusCode) {
            case 401:
                throw new AuthenticationException("Credenziali non valide. Controlla email e password.");
            case 404:
                throw new ResourceNotFoundException("Agente non trovato.");
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
                } else {
                    logGenericException(statusCode, response.body());
                    throw new ApiClientException(response.body());
                }
        }
    }
}
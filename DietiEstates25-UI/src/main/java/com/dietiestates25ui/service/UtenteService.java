package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ResourceNotFoundException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.Utente;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtenteService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);
    private static final String BASE_URL = "http://localhost:8080/api/users";

    protected static String getBaseUrl() {
        return BASE_URL;
    }

    public void registraUtente(Utente user) throws GenericServiceException {
        try {
            fetchCsrfToken();
            String jsonBody = objectMapper.writeValueAsString(user); // Usa objectMapper statico
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/register"))
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .header(csrfTokenHeaderName, csrfTokenValue)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();
            logger.info("Registrazione utente effettuata con successo. Status code: {}", statusCode);

            if (statusCode == 201) {
                ApiResponse<UtenteDTO> apiResponse =
                        handleResponse(response, UtenteDTO.class); // Rimosso objectMapper
                if (apiResponse != null && apiResponse.isSuccess()) {
                    return;
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Errore sconosciuto durante la registrazione.";
                    throw new GenericServiceException(errorMessage);
                }
            } else if (statusCode == 409) {
                logEmailAlreadyInUse(response);
                throw new AuthenticationException("Email già in uso. Inserisci un'altra email.");
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException(
                        "Errore durante la registrazione: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante la registrazione. Riprova più tardi.");
            }

            throw new GenericServiceException("Registrazione fallita con status code: " + statusCode);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    public String loginUtente(Utente user) throws GenericServiceException {
        try {
            fetchCsrfToken();
            String jsonBody =
                    objectMapper.writeValueAsString( // Usa objectMapper statico
                            Map.of("email", user.getEmail(), "password", user.getPassword()));

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/login"))
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .header(csrfTokenHeaderName, csrfTokenValue)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<LoginResponse> apiResponse =
                        handleResponse(response, LoginResponse.class); // Rimosso objectMapper
                if (apiResponse != null && apiResponse.isSuccess()) {
                    LoginResponse loginResponse = apiResponse.getData();
                    logger.info("Login effettuato con successo per l'utente: {}", user.getEmail());
                    return loginResponse.getToken();
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Login fallito.";
                    throw new AuthenticationException(errorMessage);
                }

            } else {
                logLoginFailed(user.getEmail(), statusCode);
                switch (statusCode) {
                    case 401:
                        throw new AuthenticationException("Credenziali non valide. Controlla email e password.");
                    case 404:
                        throw new ResourceNotFoundException("Utente non trovato. Controlla email e password.");
                    default:
                        throw new GenericServiceException("Login fallito: (" + statusCode + ")");
                }
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    public void updateUtente(Utente user, String token) throws GenericServiceException {
        try {
            fetchCsrfToken();
            String jsonBody;
            if (user.getCitta() != null) {
                jsonBody =
                        objectMapper.writeValueAsString( // Usa objectMapper statico
                                Map.of("nome", user.getNome(), "cognome", user.getCognome(), "citta", user.getCitta()));
            } else {
                jsonBody =
                        objectMapper.writeValueAsString(Map.of("nome", user.getNome(), "cognome", user.getCognome())); // Usa objectMapper statico
            }

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/update"))
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .header("Authorization", "Bearer " + token)
                            .header(csrfTokenHeaderName, csrfTokenValue)
                            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            logger.info("Aggiornamento utente effettuato. Status code: {}", statusCode);
            if (logger.isTraceEnabled()) {
                logger.trace("Response Body: {}", response.body());
            }

            handleUpdateRequestStatusCode(statusCode, response); //Rimosso objectmapper

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    private void handleUpdateRequestStatusCode(
            int statusCode, HttpResponse<String> response) //Rimosso objectmapper
            throws ApiClientException, GenericServiceException, ServiceUnavailableException {
        if (statusCode == 200) {
            ApiResponse<UtenteDTO> apiResponse = handleResponse(response, UtenteDTO.class); // Rimosso objectMapper
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String errorMessage =
                        (apiResponse != null && apiResponse.getMessage() != null)
                                ? apiResponse.getMessage()
                                : "Errore durante l'aggiornamento dell'utente.";
                throw new GenericServiceException(errorMessage);
            }

        } else if (statusCode >= 400 && statusCode < 500) {
            logUpdateFailedClientError(statusCode, response.body());
            throw new ApiClientException(
                    "Errore durante l'aggiornamento: " + statusCode + ". Controlla i dati inseriti.");
        } else if (statusCode >= 500) {
            logUpdateFailedServerError(statusCode, response.body());
            throw new ServiceUnavailableException("Errore del server durante l'aggiornamento. Riprova più tardi.");
        } else {
            throw new GenericServiceException("Aggiornamento fallito con status code: " + statusCode);
        }
    }

    public UtenteDTO getUtenteDetails(String token) throws GenericServiceException {
        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/me"))
                            .header("Authorization", "Bearer " + token)
                            .GET()
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<UtenteDTO> apiResponse = handleResponse(response, UtenteDTO.class); // Rimosso new ObjectMapper()
                if (apiResponse != null && apiResponse.isSuccess()) {
                    UtenteDTO utenteDTO = apiResponse.getData();
                    logger.info("Dettagli utente recuperati con successo.");
                    return utenteDTO;
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Impossibile recuperare i dettagli dell'utente.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                logGetDetailsFailed(statusCode);
                if (statusCode == 404) {
                    throw new ResourceNotFoundException("Utente non trovato.");
                }
                throw new GenericServiceException("Impossibile recuperare i dettagli dell'utente: " + statusCode);
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }
}
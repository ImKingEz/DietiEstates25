package com.dietiestates25ui.service;

import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ResourceNotFoundException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.Amministratore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmministratoreService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AmministratoreService.class);
    private static final String BASE_URL = "http://localhost:8080/api/admin";

    protected static String getBaseUrl() {
        return BASE_URL;
    }

    public void registraAmministratore(Amministratore admin) throws GenericServiceException {
        try {
            fetchCsrfToken();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(admin);
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
                ApiResponse<AmministratoreDTO> apiResponse =
                        handleResponse(response, objectMapper, AmministratoreDTO.class);
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

    public String loginAmministratore(Amministratore admin) throws GenericServiceException {
        try {
            fetchCsrfToken(); // Ottieni il CSRF token

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(admin);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/login"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(csrfTokenHeaderName, csrfTokenValue) // Includi il CSRF token
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<LoginResponse> apiResponse = handleResponse(response, objectMapper, LoginResponse.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    LoginResponse loginResponse = apiResponse.getData();
                    return loginResponse.getToken();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante il login.";
                    throw new AuthenticationException(errorMessage);
                }
            } else if (statusCode == 401) {
                logger.error("Credenziali non valide. Status code: {}, Response body: {}", statusCode, response.body());
                throw new AuthenticationException("Credenziali non valide. Riprova.");
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante il login: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante il login. Riprova più tardi.");
            }

            throw new GenericServiceException("Login fallito con status code: " + statusCode);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    public AmministratoreDTO getAmministratoreDetails(String token) throws GenericServiceException {
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
                ApiResponse<AmministratoreDTO> apiResponse =
                        handleResponse(response, new ObjectMapper(), AmministratoreDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    AmministratoreDTO amministratoreDTO = apiResponse.getData();
                    logger.info("Dettagli utente recuperati con successo.");
                    return amministratoreDTO;
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
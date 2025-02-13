package com.dietiestates25ui.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgenziaService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);
    private static final String BASE_URL = "http://localhost:8080/api/agenzie";

    protected static String getBaseUrl() {
        return BASE_URL;
    }

    public void registraAgenzia(AgenziaImmobiliare agenzia) throws GenericServiceException {
        try {
            fetchCsrfToken();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(agenzia);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/register"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            logger.info("Tentativo di registrazione agenzia. Status code: {}", statusCode);

            if (statusCode == 201) {
                ApiResponse apiResponse = handleResponse(response, objectMapper, Void.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Registrazione agenzia effettuata con successo.");
                    return;
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto durante la registrazione dell'agenzia.";
                    logger.error("Errore nella risposta durante la registrazione dell'agenzia: {}", errorMessage);
                    throw new GenericServiceException(errorMessage);
                }
            } else if (statusCode == 409) {
                logEmailAlreadyInUse(response);
                throw new AuthenticationException("Email già in uso. Inserisci un'altra email.");
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante la registrazione dell'agenzia: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante la registrazione dell'agenzia. Riprova più tardi.");
            } else {
                logger.error("Registrazione agenzia fallita. Status code: {}, Response body: {}", statusCode, response.body());
                throw new GenericServiceException("Registrazione agenzia fallita con status code: " + statusCode);
            }

        } catch (Exception e) {
            logger.error("Errore generico durante la registrazione dell'agenzia: {}", e.getMessage());
            throw handleGenericException("Errore durante la registrazione dell'agenzia: " + e.getMessage(), e);
        }
    }

    public AgenziaDTO getAgenziaDetails(Long agenziaId, String token) throws GenericServiceException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/" + agenziaId))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            logger.info("Tentativo di recupero dettagli agenzia con ID: {}. Status code: {}", agenziaId, statusCode);

            ObjectMapper objectMapper = new ObjectMapper();

            if (statusCode == 200) {
                ApiResponse<AgenziaDTO> apiResponse = handleResponse(response, objectMapper, AgenziaDTO.class);
                if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                    logger.info("Dettagli agenzia recuperati con successo per ID: {}.", agenziaId);
                    return apiResponse.getData();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto durante il recupero dei dettagli dell'agenzia.";
                    logger.error("Errore nella risposta durante il recupero dei dettagli dell'agenzia: {}", errorMessage);
                    throw new GenericServiceException(errorMessage);
                }
            } else if (statusCode == 404) {
                logger.warn("Agenzia non trovata con ID: {}", agenziaId);
                throw new ApiClientException("Agenzia non trovata.");
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante il recupero dei dettagli dell'agenzia: " + statusCode + ". Controlla l'ID dell'agenzia.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante il recupero dei dettagli dell'agenzia. Riprova più tardi.");
            } else {
                logger.error("Recupero dettagli agenzia fallito per ID: {}. Status code: {}, Response body: {}", agenziaId, statusCode, response.body());
                throw new GenericServiceException("Recupero dettagli agenzia fallito con status code: " + statusCode);
            }

        } catch (Exception e) {
            logger.error("Errore generico durante il recupero dei dettagli dell'agenzia con ID: {}: {}", agenziaId, e.getMessage());
            throw handleGenericException("Errore durante il recupero dei dettagli dell'agenzia: " + e.getMessage(), e);
        }
    }
}
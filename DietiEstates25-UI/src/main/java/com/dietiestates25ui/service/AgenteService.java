package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ResourceNotFoundException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.AgenteImmobiliare;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgenteService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AgenteService.class);
    private static final String BASE_URL = "http://localhost:8080/api/agenti";

    protected static String getBaseUrl() {
        return BASE_URL;
    }

    public void registraAgente(AgenteImmobiliare agente) throws GenericServiceException {
        try {
            fetchCsrfToken();
            String jsonBody = objectMapper.writeValueAsString(agente); // Usa objectMapper statico

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/register"))
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .header(csrfTokenHeaderName, csrfTokenValue)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<AgenteDTO> apiResponse = handleResponse(response, AgenteDTO.class); // Rimosso objectMapper
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Agente registrato con successo: {}", agente.getEmail());
                    return;
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Errore sconosciuto durante la registrazione dell'agente.";
                    throw new GenericServiceException(errorMessage);
                }
            } else if (statusCode == 409) {
                logEmailAlreadyInUse(response);
                throw new AuthenticationException("Email già in uso. Inserisci un'altra email.");
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante la registrazione: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante la registrazione. Riprova più tardi.");
            }

            throw new GenericServiceException("Registrazione fallita con status code: " + statusCode);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    public void updateAgente(AgenteImmobiliare agente, String token) throws GenericServiceException {
        try {
            fetchCsrfToken();
            String jsonBody = objectMapper.writeValueAsString(agente); // Usa objectMapper statico

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

            if (statusCode == 200) {
                ApiResponse<AgenteDTO> apiResponse = handleResponse(response, AgenteDTO.class); // Rimosso objectMapper
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Agente aggiornato con successo: {}", agente.getEmail());
                    return;
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Errore sconosciuto durante l'aggiornamento dell'agente.";
                    throw new GenericServiceException(errorMessage);
                }
            } else if (statusCode >= 400 && statusCode < 500) {
                logClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante l'aggiornamento: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante l'aggiornamento. Riprova più tardi.");
            }

            throw new GenericServiceException("Aggiornamento fallito con status code: " + statusCode);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    public AgenteDTO getAgenteDetails(Long id, String token) throws GenericServiceException {
        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(getBaseUrl() + "/" + id))
                            .header("Authorization", "Bearer " + token)
                            .GET()
                            .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<AgenteDTO> apiResponse = handleResponse(response, AgenteDTO.class); // Rimosso new ObjectMapper()
                if (apiResponse != null && apiResponse.isSuccess()) {
                    AgenteDTO agenteDTO = apiResponse.getData();
                    logger.info("Dettagli agente recuperati con successo: {}", agenteDTO.getEmail());
                    return agenteDTO;
                } else {
                    String errorMessage =
                            (apiResponse != null && apiResponse.getMessage() != null)
                                    ? apiResponse.getMessage()
                                    : "Impossibile recuperare i dettagli dell'agente.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                logGetDetailsFailed(statusCode);
                if (statusCode == 404) {
                    throw new ResourceNotFoundException("Agente non trovato.");
                }
                throw new GenericServiceException("Impossibile recuperare i dettagli dell'agente: " + statusCode);
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

}
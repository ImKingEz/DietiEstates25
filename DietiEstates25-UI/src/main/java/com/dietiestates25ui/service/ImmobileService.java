package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.dto.CsrfResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ImmobileService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);
    private static final String BASE_URL = "http://localhost:8080/api/immobili";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TIMEOUT_ERROR = "Timeout durante la comunicazione con il server. Riprova più tardi.";
    public static final String CONNECTION_ERROR = "Impossibile connettersi al server. Verifica la connessione e riprova.";
    public static final String COMUNICATION_ERROR = "Errore durante la comunicazione con il server. Riprova più tardi.";
    public static final String INTERRUPT_OPERATION_ERROR = "Operazione interrotta. Riprova.";

    private static String csrfTokenValue;
    private static String csrfTokenHeaderName;

    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    public static void fetchCsrfToken() throws ServiceUnavailableException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/csrf"))
                    .GET()
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                CsrfResponse csrfResponse = objectMapper.readValue(response.body(), CsrfResponse.class);
                if (csrfResponse != null && csrfResponse.getToken() != null && csrfResponse.getHeaderName() != null) {
                    csrfTokenValue = csrfResponse.getToken();
                    csrfTokenHeaderName = csrfResponse.getHeaderName();
                    logger.info("CSRF token fetched successfully: {}, Header Name: {}", csrfTokenValue, csrfTokenHeaderName);
                } else {
                    String responseBody = response.body() != null ? response.body() : "Response body is null";
                    logger.error("Failed to parse CSRF token from response: {}", responseBody);
                    throw new GenericServiceException("Failed to fetch CSRF token.");
                }
            } else {
                String responseBody = response.body() != null ? response.body() : "Response body is null";
                logger.error("Failed to fetch CSRF token. Status code: {}, Response: {}", statusCode, responseBody);
                throw new ServiceUnavailableException("Failed to fetch CSRF token: " + statusCode);
            }
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to fetch CSRF token: " + e.getMessage());
        }
    }

    private <T> ApiResponse<T> handleResponse(HttpResponse<String> response, ObjectMapper objectMapper, Class<T> dataType) throws ApiClientException {
        try {
            Type type = new ParameterizedType() {
                @NotNull
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{dataType};
                }

                @NotNull
                @Override
                public Type getRawType() {
                    return ApiResponse.class;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            };
            return objectMapper.readValue(response.body(), objectMapper.constructType(type));
        } catch (IOException e) {
            logger.error("Errore durante la lettura della risposta JSON: ", e);
            throw new ApiClientException("Risposta del server non valida. Riprova più tardi.");
        }
    }

    public void salvaImmobile(ImmobileDTO immobileDTO, String token) throws GenericServiceException {
        try {
            fetchCsrfToken(); // Assicurati di avere il token CSRF più recente
            ObjectMapper objectMapper = new ObjectMapper();
            String immobileJson = objectMapper.writeValueAsString(immobileDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/create"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(immobileJson))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<String> apiResponse = handleResponse(response, objectMapper, String.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Immobile salvato correttamente!");
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante il salvataggio dell'immobile.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                throw new GenericServiceException("Salvataggio immobile fallito con status code: " + statusCode);
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    private static HttpResponse<String> executeRequest(HttpRequest request) throws ServiceUnavailableException {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            logConnectException(e);
            throw new ServiceUnavailableException(CONNECTION_ERROR);
        } catch (SocketTimeoutException e) {
            logTimeoutException(e);
            throw new ServiceUnavailableException(TIMEOUT_ERROR);
        } catch (IOException e) {
            logIOException(e);
            throw new ServiceUnavailableException(COMUNICATION_ERROR);
        } catch (InterruptedException e) {
            logInterruptedException(e);
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(INTERRUPT_OPERATION_ERROR);
        }
        return response;
    }

    private GenericServiceException handleGenericException(String message, Exception e) {
        logUnexpectedException(e);
        return new GenericServiceException(message, e);
    }

    private static void logConnectException(ConnectException e) {
        logger.error("Errore di connessione al server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private static void logTimeoutException(SocketTimeoutException e) {
        logger.error("Timeout durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private static void logIOException(IOException e) {
        logger.error("Errore di I/O durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private static void logInterruptedException(InterruptedException e) {
        logger.error("Operazione interrotta durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private static void logUnexpectedException(Exception e) {
        logger.error("Errore inatteso durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }
}
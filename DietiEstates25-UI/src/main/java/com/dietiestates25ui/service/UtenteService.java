package com.dietiestates25ui.service;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.dto.ApiResponse;
import com.dietiestates25ui.dto.CsrfResponse;
import com.dietiestates25ui.dto.LoginResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ResourceNotFoundException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.Utente;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);
    private static final String BASE_URL = "http://localhost:8080/api/users";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TIMEOUT_ERROR = "Timeout durante la comunicazione con il server. Riprova più tardi.";
    public static final String CONNECTION_ERROR = "Impossibile connettersi al server. Verifica la connessione e riprova.";
    public static final String COMUNICATION_ERROR = "Errore durante la comunicazione con il server. Riprova più tardi.";
    public static final String INTERRUPT_OPERATION_ERROR = "Operazione interrotta. Riprova.";

    private static String csrfTokenValue; // Store the CSRF token here
    private static String csrfTokenHeaderName;

    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    // Method to fetch CSRF token
    public static void fetchCsrfToken() throws ServiceUnavailableException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/csrf")) //TODO:FIX ME
                    .GET()
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                CsrfResponse csrfResponse = objectMapper.readValue(response.body(), CsrfResponse.class);
                if (csrfResponse != null && csrfResponse.getToken() != null && csrfResponse.getHeaderName() != null) {
                    csrfTokenValue = csrfResponse.getToken();
                    csrfTokenHeaderName = csrfResponse.getHeaderName(); // Memorizza anche l'header name
                    logger.info("CSRF token fetched successfully: " + csrfTokenValue + ", Header Name: " + csrfTokenHeaderName);
                } else {
                    logger.error("Failed to parse CSRF token from response: " + response.body());
                    throw new GenericServiceException("Failed to fetch CSRF token.");
                }
            } else {
                logger.error("Failed to fetch CSRF token. Status code: " + statusCode + ", Response: " + response.body());
                throw new ServiceUnavailableException("Failed to fetch CSRF token: " + statusCode);
            }
        } catch (Exception e) {
            logger.error("Exception while fetching CSRF token: " + e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to fetch CSRF token: " + e.getMessage());
        }
    }

    private <T> ApiResponse<T> handleResponse(HttpResponse<String> response, ObjectMapper objectMapper, Class<T> dataType) throws ApiClientException {
        try {
            Type type = new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{dataType};
                }

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

    public UtenteDTO registraUtente(Utente user) throws GenericServiceException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(user);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/register"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();
            logger.info("Registrazione utente effettuata con successo. Status code: {}", statusCode);

            if (statusCode == 201) {
                ApiResponse<UtenteDTO> apiResponse = handleResponse(response, objectMapper, UtenteDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    return apiResponse.getData();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto durante la registrazione.";
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
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(Map.of("email", user.getEmail(), "password", user.getPassword()));

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/login"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(csrfTokenHeaderName, csrfTokenValue) // Usa l'header name corretto
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<LoginResponse> apiResponse = handleResponse(response, objectMapper, LoginResponse.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    LoginResponse loginResponse = apiResponse.getData();
                    logger.info("Login effettuato con successo per l'utente: {}", user.getEmail());
                    return loginResponse.getToken();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Login fallito.";
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

    public void updateUtente(Utente user, String token) throws ServiceUnavailableException, GenericServiceException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody;
            if (user.getCitta() != null) {
                jsonBody = objectMapper.writeValueAsString(
                        Map.of("nome", user.getNome(), "cognome", user.getCognome(), "citta", user.getCitta()));
            } else {
                jsonBody = objectMapper.writeValueAsString(
                        Map.of("nome", user.getNome(), "cognome", user.getCognome()));
            }

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/update"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header(csrfTokenHeaderName, csrfTokenValue) // Use the CSRF Header name
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            logger.info("Aggiornamento utente effettuato. Status code: {}", statusCode);
            if (logger.isTraceEnabled()) {
                logger.trace("Response Body: {}", response.body());
            }

            handleUpdateRequestStatusCode(statusCode, response, objectMapper);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }

    }

    private void handleUpdateRequestStatusCode(int statusCode, HttpResponse<String> response, ObjectMapper objectMapper) throws ApiClientException, GenericServiceException, ServiceUnavailableException {
        if (statusCode == 200) {
            ApiResponse<UtenteDTO> apiResponse = handleResponse(response, objectMapper, UtenteDTO.class);
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante l'aggiornamento dell'utente.";
                throw new GenericServiceException(errorMessage);
            }

        } else if (statusCode >= 400 && statusCode < 500) {
            logUpdateFailedClientError(statusCode, response.body());
            throw new ApiClientException("Errore durante l'aggiornamento: " + statusCode + ". Controlla i dati inseriti.");
        } else if (statusCode >= 500) {
            logUpdateFailedServerError(statusCode, response.body());
            throw new ServiceUnavailableException("Errore del server durante l'aggiornamento. Riprova più tardi.");
        } else {
            throw new GenericServiceException("Aggiornamento fallito con status code: " + statusCode);
        }
    }

    public UtenteDTO getUtenteDetails(String token) throws ServiceUnavailableException, ApiClientException, ResourceNotFoundException, GenericServiceException {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/me"))
                    .header("Authorization", "Bearer " + token).GET().build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                ApiResponse<UtenteDTO> apiResponse = handleResponse(response, new ObjectMapper(), UtenteDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    UtenteDTO utenteDTO = apiResponse.getData();
                    logger.info("Dettagli utente recuperati con successo.");
                    return utenteDTO;
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Impossibile recuperare i dettagli dell'utente.";
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


    private static void logEmailAlreadyInUse(HttpResponse<String> response) {
        if (logger.isWarnEnabled()) {
            logger.warn("Email già in uso durante la registrazione. Response body: {}", response.body());
        }
    }

    private static void logClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore del client durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private static void logServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private static void logLoginFailed(String email, int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Tentativo di login fallito per l'utente: {}. Status code: {}", email, statusCode);
        }
    }

    private static void logUpdateFailedClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore nell'aggiornamento utente: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private static void logUpdateFailedServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante l'aggiornamento: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private static void logGetDetailsFailed(int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Impossibile recuperare i dettagli dell'utente. Status code: {}", statusCode);
        }
    }
}
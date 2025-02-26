package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25ui.dto.CsrfResponse;
import com.dietiestates25ui.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

public abstract class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String TIMEOUT_ERROR = "Timeout durante la comunicazione con il server. Riprova più tardi.";
    public static final String CONNECTION_ERROR = "Impossibile connettersi al server. Verifica la connessione e riprova.";
    public static final String COMUNICATION_ERROR = "Errore durante la comunicazione con il server. Riprova più tardi.";
    public static final String INTERRUPT_OPERATION_ERROR = "Operazione interrotta. Riprova.";

    protected static String csrfTokenValue;
    protected static String csrfTokenHeaderName;

    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private static final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    protected abstract String getBaseUrl();

    public static void fetchCsrfToken() throws ServiceUnavailableException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/csrf"))
                    .GET()
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
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

    protected <D> D executeAndHandle(String path, String method, Object body, String token, Class<D> dtoClass) throws GenericServiceException {
        try {
            if (!method.equalsIgnoreCase("GET")) {
                fetchCsrfToken();
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + path))
                    .header(CONTENT_TYPE, APPLICATION_JSON);

            if (csrfTokenValue != null && !csrfTokenValue.isEmpty() && csrfTokenHeaderName != null && !csrfTokenHeaderName.isEmpty()) {
                requestBuilder.header(csrfTokenHeaderName, csrfTokenValue);
            }

            if (token != null && !token.isEmpty()) {
                requestBuilder.header(AUTHORIZATION, BEARER + token);
            }

            HttpRequest request;
            String jsonBody;
            switch (method.toUpperCase()) {
                case "POST":
                    jsonBody = objectMapper.writeValueAsString(body);
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                    break;
                case "PUT":
                    jsonBody = objectMapper.writeValueAsString(body);
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
                    break;
                case "GET":
                    requestBuilder.GET();
                    break;
                default:
                    throw new IllegalArgumentException("Metodo HTTP non supportato: " + method);
            }

            request = requestBuilder.build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            return getDataFromStatusCode(dtoClass, statusCode, response);

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    private <D> D getDataFromStatusCode(Class<D> dtoClass, int statusCode, HttpResponse<String> response) throws ApiClientException, GenericServiceException, AuthenticationException, ServiceUnavailableException, ResourceNotFoundException {
        if (statusCode >= 200 && statusCode < 300) {
            ApiResponse<D> apiResponse = handleResponse(response, dtoClass);
            if (apiResponse != null && apiResponse.isSuccess()) {
                return apiResponse.getData();
            } else {
                String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto.";
                throw new GenericServiceException(errorMessage);
            }
        } else {
            handleErrorResponse(statusCode, response);
            throw new GenericServiceException("Operazione fallita con status code: " + statusCode);
        }
    }

    protected abstract void handleErrorResponse(int statusCode, HttpResponse<String> response) throws AuthenticationException, ApiClientException, ServiceUnavailableException, ResourceNotFoundException;

    protected <T> ApiResponse<T> handleResponse(HttpResponse<String> response, Class<T> dataType) throws ApiClientException {
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

    protected static HttpResponse<String> executeRequest(HttpRequest request) throws ServiceUnavailableException {
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

    protected GenericServiceException handleGenericException(String message, Exception e) {
        logUnexpectedException(e);
        return new GenericServiceException(message, e);
    }

    protected static void logGenericException(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore generico durante la comunicazione con il server: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private static void logConnectException(ConnectException e) {
        logger.error("Errore di connessione al server. Messaggio: {}", e.getMessage());
    }

    private static void logTimeoutException(SocketTimeoutException e) {
        logger.error("Timeout durante la comunicazione con il server. Messaggio: {}", e.getMessage());
    }

    private static void logIOException(IOException e) {
        logger.error("Errore di I/O durante la comunicazione con il server. Messaggio: {}", e.getMessage());
    }

    private static void logInterruptedException(InterruptedException e) {
        logger.error("Operazione interrotta durante la comunicazione con il server. Messaggio: {}", e.getMessage());
    }

    private static void logUnexpectedException(Exception e) {
        logger.error("Errore inatteso durante la comunicazione con il server. Messaggio: {}", e.getMessage());
    }


    protected static void logEmailAlreadyInUse(HttpResponse<String> response) {
        if (logger.isWarnEnabled()) {
            logger.warn("Email già in uso durante la registrazione. Response body: {}", response.body());
        }
    }

    protected static void logClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore del client durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    protected static void logServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    protected static void logLoginFailed(String email, int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Tentativo di login fallito per l'utente: {}. Status code: {}", email, statusCode);
        }
    }

    protected static void logUpdateFailedClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore nell'aggiornamento utente: {}, Response body: {}", statusCode, responseBody);
        }
    }

    protected static void logUpdateFailedServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante l'aggiornamento: {}, Response body: {}", statusCode, responseBody);
        }
    }

    protected static void logGetDetailsFailed(int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Impossibile recuperare i dettagli dell'utente. Status code: {}", statusCode);
        }
    }
}
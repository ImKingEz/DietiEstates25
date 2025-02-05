package dietiestates25ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dietiestates25ui.dto.LoginResponse;
import dietiestates25ui.dto.UtenteDTO;
import dietiestates25ui.exception.ApiClientException;
import dietiestates25ui.exception.AuthenticationException;
import dietiestates25ui.exception.GenericServiceException;
import dietiestates25ui.exception.ResourceNotFoundException;
import dietiestates25ui.exception.ServiceUnavailableException;
import dietiestates25ui.model.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
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

    private <T> T handleResponse(HttpResponse<String> response, ObjectMapper objectMapper, Class<T> responseType) throws ApiClientException {
        try {
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            logger.error("Errore durante la lettura della risposta JSON: ", e);
            throw new ApiClientException("Risposta del server non valida. Riprova più tardi.");
        }
    }


    public HttpResponse<String> registraUtente(Utente user) throws GenericServiceException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(user);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/register"))
                    .header(CONTENT_TYPE, APPLICATION_JSON).POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();
            logger.info("Registrazione utente effettuata con successo. Status code: {}", statusCode);

            if (statusCode == 409) {
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

            return response;

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
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                LoginResponse loginResponse = handleResponse(response, new ObjectMapper(), LoginResponse.class);
                logger.info("Login effettuato con successo per l'utente: {}", user.getEmail());
                return loginResponse.getToken();

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
                    .header(CONTENT_TYPE, APPLICATION_JSON).header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            logger.info("Aggiornamento utente effettuato. Status code: {}", statusCode);
            if (logger.isTraceEnabled()) {
                logger.trace("Response Body: {}", response.body());
            }

            if (statusCode >= 400 && statusCode < 500) {
                logUpdateFailedClientError(statusCode, response.body());
                throw new ApiClientException("Errore durante l'aggiornamento: " + statusCode + ". Controlla i dati inseriti.");
            } else if (statusCode >= 500) {
                logUpdateFailedServerError(statusCode, response.body());
                throw new ServiceUnavailableException("Errore del server durante l'aggiornamento. Riprova più tardi.");
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }

    }

    public UtenteDTO getUtenteDetails(String token) throws ServiceUnavailableException, ApiClientException, ResourceNotFoundException, GenericServiceException {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/me"))
                    .header("Authorization", "Bearer " + token).GET().build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                UtenteDTO utenteDTO = handleResponse(response, new ObjectMapper(), UtenteDTO.class);
                logger.info("Dettagli utente recuperati con successo.");
                return utenteDTO;
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


    private HttpResponse<String> executeRequest(HttpRequest request) throws ServiceUnavailableException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
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


    private void logConnectException(ConnectException e) {
        logger.error("Errore di connessione al server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private void logTimeoutException(SocketTimeoutException e) {
        logger.error("Timeout durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private void logIOException(IOException e) {
        logger.error("Errore di I/O durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private void logInterruptedException(InterruptedException e) {
        logger.error("Operazione interrotta durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private void logUnexpectedException(Exception e) {
        logger.error("Errore inatteso durante la comunicazione con il server: {}. Messaggio: {}", BASE_URL, e.getMessage());
    }

    private void logEmailAlreadyInUse(HttpResponse<String> response) {
        if (logger.isWarnEnabled()) {
            logger.warn("Email già in uso durante la registrazione. Response body: {}", response.body());
        }
    }

    private void logClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore del client durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private void logServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante la registrazione: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private void logLoginFailed(String email, int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Tentativo di login fallito per l'utente: {}. Status code: {}", email, statusCode);
        }
    }

    private void logUpdateFailedClientError(int statusCode, String responseBody) {
        if (logger.isWarnEnabled()) {
            logger.warn("Errore nell'aggiornamento utente: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private void logUpdateFailedServerError(int statusCode, String responseBody) {
        if (logger.isErrorEnabled()) {
            logger.error("Errore del server durante l'aggiornamento: {}, Response body: {}", statusCode, responseBody);
        }
    }

    private void logGetDetailsFailed(int statusCode) {
        if (logger.isWarnEnabled()) {
            logger.warn("Impossibile recuperare i dettagli dell'utente. Status code: {}", statusCode);
        }
    }
}
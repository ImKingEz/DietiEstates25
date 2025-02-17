package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.dto.CsrfResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.Immobile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public class ImmobileService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);
    private static final String BASE_URL = "http://localhost:8080/api/immobili";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TIMEOUT_ERROR = "Timeout durante la comunicazione con il server. Riprova più tardi.";
    public static final String CONNECTION_ERROR = "Impossibile connettersi al server. Verifica la connessione e riprova.";
    public static final String COMUNICATION_ERROR = "Errore durante la comunicazione con il server. Riprova più tardi.";
    public static final String INTERRUPT_OPERATION_ERROR = "Operazione interrotta. Riprova.";

//    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
//    private static final HttpClient client = HttpClient.newBuilder()
//            .cookieHandler(cookieManager)
//            .build();

    public void salvaImmobile(Immobile immobile, String token, List<File> selectedImageList) throws GenericServiceException {
        try {
            // Crea un oggetto FormData
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            publisher.addFormDataPart("titolo", immobile.getTitolo());
            publisher.addFormDataPart("tipologia", immobile.getTipologia());
            publisher.addFormDataPart("indirizzo", immobile.getIndirizzo());
            publisher.addFormDataPart("prezzo", String.valueOf(immobile.getPrezzo()));
            publisher.addFormDataPart("descrizione", immobile.getDescrizione());
            publisher.addFormDataPart("dimensione", String.valueOf(immobile.getDimensione()));
            publisher.addFormDataPart("numero_camere", String.valueOf(immobile.getNumero_camere()));
            publisher.addFormDataPart("numero_bagni", String.valueOf(immobile.getNumero_bagni()));
            publisher.addFormDataPart("classeEnergetica", immobile.getClasseEnergetica());
            publisher.addFormDataPart("piano", String.valueOf(immobile.getPiano()));
            publisher.addFormDataPart("ascensore", String.valueOf(immobile.isAscensore()));
            publisher.addFormDataPart("portineria", String.valueOf(immobile.isPortineria()));
            publisher.addFormDataPart("climatizzazione", String.valueOf(immobile.isClimatizzazione()));
            publisher.addFormDataPart("latitudine", String.valueOf(immobile.getLatitudine()));
            publisher.addFormDataPart("longitudine", String.valueOf(immobile.getLongitudine()));
            publisher.addFormDataPart("vicinoScuole", String.valueOf(immobile.isVicinoScuole()));
            publisher.addFormDataPart("vicinoParchi", String.valueOf(immobile.isVicinoParchi()));
            publisher.addFormDataPart("vicinoTrasportoPubblico", String.valueOf(immobile.isVicinoTrasportoPubblico()));

            for (File file : selectedImageList) {
                if (file != null) {
                    Path logoPath = Paths.get(file.getAbsolutePath());
                    String mimeType = Files.probeContentType(file.toPath());
                    publisher.addFilePart("immaginiUrls", file.getName(), mimeType, logoPath);
                }
            }

            fetchCsrfToken(); // Assicurati di avere il token CSRF più recente

            logger.debug("prima di byte");
            byte[] requestBody = publisher.build();
            logger.debug("dopo di byte");

            logger.debug("publisher.getContentType(): {}", publisher.getContentType());
            logger.debug("requestBody.length: {}", requestBody.length);
            logger.debug("token: {}", token);
            logger.debug("csrfTokenHeaderName: {}", csrfTokenHeaderName);
            logger.debug("csrfTokenValue: {}", csrfTokenValue);
            HttpRequest request;
            try {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/immobili/create"))
                        .header("Content-Type", publisher.getContentType())
                        .header("Authorization", "Bearer " + token)
                        .header(csrfTokenHeaderName, csrfTokenValue)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                        .build();
            } catch (Exception e) {
                logger.error("Errore durante la creazione della richiesta: {}", e.getMessage(), e);
                throw new GenericServiceException("Errore durante la creazione della richiesta: " + e.getMessage());
            }

            logger.debug("Sending request to save immobile...");
            HttpResponse<String> response = executeRequest(request);
            logger.debug("Response received: {}", response.body());
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<ImmobileDTO> apiResponse = handleResponse(response, ImmobileDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Immobile salvato correttamente!");
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante il salvataggio dell'immobile.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                // Se lo status code non è 201, deserializza la risposta per ottenere il messaggio di errore
                try {
                    ApiResponse<ImmobileDTO> apiResponse = handleResponse(response, ImmobileDTO.class);
                    if (apiResponse != null && apiResponse.getMessage() != null) {
                        throw new GenericServiceException("Salvataggio immobile fallito: " + apiResponse.getMessage());
                    }
                } catch (ApiClientException e) {
                    // Se la risposta non è un ApiResponse valido, usa il messaggio di errore generico
                    throw new GenericServiceException("Salvataggio immobile fallito con status code: " + statusCode + ". Errore non specificato dal server.");
                }
            }

        } catch (ApiClientException e) {
            logger.error("Errore durante il salvataggio dell'immobile: {}", e.getMessage());
            throw handleGenericException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Errore durante il salvataggio dell'immobile: {}", e.getMessage());
            throw handleGenericException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        }
    }

    static class MultipartBodyPublisher {
        private final String boundary;
        private final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        private static final String LINE_FEED = "\r\n";

        public MultipartBodyPublisher() {
            this.boundary = generateBoundary();
        }

        private String generateBoundary() {
            SecureRandom random = new SecureRandom();
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                buffer.append(characters.charAt(random.nextInt(characters.length())));
            }
            return buffer.toString();
        }

        public void addFormDataPart(String name, String value) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED).getBytes());
                //Remove Content Type
                outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                outputStream.write(value.getBytes());
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                logger.error("Error while adding form data part: {}", e.getMessage());
            }
        }

        public void addFilePart(String fieldName, String fileName, String mimeType, Path filePath) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: " + mimeType + LINE_FEED).getBytes());
                outputStream.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                Files.copy(filePath, outputStream);
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                logger.error("Error while adding file part: {}", e.getMessage());
            }
        }

        public byte[] build() {
            try {
                outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            } catch (IOException e) {
                logger.error("Error while closing multipart body: {}", e.getMessage());
            }
            return outputStream.toByteArray();
        }

        public String getContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }
    }
}
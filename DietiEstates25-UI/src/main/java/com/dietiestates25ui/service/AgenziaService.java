package com.dietiestates25ui.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.AuthenticationException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import com.dietiestates25ui.model.AgenziaImmobiliare;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgenziaService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);
    private static final String BASE_URL = "http://localhost:8080/api/agenzie";

    protected static String getBaseUrl() {
        return BASE_URL;
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

            if (statusCode == 200) {
                ApiResponse<AgenziaDTO> apiResponse = handleResponse(response, AgenziaDTO.class); //Rimosso objectMapper
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

    public void registerAgenzia(AgenziaImmobiliare agenzia, File logoFile, String email, String password) throws GenericServiceException {
        try {
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            publisher.addFormDataPart("nome", agenzia.getNome());
            publisher.addFormDataPart("partitaIva", agenzia.getPartitaIva());
            publisher.addFormDataPart("indirizzo", agenzia.getIndirizzo());
            publisher.addFormDataPart("email", agenzia.getEmail());
            publisher.addFormDataPart("telefono", agenzia.getTelefono());
            publisher.addFormDataPart("password", password);

            if (logoFile != null) {
                Path logoPath = Paths.get(logoFile.getAbsolutePath());
                String mimeType = Files.probeContentType(logoPath);
                publisher.addFilePart("logo", logoFile.getName(), mimeType, logoPath);
            }

            fetchCsrfToken();

            byte[] requestBody = publisher.build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/agenzie/register"))
                    .header("Content-Type", publisher.getContentType())
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<AgenziaDTO> apiResponse =
                        handleResponse(response, AgenziaDTO.class); // Rimosso objectMapper
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

    public String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        sb.append((char) (random.nextInt(26) + 'A'));

        sb.append((char) (random.nextInt(10) + '0'));

        for (int i = 2; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        List<Character> charList = new java.util.ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            charList.add(c);
        }
        java.util.Collections.shuffle(charList);

        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : charList) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }

    static class MultipartBodyPublisher {
        private final String boundary;
        private final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        private final String LINE_FEED = "\r\n";

        public MultipartBodyPublisher() {
            this.boundary = generateBoundary();
        }

        private String generateBoundary() {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder buffer = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 20; i++) { // A shorter boundary
                buffer.append(characters.charAt(random.nextInt(characters.length())));
            }
            return buffer.toString();
        }

        public void addFormDataPart(String name, String value) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                outputStream.write(value.getBytes());
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }

        public byte[] build() {
            try {
                outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputStream.toByteArray();
        }

        public String getContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }
    }
}
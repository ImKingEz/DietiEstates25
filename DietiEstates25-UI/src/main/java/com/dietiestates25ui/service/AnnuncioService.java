package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.MapSearchDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.Annuncio;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

public class AnnuncioService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AnnuncioService.class);

    public void salvaAnnuncio(Annuncio annuncio, String token, List<File> selectedImageList) throws GenericServiceException {
        try {
            AnnuncioService.MultipartBodyPublisher publisher = new AnnuncioService.MultipartBodyPublisher();
            addFormDataPartAndFilePartForPublisher(annuncio, selectedImageList, publisher);

            fetchCsrfToken();

            byte[] requestBody = publisher.build();

            HttpRequest request;
            request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/annunci/create"))
                    .header("Content-Type", publisher.getContentType())
                    .header("Authorization", "Bearer " + token)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<AnnuncioDTO> apiResponse = handleResponse(response, AnnuncioDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Annuncio salvato correttamente!");
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante il salvataggio dell'annuncio.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                handleErrorResponse(statusCode, response);
                throw new GenericServiceException("Salvataggio annuncio fallito con status code: " + statusCode + ". Errore non specificato dal server.");
            }
        } catch (Exception e) {
            throw handleGenericException("Errore durante il salvataggio dell'annuncio: " + e.getMessage(), e);
        }
    }

    private static void addFormDataPartAndFilePartForPublisher(Annuncio annuncio, List<File> selectedImageList, AnnuncioService.MultipartBodyPublisher publisher) throws IOException {
        publisher.addFormDataPart("titolo", annuncio.getTitolo());
        publisher.addFormDataPart("tipo", annuncio.getTipo());
        publisher.addFormDataPart("prezzo", String.valueOf(annuncio.getPrezzo()));
        publisher.addFormDataPart("descrizione", annuncio.getDescrizione());
        publisher.addFormDataPart("idImmobile", String.valueOf(annuncio.getIdImmobile()));
        publisher.addFormDataPart("idAgente", String.valueOf(annuncio.getIdAgente()));

        for (File file : selectedImageList) {
            if (file != null) {
                Path logoPath = Paths.get(file.getAbsolutePath());
                String mimeType = Files.probeContentType(file.toPath());
                publisher.addFilePart("immaginiUrls", file.getName(), mimeType, logoPath);
            }
        }
    }

    @Override
    protected String getBaseUrl() {
        return "http://localhost:8080/api/annunci";
    }

    public List<AnnuncioDTO> searchAnnunciByCittaAndFiltro(String citta, FiltroAnnunci filtro, String token) throws GenericServiceException {
        try {
            fetchCsrfToken();

            ObjectMapper objectMapper = new ObjectMapper();
            String filtroJson = objectMapper.writeValueAsString(filtro);

            String encodedCitta = URLEncoder.encode(citta, StandardCharsets.UTF_8);
            String uri = getBaseUrl() + "/search?citta=" + encodedCitta;


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .header(AUTHORIZATION, BEARER + token)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(filtroJson))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                TypeReference<ApiResponse<List<AnnuncioDTO>>> typeReference = new TypeReference<ApiResponse<List<AnnuncioDTO>>>() {};
                ApiResponse<List<AnnuncioDTO>> apiResponse = null;
                try {
                    apiResponse = objectMapper.readValue(response.body(), typeReference);
                } catch (IOException e) {
                    logger.error("Errore durante la deserializzazione della risposta JSON: {}", e.getMessage(), e);
                    throw new ApiClientException("Errore nella risposta del server. Impossibile leggere gli annunci.");
                }


                if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return apiResponse.getData();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto durante la ricerca.";
                    logger.warn("Ricerca annunci fallita: {}", errorMessage);
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                handleErrorResponse(statusCode, response);
                throw new GenericServiceException("Operazione fallita con status code: " + statusCode);
            }
        } catch (Exception e) {
            throw handleGenericException("Errore durante la ricerca degli annunci: " + e.getMessage(), e);
        }
    }

    public List<AnnuncioDTO> searchAnnunciByMap(MapSearchDTO mapSearchDTO, FiltroAnnunci filtro, String token) throws GenericServiceException {
        try {
            fetchCsrfToken();

            ObjectMapper objectMapper = new ObjectMapper();
            String mapSearchJson = objectMapper.writeValueAsString(mapSearchDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/search-map?tipoAnnuncio=" + URLEncoder.encode(filtro.getTipo(), StandardCharsets.UTF_8) +
                            "&tipologiaImmobile=" + URLEncoder.encode(filtro.getTipologia(), StandardCharsets.UTF_8)))
                    .header("Content-Type", "application/json")
                    .header(AUTHORIZATION, BEARER + token)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(mapSearchJson))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                TypeReference<ApiResponse<List<AnnuncioDTO>>> typeReference = new TypeReference<ApiResponse<List<AnnuncioDTO>>>() {};
                ApiResponse<List<AnnuncioDTO>> apiResponse = null;
                try {
                    apiResponse = objectMapper.readValue(response.body(), typeReference);
                } catch (IOException e) {
                    logger.error("Errore durante la deserializzazione della risposta JSON: {}", e.getMessage(), e);
                    throw new ApiClientException("Errore nella risposta del server. Impossibile leggere gli annunci.");
                }

                if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return apiResponse.getData();
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto durante la ricerca.";
                    logger.warn("Ricerca annunci fallita: {}", errorMessage);
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                handleErrorResponse(statusCode, response);
                throw new GenericServiceException("Operazione fallita con status code: " + statusCode);
            }
        } catch (Exception e) {
            throw handleGenericException("Errore durante la ricerca degli annunci con mappa: " + e.getMessage(), e);
        }
    }

    @Override
    protected void handleErrorResponse(int statusCode, HttpResponse<String> response) throws ApiClientException, ServiceUnavailableException {
        logger.error("Ricevuta risposta con status code: {}", statusCode);

        if (statusCode >= 400 && statusCode < 500) {
            logClientError(statusCode, response.body());
            throw new ApiClientException("Errore del client: " + statusCode);
        } else if (statusCode >= 500) {
            logServerError(statusCode, response.body());
            throw new ServiceUnavailableException("Errore del server.");
        } else {
            logGenericException(statusCode, response.body());
            throw new ApiClientException("Errore generico: " + statusCode);
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
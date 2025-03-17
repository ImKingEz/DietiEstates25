package com.dietiestates25ui.service;

import com.dietiestates25ui.exception.ApiClientException;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;

public class GeoapifyService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(GeoapifyService.class);

    @Override
    protected String getBaseUrl() {
        return "http://" + BACKEND_IP + ":8080/api/geoapify";
    }

    public String getKey(String token) throws GenericServiceException {
        return executeAndHandle("/key",
                "GET",
                null,
                token,
                String.class);
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
}

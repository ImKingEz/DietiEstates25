package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;

public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(true, data, null);
        return ResponseEntity.status(status).body(response);
    }

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return successResponse(data, HttpStatus.OK);
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(Exception ex, String message, HttpStatus status, String entityType, String entityId) {
        logger.error("Errore durante l'operazione su {}: {} - {}", entityType, entityId, ex.getMessage(), ex);
        ApiResponse<T> response = new ApiResponse<>(false, null, message + ": " + ex.getMessage());
        return ResponseEntity.status(status).body(response);
    }

    protected <T> ResponseEntity<ApiResponse<T>> handleDataIntegrityViolation(DataIntegrityViolationException ex, String entityType) {
        logger.error("Violazione dell'integrità dei dati durante l'operazione su {}: {}", entityType, ex.getMessage(), ex);
        ApiResponse<T> response = new ApiResponse<>(false, null, "Email già in uso");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    protected <T> ResponseEntity<ApiResponse<T>> handleGenericException(Exception ex, String message, String entityType) {
        logger.error("Errore generico durante l'operazione su {}: {}", entityType, ex.getMessage(), ex);
        ApiResponse<T> response = new ApiResponse<>(false, null, message + ": " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

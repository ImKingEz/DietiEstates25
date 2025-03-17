package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geoapify")
public class GeoapifyController {

    private static final Logger logger = LoggerFactory.getLogger(GeoapifyController.class);

    @GetMapping("/key")
    @PreAuthorize("hasRole('ROLE_AGENTE')")
    public ResponseEntity<ApiResponse<String>> getKey() {
        String key = System.getenv("GEOAPIFY_API_KEY");
        logger.debug("getKey() called, key : {}", key);
        return new ResponseEntity<>(new ApiResponse<>(true, key, null), HttpStatus.OK);
    }
}

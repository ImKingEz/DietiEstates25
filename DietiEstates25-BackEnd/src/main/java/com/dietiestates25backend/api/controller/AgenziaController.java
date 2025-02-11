package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25backend.api.dto.RegisterAgenziaDTO;
import com.dietiestates25backend.business.service.AgenziaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenzie")
public class AgenziaController {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaController.class);

    private final AgenziaService agenziaService;

    @Autowired
    public AgenziaController(AgenziaService agenziaService) {
        this.agenziaService = agenziaService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AgenziaDTO>> registerAgenzia(@RequestBody @Valid RegisterAgenziaDTO registerAgenziaDTO) {
        logger.debug("registerAgenzia() called with registerAgenziaDTO: {}", registerAgenziaDTO.getEmail());

        try {
            AgenziaDTO agenziaDTO = agenziaService.registraAgenzia(registerAgenziaDTO);
            logger.debug("registerAgenzia() successful with agenzia: {}", agenziaDTO.getEmail());
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(true, agenziaDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerAgenzia() failed, email already registered: {}", registerAgenziaDTO.getEmail());
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception ex) {
            logger.error("registerAgenzia() failed with error: {}", ex.getMessage());
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Errore durante la registrazione: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
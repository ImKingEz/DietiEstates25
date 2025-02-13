package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25backend.api.dto.RegisterAgenteDTO;
import com.dietiestates25backend.business.service.AgenteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agenti")
public class AgenteController {

    private static final Logger logger = LoggerFactory.getLogger(AgenteController.class);

    private final AgenteService agenteService;

    @Autowired
    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AgenteDTO>> registerAgente(@RequestBody @Valid RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("registerAgente() called with registerAgenteDTO: {}", registerAgenteDTO.getEmail());
        try {
            AgenteDTO agenteDTO = agenteService.registraAgente(registerAgenteDTO);
            logger.debug("registerAgente() successful with agente: {}", agenteDTO.getEmail());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(true, agenteDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerAgente() failed, email already registered: {}", registerAgenteDTO.getEmail());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception ex) {
            logger.error("registerAgente() failed with error: {}", ex.getMessage());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Errore durante la registrazione: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<AgenteDTO>> updateAgente(@RequestBody @Valid RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("updateAgente() called with registerAgenteDTO: {}", registerAgenteDTO.getEmail());
        try {
            AgenteDTO agenteDTO = agenteService.registraAgente(registerAgenteDTO);
            logger.debug("updateAgente() successful with agente: {}", agenteDTO.getEmail());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(true, agenteDTO, null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DataIntegrityViolationException ex) {
            logger.error("updateAgente() failed, email already registered: {}", registerAgenteDTO.getEmail());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception ex) {
            logger.error("updateAgente() failed with error: {}", ex.getMessage());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Errore durante la registrazione: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgenteDTO>> getAgenteDetails(@PathVariable Long id) {
        logger.debug("getAgenteDetails() called with id: {}", id);
        try {
            AgenteDTO agenteDTO = agenteService.getAgenteDetails(id);
            logger.debug("getAgenteDetails() successful for agente with id: {}", id);
            ApiResponse<AgenteDTO> response = new ApiResponse<>(true, agenteDTO, null);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("getAgenteDetails() failed with error: {}", ex.getMessage());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Errore nel recupero dei dettagli dell'agente: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
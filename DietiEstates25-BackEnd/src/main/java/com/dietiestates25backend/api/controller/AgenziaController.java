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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/agenzie")
public class AgenziaController {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaController.class);

    private final AgenziaService agenziaService;

    @Autowired
    public AgenziaController(AgenziaService agenziaService) {
        this.agenziaService = agenziaService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AgenziaDTO>> registerAgenzia(
            @RequestPart("nome") String nome,
            @RequestPart("partitaIva") String partitaIva,
            @RequestPart("indirizzo") String indirizzo,
            @RequestPart("email") String email,
            @RequestPart("telefono") String telefono,
            @RequestPart("password") String password,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        RegisterAgenziaDTO registerAgenziaDTO = new RegisterAgenziaDTO();
        registerAgenziaDTO.setNome(nome);
        registerAgenziaDTO.setPartitaIva(partitaIva);
        registerAgenziaDTO.setIndirizzo(indirizzo);
        registerAgenziaDTO.setEmail(email);
        registerAgenziaDTO.setTelefono(telefono);
        registerAgenziaDTO.setPassword(password);
        registerAgenziaDTO.setLogo(logo);

        try {
            AgenziaDTO agenziaDTO = agenziaService.registraAgenzia(registerAgenziaDTO);
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(true, agenziaDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException ex) {
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception ex) {
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Errore durante la registrazione: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{agenziaId}")
    public ResponseEntity<ApiResponse<AgenziaDTO>> getAgenziaDetails(@PathVariable Long agenziaId) {
        try {
            AgenziaDTO agenziaDTO = agenziaService.getAgenziaDetails(agenziaId);
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(true, agenziaDTO, null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Agenzia non trovata con ID: {}", agenziaId);
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Agenzia non trovata");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            logger.error("Errore durante il recupero dei dettagli dell'agenzia con ID: {}", agenziaId, ex);
            ApiResponse<AgenziaDTO> response = new ApiResponse<>(false, null, "Errore durante il recupero dei dettagli dell'agenzia: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
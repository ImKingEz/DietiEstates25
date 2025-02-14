package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25backend.api.dto.LoginDTO;
import com.dietiestates25backend.api.dto.RegisterAgenteDTO;
import com.dietiestates25backend.business.service.AgenteService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agenti")
public class AgenteController {

    private static final Logger logger = LoggerFactory.getLogger(AgenteController.class);

    private final AgenteService agenteService;
    private final JwtService jwtService;

    @Autowired
    public AgenteController(AgenteService agenteService, JwtService jwtService) {
        this.agenteService = agenteService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        logger.debug("loginAgente() called with email: {}", loginDTO.getEmail());
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader);

        try {
            String token = agenteService.loginAgente(loginDTO.getEmail(), loginDTO.getPassword(), csrfTokenHeader);
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginUser() successful for agente: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(true, loginResponse, null);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            logger.error("loginUser() failed, authentication error for agente: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Credenziali non valide");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception ex){
            logger.error("loginUser() failed with error: {}", ex.getMessage());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Login fallito : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AgenteDTO>> getUserDetails() {
        logger.debug("getUserDetails() called");

        try {
            // Ottieni l'email dell'utente autenticato dal SecurityContextHolder
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            AgenteDTO agenteDTO = agenteService.getAgenteDetails(userEmail);
            logger.debug("getUserDetails() successful for agente: {}", userEmail);
            ApiResponse<AgenteDTO> response = new ApiResponse<>(true, agenteDTO, null);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("getUserDetails() failed with error: {}", ex.getMessage());
            ApiResponse<AgenteDTO> response = new ApiResponse<>(false, null, "Errore nel recupero dei dettagli dell'agente: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
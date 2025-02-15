package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25backend.api.dto.RegisterAmministratoreDTO;
import com.dietiestates25backend.api.dto.LoginDTO;
import com.dietiestates25backend.business.service.AmministratoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AmministratoreController {

    private static final Logger logger = LoggerFactory.getLogger(AmministratoreController.class);

    private final AmministratoreService amministratoreService;

    @Autowired
    public AmministratoreController(AmministratoreService amministratoreService) {
        this.amministratoreService = amministratoreService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AmministratoreDTO>> registerAdmin(@RequestBody @Valid RegisterAmministratoreDTO registerDTO) {
        logger.debug("registerAdmin() called with registerDTO: {}", registerDTO.getEmail());
        try {
            AmministratoreDTO amministratoreDTO = amministratoreService.registraAmministratore(registerDTO);
            logger.debug("registerAdmin() successful with admin: {}", amministratoreDTO.getEmail());
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(true, amministratoreDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerAdmin() failed, email already registered: {}", registerDTO.getEmail());
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (IllegalArgumentException ex) {
            logger.error("registerAdmin() failed, agency not found: {}", registerDTO.getIdAgenzia());
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(false, null, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAdmin(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        logger.debug("loginAdmin() called with email: {}", loginDTO.getEmail());
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader);

        try {
            String token = amministratoreService.loginAmministratore(loginDTO.getEmail(), loginDTO.getPassword(), csrfTokenHeader);
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginAdmin() successful for admin: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(true, loginResponse, null);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            logger.error("loginAdmin() failed, authentication error for admin: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Credenziali non valide");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }  catch (Exception ex){
            logger.error("loginAdmin() failed with error: {}", ex.getMessage());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Login fallito : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AmministratoreDTO>> getAdminDetails() {
        logger.debug("getAdminDetails() called");
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            AmministratoreDTO amministratoreDTO = amministratoreService.getAmministratoreDetails(userEmail);
            logger.debug("getAdminDetails() successful for admin: {}", userEmail);
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(true, amministratoreDTO, null);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            logger.error("getAdminDetails() failed with error: {}", ex.getMessage());
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(false, null, "Errore nel recupero dei dettagli dell'amministratore: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
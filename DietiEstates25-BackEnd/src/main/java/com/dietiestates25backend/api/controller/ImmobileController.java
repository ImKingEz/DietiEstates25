package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.service.ImmobileService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/immobili")
public class ImmobileController extends BaseController {

    public static final String ENTITY_TYPE = "Immobile";

    private final ImmobileService immobileService;

    @Autowired
    public ImmobileController(ImmobileService immobileService, AuthService authService, JwtService jwtService) {
        this.immobileService = immobileService;
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('ROLE_AGENTE')")
    public ResponseEntity<ApiResponse<ImmobileDTO>> registerImmobile(@RequestBody @Valid ImmobileDTO immobileDTO) {
        try {
            return successResponse(immobileService.saveImmobile(immobileDTO), HttpStatus.CREATED);
        } catch (Exception ex) {
            logger.error("salvaImmobile() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante la registrazione", ENTITY_TYPE);
        }
    }
}
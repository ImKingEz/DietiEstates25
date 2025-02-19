package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25backend.api.dto.RegisterAnnuncioDTO;
import com.dietiestates25backend.business.service.AnnuncioService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController extends BaseController {

    private final AnnuncioService annuncioService;

    private final AuthService authService;

    private final JwtService jwtService;

    @Autowired
    public AnnuncioController(AnnuncioService annuncioService, AuthService authService, JwtService jwtService) {
        this.annuncioService = annuncioService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('ROLE_AGENTE')")
    @PermitAll //TODO: remove this line
    public ResponseEntity<ApiResponse<AnnuncioDTO>> createAnnuncio(
            @ModelAttribute @Valid RegisterAnnuncioDTO registerAnnuncioDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        try {
            AnnuncioDTO annuncioDTO = annuncioService.saveAnnuncio(registerAnnuncioDTO);
            return successResponse(annuncioDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'annuncio: {}", e.getMessage(), e);
            ApiResponse<AnnuncioDTO> response = new ApiResponse<>(false, null, "Errore durante la creazione dell'annuncio: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

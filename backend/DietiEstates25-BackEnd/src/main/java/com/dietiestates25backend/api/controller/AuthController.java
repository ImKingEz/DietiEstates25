package com.dietiestates25backend.api.controller;

import com.dietiestates25backend.api.dto.LoginDTO;
import com.dietiestates25backend.api.dto.RegisterDTO;
import com.dietiestates25backend.api.dto.UtenteDTO;
import com.dietiestates25backend.api.dto.LoginResponse;
import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import com.dietiestates25backend.data.repository.UtenteRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtenteRepository utenteRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterDTO registerDTO) {
        logger.debug("registerUser() called with registerDTO: {}", registerDTO.getEmail());
        Utente utente = new Utente(
                registerDTO.getNome(),
                registerDTO.getCognome(),
                registerDTO.getCitta(),
                registerDTO.getEmail(),
                registerDTO.getPassword()
        );

        try {
            UtenteDTO utenteDTO = authService.registraUtente(utente);
            logger.debug("registerUser() successful with user: {}", utenteDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(utenteDTO);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerUser() failed, email already registered: {}", registerDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email già in uso");
        } catch (Exception ex) {
            logger.error("registerUser() failed with error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la registrazione : "+ ex.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        logger.debug("loginUser() called with email: {}", loginDTO.getEmail());
        try {
            String token = authService.loginUtente(loginDTO.getEmail(), loginDTO.getPassword());
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginUser() successful for user: {}", loginDTO.getEmail());
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException ex) {
            logger.error("loginUser() failed, authentication error for user: {}", loginDTO.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        } catch (Exception ex){
            logger.error("loginUser() failed with error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed : " + ex.getMessage());
        }
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateUtente(@RequestBody com.dietiestates25backend.api.dto.UpdateUtenteDTO updateUtenteDTO, @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        logger.debug("updateUtente() called with user: {}", updateUtenteDTO);
        String token = authorizationHeader.substring(7);
        try{
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if(jwtService.isTokenValid(token, userDetails)){
                UtenteDTO utenteDTO = authService.updateUtente(updateUtenteDTO, userDetails.getUsername());
                logger.debug("updateUtente() successful for user: {}", userDetails.getUsername());
                return new ResponseEntity<>(utenteDTO, HttpStatus.OK);
            } else{
                logger.error("updateUtente() failed, token not valid");
                return new ResponseEntity<>("Token non valido", HttpStatus.UNAUTHORIZED);
            }
        } catch(Exception ex){
            logger.error("updateUtente() failed with error: {}", ex.getMessage());
            return new ResponseEntity<>("Errore durante l'aggiornamento dell'utente: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        logger.debug("getUserDetails() called");
        String token = authorizationHeader.substring(7);
        try {
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if(jwtService.isTokenValid(token, userDetails)){
                UtenteDTO utenteDTO = authService.getUtenteDetails(userDetails.getUsername());
                logger.debug("getUserDetails() successful for user: {}", userDetails.getUsername());
                return ResponseEntity.ok(utenteDTO);
            } else {
                logger.error("getUserDetails() failed, token not valid");
                return new ResponseEntity<>("Token non valido", HttpStatus.UNAUTHORIZED);
            }

        }catch (Exception ex){
            logger.error("getUserDetails() failed with error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nel recupero dei dettagli dell'utente: " + ex.getMessage());
        }
    }
}
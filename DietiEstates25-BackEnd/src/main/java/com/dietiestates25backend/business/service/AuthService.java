package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25backend.api.dto.RegisterUtenteDTO;
import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.data.repository.AgenteRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.dietiestates25backend.api.dto.UpdateUtenteDTO;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UtenteRepository userRepository;
    private final AgenteRepository agenteRepository;
    private final AmministratoreRepository amministratoreRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AuthService(UtenteRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, HttpServletRequest httpServletRequest, AgenteRepository agenteRepository, AmministratoreRepository amministratoreRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.httpServletRequest = httpServletRequest;
        this.agenteRepository = agenteRepository;
        this.amministratoreRepository = amministratoreRepository;
    }

    @Transactional
    public UtenteDTO registraUtente(RegisterUtenteDTO userDTO) {
        logger.debug("Starting registerUtente with user: {}", userDTO.getEmail());
        String emailLowerCase = userDTO.getEmail().toLowerCase();
        userDTO.setEmail(emailLowerCase);
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.error("Email already registered in utente: {}", userDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli utenti");
        }
        if (agenteRepository.existsByEmail(userDTO.getEmail())) {
            logger.error("Email already registered in agente: {}", userDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli agenti");
        }
        if (amministratoreRepository.existsByEmail(userDTO.getEmail())) {
            logger.error("Email already registered in amministratore: {}", userDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli amministratori");
        }

        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Utente user;
        if (userDTO.getCitta() == null) {
            user = new Utente(userDTO.getNome(), userDTO.getCognome(), userDTO.getEmail(), userDTO.getPassword());
        } else {
            user = new Utente(userDTO.getNome(), userDTO.getCognome(), userDTO.getCitta(), userDTO.getEmail(), userDTO.getPassword());
        }

        Utente savedUser = userRepository.save(user);

        logger.debug("Ending registerUtente with user: {}", userDTO.getEmail());
        return new UtenteDTO(savedUser.getNome(), savedUser.getCognome(), savedUser.getCitta(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public String loginUtente(String email, String password, String csrfToken) throws BadCredentialsException {
        logger.debug("Starting loginUtente with email: {}", email);
        String emailLowerCase = email.toLowerCase();
        Utente user = userRepository.findByEmail(emailLowerCase).orElseThrow(() -> new BadCredentialsException("User not found"));
        logger.debug("User retrieved from database: {}", user.getEmail());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.error("Login failed: Invalid password for user: {}", email);
            throw new BadCredentialsException("Login Failed");
        }

        CsrfToken expectedCsrfToken = (CsrfToken) httpServletRequest.getAttribute(CsrfToken.class.getName());
        if (expectedCsrfToken == null || !expectedCsrfToken.getToken().equals(csrfToken)) {
            logger.error("Login failed: Invalid CSRF token for user: {}", email);
            throw new BadCredentialsException("Invalid CSRF Token");
        }

        logger.debug("Login successful for user: {}", user.getEmail());
        return jwtService.generateToken(user);
    }

    @Transactional
    public UtenteDTO updateUtente(UpdateUtenteDTO updateUtenteDTO, String email) throws EntityNotFoundException {
        logger.debug("Starting updateUtente with email: {}", email);
        String emailLowerCase = email.toLowerCase();
        Utente user = userRepository.findByEmail(emailLowerCase).orElseThrow(() -> new EntityNotFoundException("User not found"));
        logger.debug("User retrieved from database: {}", user.getEmail());
        user.setNome(updateUtenteDTO.getNome());
        user.setCognome(updateUtenteDTO.getCognome());
        user.setCitta(updateUtenteDTO.getCitta());
        Utente savedUser =  userRepository.save(user);
        logger.debug("User updated : {}", savedUser.getId());
        UtenteDTO utenteDTO = new UtenteDTO(savedUser.getNome(), savedUser.getCognome(), savedUser.getCitta(), savedUser.getEmail());
        logger.debug("Ending updateUtente with user: {}", utenteDTO.getEmail());
        return utenteDTO;
    }

    public UtenteDTO getUtenteDetails(String email){
        String emailLowerCase = email.toLowerCase();
        Utente utente = userRepository.findByEmail(emailLowerCase).orElseThrow(()->new EntityNotFoundException("Utente not found with email: " + email));
        return new UtenteDTO(utente.getNome(), utente.getCognome(), utente.getCitta(), utente.getEmail());
    }

}
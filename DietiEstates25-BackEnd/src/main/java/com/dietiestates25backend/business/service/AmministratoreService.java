package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25backend.api.dto.RegisterAmministratoreDTO;
import com.dietiestates25backend.business.entity.Amministratore;
import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import com.dietiestates25backend.data.repository.AgenteRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.AgenziaRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AmministratoreService {

    private static final Logger logger = LoggerFactory.getLogger(AmministratoreService.class);

    private final AmministratoreRepository amministratoreRepository;
    private final UtenteRepository userRepository;
    private final AgenteRepository agenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AgenziaRepository agenziaRepository;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AmministratoreService(AmministratoreRepository amministratoreRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AgenziaRepository agenziaRepository, HttpServletRequest httpServletRequest, UtenteRepository utenteRepository, AgenteRepository agenteRepository) {
        this.amministratoreRepository = amministratoreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.agenziaRepository = agenziaRepository;
        this.userRepository = utenteRepository;
        this.httpServletRequest = httpServletRequest;
        this.agenteRepository = agenteRepository;
    }

    @Transactional
    public AmministratoreDTO registraAmministratore(RegisterAmministratoreDTO registerDTO) {
        logger.debug("Starting registraAmministratore with email: {}", registerDTO.getEmail());
        if (amministratoreRepository.existsByEmail(registerDTO.getEmail())) {
            logger.error("Admin email already registered in amministratore: {}", registerDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli amministratori");
        }
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            logger.error("Email already registered in utente: {}", registerDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli utenti");
        }
        if (agenteRepository.existsByEmail(registerDTO.getEmail())) {
            logger.error("Email already registered in agente: {}", registerDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli agenti");
        }

        AgenziaImmobiliare agenzia = agenziaRepository.findById(registerDTO.getIdAgenzia())
                .orElseThrow(() -> new IllegalArgumentException("Agenzia non trovata per l'ID: " + registerDTO.getIdAgenzia()));

        Amministratore amministratore = new Amministratore();
        amministratore.setEmail(registerDTO.getEmail());
        amministratore.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        amministratore.setIdAgenzia(agenzia.getId()); // Imposta l'ID dell'agenzia
        Amministratore savedAdmin = amministratoreRepository.save(amministratore);
        logger.debug("Admin saved: {}", savedAdmin.getId());
        return new AmministratoreDTO(savedAdmin.getEmail(), savedAdmin.getIdAgenzia());
    }

    @Transactional(readOnly = true)
    public String loginAmministratore(String email, String password, String csrfToken) throws BadCredentialsException {
        logger.debug("Starting loginAmministratore with email: {}", email);
        Amministratore amministratore = amministratoreRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Admin not found"));
        logger.debug("Admin retrieved from database: {}", amministratore.getEmail());

        if (!passwordEncoder.matches(password, amministratore.getPassword())) {
            logger.error("Login failed: Invalid password for admin: {}", email);
            throw new BadCredentialsException("Login Failed");
        }

        CsrfToken expectedCsrfToken = (CsrfToken) httpServletRequest.getAttribute(CsrfToken.class.getName());
        if (expectedCsrfToken == null || !expectedCsrfToken.getToken().equals(csrfToken)) {
            logger.error("Login failed: Invalid CSRF token for admin: {}", email);
            throw new BadCredentialsException("Invalid CSRF Token");
        }

        logger.debug("Login successful for admin: {}", amministratore.getEmail());
        return jwtService.generateToken(amministratore);
    }

    @Transactional(readOnly = true)
    public AmministratoreDTO getAmministratoreDetails(String email){
        Amministratore amministratore = amministratoreRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Admin not found with email: " + email));
        return new AmministratoreDTO(amministratore.getEmail(), amministratore.getIdAgenzia());
    }
}
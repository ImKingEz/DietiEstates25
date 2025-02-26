package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25backend.api.dto.RegisterAgenteDTO;
import com.dietiestates25backend.business.entity.AgenteImmobiliare;
import com.dietiestates25backend.data.repository.AgenteRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenteService {

    private static final Logger logger = LoggerFactory.getLogger(AgenteService.class);

    private final AgenteRepository agenteRepository;
    private final UtenteRepository userRepository;
    private final AmministratoreRepository amministratoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AgenteService(AgenteRepository agenteRepository, PasswordEncoder passwordEncoder, JwtService jwtService, HttpServletRequest httpServletRequest, UtenteRepository userRepository, AmministratoreRepository amministratoreRepository) {
        this.agenteRepository = agenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.httpServletRequest = httpServletRequest;
        this.userRepository = userRepository;
        this.amministratoreRepository = amministratoreRepository;
    }

    @Transactional(readOnly = true)
    public String loginAgente(String email, String password, String csrfToken) throws BadCredentialsException {
        logger.debug("Starting loginAgente with email: {}", email);
        String emailLowerCase = email.toLowerCase();
        AgenteImmobiliare agente = agenteRepository.findByEmail(emailLowerCase).orElseThrow(() -> new BadCredentialsException("Agente not found"));
        logger.debug("Agente retrieved from database: {}", agente.getEmail());

        if (!passwordEncoder.matches(password, agente.getPassword())) {
            logger.error("Login failed: Invalid password for agente: {}", email);
            throw new BadCredentialsException("Login Failed");
        }

        CsrfToken expectedCsrfToken = (CsrfToken) httpServletRequest.getAttribute(CsrfToken.class.getName());
        if (expectedCsrfToken == null || !expectedCsrfToken.getToken().equals(csrfToken)) {
            logger.error("Login failed: Invalid CSRF token for agente: {}", email);
            throw new BadCredentialsException("Invalid CSRF Token");
        }

        logger.debug("Login successful for agente: {}", agente.getEmail());
        return jwtService.generateToken(agente);
    }

    @Transactional
    public AgenteDTO registraAgente(RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("Starting registraAgente with email: {}", registerAgenteDTO.getEmail());
        String emailLowerCase = registerAgenteDTO.getEmail().toLowerCase();
        registerAgenteDTO.setEmail(emailLowerCase);
        if (agenteRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email already registered in agente: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli agenti");
        }
        if (userRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email already registered in utente: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli utenti");
        }
        if (amministratoreRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email already registered in amministratore: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli amministratori");
        }

        AgenteImmobiliare agente = new AgenteImmobiliare(registerAgenteDTO.getIdAgenzia(), registerAgenteDTO.getNome(), registerAgenteDTO.getCognome(), registerAgenteDTO.getDataDiNascita(), registerAgenteDTO.getSesso(), registerAgenteDTO.getEmail(), passwordEncoder.encode(registerAgenteDTO.getPassword()));

        AgenteImmobiliare savedAgente = agenteRepository.save(agente);

        logger.debug("Ending registraAgente with agente: {}", savedAgente.getEmail());
        return new AgenteDTO(savedAgente.getId(), savedAgente.getIdAgenzia(), savedAgente.getNome(), savedAgente.getCognome(), savedAgente.getDataDiNascita(), savedAgente.getSesso(), savedAgente.getEmail());
    }

    public AgenteDTO updateAgente(RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("Starting updateAgente with email: {}", registerAgenteDTO.getEmail());
        String emailLowerCase = registerAgenteDTO.getEmail().toLowerCase();
        registerAgenteDTO.setEmail(emailLowerCase);
        if (agenteRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email già registrata: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        AgenteImmobiliare agente = new AgenteImmobiliare(registerAgenteDTO.getIdAgenzia(), registerAgenteDTO.getNome(), registerAgenteDTO.getCognome(), registerAgenteDTO.getDataDiNascita(), registerAgenteDTO.getSesso(), registerAgenteDTO.getEmail(), registerAgenteDTO.getPassword());

        AgenteImmobiliare savedAgente = agenteRepository.save(agente);

        logger.debug("Ending updateAgente with agente: {}", savedAgente.getEmail());
        return new AgenteDTO(savedAgente.getId(), savedAgente.getIdAgenzia(), savedAgente.getNome(), savedAgente.getCognome(), savedAgente.getDataDiNascita(), savedAgente.getSesso(), savedAgente.getEmail());
    }

    @Transactional(readOnly = true)
    public AgenteDTO getAgenteDetails(String email){
        String emailLowerCase = email.toLowerCase();
        AgenteImmobiliare agente = agenteRepository.findByEmail(emailLowerCase).orElseThrow(()->new EntityNotFoundException("Agente not found with email: " + email));
        return new AgenteDTO(agente.getId(), agente.getIdAgenzia(), agente.getNome(), agente.getCognome(), agente.getDataDiNascita(), agente.getSesso(), agente.getEmail());
    }

    @Transactional(readOnly = true)
    public AgenteDTO getAgenteDetails(long idAgente){
        AgenteImmobiliare agente = agenteRepository.findById(idAgente).orElseThrow(()->new EntityNotFoundException("Agente not found with id: " + idAgente));
        return new AgenteDTO(agente.getId(), agente.getIdAgenzia(), agente.getNome(), agente.getCognome(), agente.getDataDiNascita(), agente.getSesso(), agente.getEmail());
    }
}
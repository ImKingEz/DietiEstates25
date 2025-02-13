package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25backend.api.dto.RegisterAmministratoreDTO;
import com.dietiestates25backend.business.entity.Amministratore;
import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.AgenziaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AmministratoreService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AmministratoreService.class);

    private final AmministratoreRepository amministratoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AgenziaRepository agenziaRepository;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AmministratoreService(AmministratoreRepository amministratoreRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AgenziaRepository agenziaRepository, HttpServletRequest httpServletRequest) {
        this.amministratoreRepository = amministratoreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.agenziaRepository = agenziaRepository;
        this.httpServletRequest = httpServletRequest;
    }

    @Transactional
    public AmministratoreDTO registraAmministratore(RegisterAmministratoreDTO registerDTO) {
        logger.debug("Starting registraAmministratore with email: {}", registerDTO.getEmail());
        if (amministratoreRepository.existsByEmail(registerDTO.getEmail())) {
            logger.error("Admin email already registered: {}", registerDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
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
    public String loginAmministratore(String email, String password, String csrfToken) throws BadCredentialsException, UsernameNotFoundException {
        logger.debug("Starting loginAmministratore with email: {}", email);
        Amministratore amministratore = amministratoreRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Amministratore amministratore = amministratoreRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new User(amministratore.getEmail(), amministratore.getPassword(), new ArrayList<>());
    }

    @Transactional(readOnly = true)
    public AmministratoreDTO getAmministratoreDetails(String email){
        Amministratore amministratore = amministratoreRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Admin not found with email: " + email));
        return new AmministratoreDTO(amministratore.getEmail(), amministratore.getIdAgenzia());
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return jwtService.isTokenValid(token, userDetails);
    }
}
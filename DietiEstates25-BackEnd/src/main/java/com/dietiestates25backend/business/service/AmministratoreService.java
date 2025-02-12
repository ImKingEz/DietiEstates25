package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AmministratoreDTO;
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

@Service
public class AmministratoreService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AmministratoreService.class);

    private final AmministratoreRepository amministratoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AgenziaRepository agenziaRepository;

    @Autowired
    public AmministratoreService(AmministratoreRepository amministratoreRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AgenziaRepository agenziaRepository) {
        this.amministratoreRepository = amministratoreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.agenziaRepository = agenziaRepository;
    }

    @Transactional
    public AmministratoreDTO registraAmministratore(Amministratore amministratore) {
        logger.debug("Starting registraAmministratore with email: {}", amministratore.getEmail());
        if (amministratoreRepository.existsByEmail(amministratore.getEmail())) {
            logger.error("Admin email already registered: {}", amministratore.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        // TODO: Implementare la logica per associare l'amministratore all'agenzia
        AgenziaImmobiliare agenzia = agenziaRepository.findByEmail(amministratore.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Agenzia non trovata per l'email: " + amministratore.getEmail()));

        amministratore.setIdAgenzia(agenzia.getId()); // Imposta l'ID dell'agenzia
        amministratore.setPassword(passwordEncoder.encode(amministratore.getPassword()));
        Amministratore savedAdmin = amministratoreRepository.save(amministratore);
        logger.debug("Admin saved: {}", savedAdmin.getId());
        return new AmministratoreDTO(savedAdmin.getEmail(), savedAdmin.getIdAgenzia());
    }

    @Transactional(readOnly = true)
    public String loginAmministratore(String email, String password) throws BadCredentialsException, UsernameNotFoundException {
        logger.debug("Starting loginAmministratore with email: {}", email);
        Amministratore amministratore = amministratoreRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        logger.debug("Admin retrieved from database: {}", amministratore.getEmail());

        if (!passwordEncoder.matches(password, amministratore.getPassword())) {
            logger.error("Login failed: Invalid password for admin: {}", email);
            throw new BadCredentialsException("Login Failed");
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

}
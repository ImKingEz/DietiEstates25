package com.dietiestates25backend.business.service;

import com.dietiestates25backend.business.entity.AgenteImmobiliare;
import com.dietiestates25backend.business.entity.Amministratore;
import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.data.repository.AgenteRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UtenteRepository utenteRepository;
    private final AgenteRepository agenteRepository;
    private final AmministratoreRepository amministratoreRepository;

    @Autowired
    public CustomUserDetailsService(UtenteRepository utenteRepository, AgenteRepository agenteRepository, AmministratoreRepository amministratoreRepository) {
        this.utenteRepository = utenteRepository;
        this.agenteRepository = agenteRepository;
        this.amministratoreRepository = amministratoreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("CustomUserDetailsService.loadUserByUsername: Attemting to find user: {}", username);

        Optional<Utente> utente = utenteRepository.findByEmail(username);
        if (utente.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found utente: {}", username);
            return new User(utente.get().getEmail(), utente.get().getPassword(), new ArrayList<>());
        }

        Optional<AgenteImmobiliare> agente = agenteRepository.findByEmail(username);
        if (agente.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found agente: {}", username);
            return new User(agente.get().getEmail(), agente.get().getPassword(), new ArrayList<>());
        }

        Optional<Amministratore> amministratore = amministratoreRepository.findByEmail(username);
        if (amministratore.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found amministratore: {}", username);
            return new User(amministratore.get().getEmail(), amministratore.get().getPassword(), new ArrayList<>());
        }

        logger.warn("CustomUserDetailsService.loadUserByUsername: User not found: {}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
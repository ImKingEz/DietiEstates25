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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

        List<GrantedAuthority> authorities = new ArrayList<>();

        Optional<Utente> utente = utenteRepository.findByEmail(username);
        if (utente.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found utente: {}", username);
            authorities.add(new SimpleGrantedAuthority("ROLE_UTENTE"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENTE"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENZIA"));
            return new User(utente.get().getEmail(), utente.get().getPassword(), authorities);
        }

        Optional<AgenteImmobiliare> agente = agenteRepository.findByEmail(username);
        if (agente.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found agente: {}", username);
            authorities.add(new SimpleGrantedAuthority("ROLE_AGENTE"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENZIA"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENTE"));
            return new User(agente.get().getEmail(), agente.get().getPassword(), authorities);
        }

        Optional<Amministratore> amministratore = amministratoreRepository.findByEmail(username);
        if (amministratore.isPresent()) {
            logger.debug("CustomUserDetailsService.loadUserByUsername: Found amministratore: {}", username);
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENZIA"));
            authorities.add(new SimpleGrantedAuthority("WRITE_AGENTE"));
            authorities.add(new SimpleGrantedAuthority("READ_AGENTE"));
            return new User(amministratore.get().getEmail(), amministratore.get().getPassword(), authorities);
        }

        logger.warn("CustomUserDetailsService.loadUserByUsername: User not found: {}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
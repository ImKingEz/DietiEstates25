package com.dietiestates25backend.data.config;

import com.dietiestates25backend.business.service.AmministratoreService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger loggerJwtAuthFilter = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final ApplicationContext applicationContext; // Inietta ApplicationContext

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
                                   ApplicationContext applicationContext) {
        this.jwtService = jwtService;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        loggerJwtAuthFilter.debug("Starting JWT authentication filter...");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            loggerJwtAuthFilter.debug("No or invalid authorization header");
            return;
        }

        jwt = authHeader.substring(7);
        loggerJwtAuthFilter.debug("Extracted jwt : {}", jwt);
        userEmail = jwtService.extractUsername(jwt);
        loggerJwtAuthFilter.debug("Extracted userEmail : {}", userEmail);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                // Prova a caricare l'utente dal servizio utenti
                UserDetailsService utenteUserDetailsService = applicationContext.getBean("authService", UserDetailsService.class);
                userDetails = utenteUserDetailsService.loadUserByUsername(userEmail);
                loggerJwtAuthFilter.debug("Retrieved user details (user) : {}", userDetails);
            } catch (UsernameNotFoundException e) {
                loggerJwtAuthFilter.debug("User not found in user repository, checking admin repository");
                try {
                    // Ottieni AmministratoreService dal ApplicationContext
                    AmministratoreService amministratoreService = applicationContext.getBean(AmministratoreService.class);
                    // Se non trovato, prova a caricare l'amministratore dal servizio amministratori
                    userDetails = amministratoreService.loadUserByUsername(userEmail);
                    loggerJwtAuthFilter.debug("Retrieved user details (admin): {}", userDetails);
                } catch (UsernameNotFoundException ex) {
                    loggerJwtAuthFilter.error("User not found in either user or admin repository: {}", userEmail);
                    // Non fare nulla, l'utente non è autorizzato
                }
            }

            if (userDetails != null && jwtService.isTokenValid(jwt, userDetails)) {
                loggerJwtAuthFilter.debug("Token is valid, creating authentication object");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                loggerJwtAuthFilter.debug("Authentication complete for user : {}", userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }
}
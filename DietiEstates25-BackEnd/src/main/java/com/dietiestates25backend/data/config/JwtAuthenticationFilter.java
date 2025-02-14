package com.dietiestates25backend.data.config;

import com.dietiestates25backend.business.service.AmministratoreService;
import com.dietiestates25backend.business.service.CustomUserDetailsService; // Importa CustomUserDetailsService
import com.dietiestates25backend.business.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CustomUserDetailsService userDetailsService; // Inietta CustomUserDetailsService

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
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
                userDetails = userDetailsService.loadUserByUsername(userEmail);
                loggerJwtAuthFilter.debug("Retrieved user details : {}", userDetails);
            } catch (UsernameNotFoundException e) {
                loggerJwtAuthFilter.error("User not found: {}", userEmail);
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
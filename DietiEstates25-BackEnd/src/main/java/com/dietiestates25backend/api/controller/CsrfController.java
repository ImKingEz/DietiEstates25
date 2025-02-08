package com.dietiestates25backend.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    private static final Logger logger = LoggerFactory.getLogger(CsrfController.class);

    @GetMapping("/api/csrf")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        logger.debug("CsrfController.getCsrfToken() called"); // Aggiungi questo log

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken == null) {
            logger.warn("CSRF token not found in request"); // Aggiungi questo log
        } else {
            logger.debug("CSRF token found: " + csrfToken.getToken()); // Aggiungi questo log
        }

        return csrfToken;
    }
}
package com.dietiestates25backend.data.config;

import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.business.service.JwtService;
import com.dietiestates25backend.data.repository.UtenteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    public static final String OAUTH_EMAIL_FIELD = "email";
    private static final String FRONTEND_REDIRECT_BASE_URL = "http://localhost:8000/oauth2/";
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final JwtService jwtService;
    private final UtenteRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public OAuth2SuccessHandler(JwtService jwtService, UtenteRepository userRepository, OAuth2AuthorizedClientService authorizedClientService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logger.debug("onAuthenticationSuccess() called");

        try {
            if (!(authentication.getPrincipal() instanceof OAuth2User principal)) {
                logger.warn("Authentication failed, principal is not OAuth2User");
                sendRedirect(response, "error?error=Authentication Failed");
                return;
            }

            Map<String, Object> attributes = principal.getAttributes();
            logger.debug("Attributes: {}", attributes);

            String email = extractEmail(attributes, authentication, response);
            if (email == null) return;

            Optional<Utente> existingUser = userRepository.findByEmail(email);
            boolean firstLogin = existingUser.isEmpty();

            Utente user = createUserAndReturn(email, attributes, existingUser);
            logger.debug("User: {}", user);
            logger.debug("First login: {}", firstLogin);

            String token = jwtService.generateToken(Map.of("firstLogin", firstLogin), user);
            String redirectUrl = (firstLogin ? "firstlogin" : "success") + "?token=" + token;

            logger.debug("Redirecting to: {}{}", FRONTEND_REDIRECT_BASE_URL, redirectUrl);
            sendRedirect(response, redirectUrl);

        } catch (Exception e) {
            logger.error("Errore durante il login oauth2: {}", e.getMessage());
            sendRedirect(response, "error?error=" + e.getMessage());
        }
    }

    private String extractEmail(Map<String, Object> attributes, Authentication authentication, HttpServletResponse response) throws IOException {
        String email = (String) attributes.get(OAUTH_EMAIL_FIELD);
        String provider = getProvider(authentication);

        if (email == null || email.isBlank() || provider.equals("github")) {
            try {
                email = retrieveEmailFromGithub(authentication);
                if (email == null || email.isBlank()) {
                    logger.error("Email non recuperabile con l'api di Github");
                    sendRedirect(response, "error?error=Errore durante l'autenticazione: Impossibile recuperare la mail");
                    return null;
                }
                logger.debug("Email recuperata con l'api di Github: {}", email);
            } catch (Exception e) {
                logger.error("Errore durante il recupero della mail di Github: {}", e.getMessage());
                sendRedirect(response, "error?error=Errore durante l'autenticazione: Impossibile recuperare la mail");
                return null;
            }
        }
        return email;
    }

    private String getProvider(Authentication authentication) {
        String provider = ((OAuth2User) authentication.getPrincipal()).getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("Nessuna autorità trovata per l'utente OAuth2"));
        if (provider.equals("OAUTH2_USER")) {
            provider = "github";
        }
        return provider;
    }

    private String retrieveEmailFromGithub(Authentication authentication) {
        RestTemplate restTemplate2 = new RestTemplate();
        String userInfoUrl = "https://api.github.com/user/emails";
        String accessTokenValue = getAccessToken(authentication);

        UriComponentsBuilder builder2 = UriComponentsBuilder.fromUriString(userInfoUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenValue);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response2 = restTemplate2.exchange(builder2.toUriString(), HttpMethod.GET, entity, JsonNode.class);
        JsonNode userInfoResponse = response2.getBody();

        if (userInfoResponse != null && userInfoResponse.isArray()) {
            for (JsonNode node : userInfoResponse) {
                if (node.has(OAUTH_EMAIL_FIELD) && node.get("primary").asBoolean()) {
                    return node.get(OAUTH_EMAIL_FIELD).asText();
                }
            }
        }
        return null;
    }

    private String getAccessToken(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            return null;
        }

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        return Optional.ofNullable(client)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue)
                .orElse(null);
    }

    private Utente createUserAndReturn(String email, Map<String, Object> attributes, Optional<Utente> existingUser) {
        if (existingUser.isPresent()) {
            logger.debug("Utente gia esistente, lo ritorno {}", existingUser.get());
            return existingUser.get();
        }

        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String userName = (String) attributes.get("login");

        String generatedPassword = passwordEncoder.encode(generateRandomPassword()); // Genera e codifica

        Utente user;
        if (givenName != null && familyName != null) {
            user = new Utente(givenName, familyName, email, generatedPassword);
        } else if (name != null && !name.isBlank()) {
            user = new Utente(name, name, email, generatedPassword);
        } else if (userName != null && !userName.isBlank()) {
            user = new Utente(userName, userName, email, generatedPassword);
        } else {
            user = new Utente("null", "null", email, generatedPassword);
        }

        user = userRepository.save(user);

        return user;
    }

    private String generateRandomPassword() {
        int length = 20;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    private void sendRedirect(HttpServletResponse response, String path) throws IOException {
        String redirectUrl = FRONTEND_REDIRECT_BASE_URL + path;
        logger.debug("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
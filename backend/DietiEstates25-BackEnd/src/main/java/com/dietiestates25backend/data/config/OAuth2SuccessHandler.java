package com.dietiestates25backend.data.config;

import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.business.service.JwtService;
import com.dietiestates25backend.data.repository.UtenteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletException;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UtenteRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("onAuthenticationSuccess() called");

        if (authentication.getPrincipal() instanceof OAuth2User principal) {
            logger.debug("Principal non è null");
            Map<String, Object> attributes = principal.getAttributes();
            logger.debug("Attributes: {}", attributes);
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String givenName = null;
            if (attributes.get("given_name") != null) {
                givenName = (String) attributes.get("given_name");
            }
            String familyName = null;
            if (attributes.get("family_name") != null) {
                familyName = (String) attributes.get("family_name");
            }

            String provider = ((OAuth2User) authentication.getPrincipal()).getAuthorities().stream().findFirst().get().getAuthority();
            if (provider.equals("OAUTH2_USER")) {
                provider = "github";
            }

            String userName = (String) attributes.get("login");

            if (email == null || email.isBlank() || provider.equals("github")) {
                try {
                    RestTemplate restTemplate2 = new RestTemplate();
                    String userInfoUrl = "https://api.github.com/user/emails";
                    String accessTokenValue = null;
                    if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                                oauthToken.getAuthorizedClientRegistrationId(),
                                authentication.getName()
                        );
                        if (client != null) {
                            OAuth2AccessToken accessToken = client.getAccessToken();
                            if(accessToken != null){
                                accessTokenValue = accessToken.getTokenValue();
                            }
                        }
                    }


                    UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(userInfoUrl);
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + accessTokenValue);
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<JsonNode> response2 = restTemplate2.exchange(builder2.toUriString(), HttpMethod.GET, entity, JsonNode.class);
                    JsonNode userInfoResponse = response2.getBody();
                    if (userInfoResponse.isArray()) {
                        for (JsonNode node : userInfoResponse) {
                            if (node.has("email") && node.get("primary").asBoolean()) {
                                email = node.get("email").asText();
                                break;
                            }
                        }
                    }
                    if (email == null || email.isBlank()) {
                        logger.error("Email non recuperabile con l'api di Github");
                        response.sendRedirect("http://localhost:8000/oauth2/error?error=Errore durante l'autenticazione: Impossibile recuperare la mail");
                        return;
                    }
                    logger.debug("Email recuperata con l'api di Github: {}", email);

                } catch (Exception e) {
                    logger.error("Errore durante il recupero della mail di Github: {}", e.getMessage());
                    response.sendRedirect("http://localhost:8000/oauth2/error?error=Errore durante l'autenticazione: Impossibile recuperare la mail");
                    return;
                }
            }

            try {
                Utente user;
                boolean firstLogin = false;
                if (givenName != null && familyName != null) {
                    user = userRepository.findByEmail(email).orElse(new Utente(givenName, familyName, email, "OAuth2User"));
                    if (user.getId() == null) {
                        firstLogin = true;
                    }
                } else {
                    if(name != null && !name.isBlank()){
                        user = userRepository.findByEmail(email).orElse(new Utente(name, name, email, "OAuth2User"));
                        if (user.getId() == null) {
                            firstLogin = true;
                        }
                    } else if (userName != null && !userName.isBlank()) {
                        user = userRepository.findByEmail(email).orElse(new Utente(userName, userName, email, "OAuth2User"));
                        if (user.getId() == null) {
                            firstLogin = true;
                        }
                    } else {
                        user = userRepository.findByEmail(email).orElse(new Utente("null", "null", email, "OAuth2User"));
                        if (user.getId() == null) {
                            firstLogin = true;
                        }
                    }
                }
                if (user.getId() == null) {
                    userRepository.save(user);
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("firstLogin", firstLogin);
                String token = jwtService.generateToken(claims,user);
                String redirectUrl;
                if(firstLogin){
                    redirectUrl = "http://localhost:8000/oauth2/firstlogin?token=" + token;
                }else{
                    redirectUrl = "http://localhost:8000/oauth2/success?token=" + token;
                }

                logger.debug("Redirecting to: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
            } catch (Exception e) {
                logger.error("Errore durante il login oauth2: {}", e.getMessage());
                response.sendRedirect("http://localhost:8000/oauth2/error?error=" + e.getMessage());
            }
        } else {
            logger.debug("Authentication failed, principal is not OAuth2User, redirecting to error page");
            response.sendRedirect("http://localhost:8000/oauth2/error?error=Authentication Failed");
        }
    }
}
package com.dietiestates25backend.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Controller
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    @GetMapping("/login/oauth2/code/github")
    public void githubLogin(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
        logger.debug("githubLogin() called");
    }

    @GetMapping("/login/oauth2/code/google")
    public void googleLogin(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
        logger.debug("googleLogin() called");
    }


}
package com.dietiestates25backend.data.config;

import com.dietiestates25backend.business.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dietiestates25backend.business.service.JwtService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService; // Injected here


    @Autowired
    public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler, JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.debug("Request received: {}", http);
        logger.info("Configuring security filters...");

        http.csrf(csrf -> {
            CookieCsrfTokenRepository tokenRepository = new CookieCsrfTokenRepository();
            tokenRepository.setCookieName("XSRF-TOKEN");
            csrf.csrfTokenRepository(tokenRepository);
            csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
        });

        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/users/register", "/api/users/login", "/api/csrf").permitAll()
                .requestMatchers("/api/admin/register", "/api/admin/login").permitAll()
                .requestMatchers("/api/agenzie/register").permitAll()
                .requestMatchers("/api/agenti/login").permitAll()
                .requestMatchers("/uploads/foto_immobili/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .anyRequest().authenticated()
        );

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login").permitAll()
                .successHandler(oAuth2SuccessHandler)
        );

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        logger.info("Security filters configured.");
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-CSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        logger.debug("CORS configuration: {}", configuration.getAllowedOrigins());
        logger.debug("CORS allowed methods: {}", configuration.getAllowedMethods());
        logger.debug("CORS allowed headers: {}", configuration.getAllowedHeaders());
        logger.debug("CORS allow credentials: {}", configuration.getAllowCredentials());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(){
        return new JwtAuthenticationFilter(jwtService, customUserDetailsService);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
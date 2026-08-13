package com.example.fincredex.config;

import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.enums.Role;
import com.example.fincredex.repository.UserRepository;
import com.example.fincredex.security.JwtService;
import com.example.fincredex.security.filter.JwtRequestFilter;
import com.example.fincredex.security.handler.AccessRestrictHandler;
import com.example.fincredex.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AccessRestrictHandler accessRestrictHandler;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth/login",
            "/auth/register"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/auth/refresh/token",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**"
    };

    // =========================================================
    // DEBUG FRONTEND URL
    // =========================================================

    @PostConstruct
    public void logFrontendUrl() {
        System.out.println(
                "CORS FRONTEND URL = " + frontendUrl
        );
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =========================================================
    // DAO AUTH PROVIDER
    // =========================================================

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserService userService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userService
        );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider
    ) throws Exception {

        http

                // IMPORTANT FOR FRONTEND REQUESTS
                .cors(cors -> {})

                // REST API + JWT
                .csrf(AbstractHttpConfigurer::disable)

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // Allow OPTIONS preflight request
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        // Public POST endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                PUBLIC_POST_ENDPOINTS
                        )
                        .permitAll()

                        // Public GET endpoints
                        .requestMatchers(
                                HttpMethod.GET,
                                PUBLIC_GET_ENDPOINTS
                        )
                        .permitAll()

                        // Google OAuth
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        )
                        .permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        // Everything else requires JWT
                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // SESSION
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // =================================================
                // ERROR HANDLING
                // =================================================

                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )

                        .accessDeniedHandler(
                                accessRestrictHandler
                        )
                )

                // =================================================
                // PROVIDER
                // =================================================

                .authenticationProvider(
                        provider
                )

                // =================================================
                // GOOGLE OAUTH
                // =================================================

                .oauth2Login(oauth2 -> oauth2

                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(
                                        oauth2UserService()
                                )
                        )

                        .successHandler(
                                (
                                        request,
                                        response,
                                        authentication
                                ) -> {

                                    OAuth2User oAuth2User =
                                            (OAuth2User)
                                                    authentication
                                                            .getPrincipal();

                                    String email =
                                            oAuth2User
                                                    .getAttribute(
                                                            "email"
                                                    );

                                    User user =
                                            userRepository
                                                    .findByEmail(
                                                            email
                                                    )
                                                    .orElseThrow(
                                                            () ->
                                                                    new RuntimeException(
                                                                            "User not found"
                                                                    )
                                                    );

                                    String jwt =
                                            jwtService
                                                    .generateAccessToken(
                                                            user
                                                    );

                                    String redirectUrl =
                                            frontendUrl
                                                    + "/oauth2/callback?token="
                                                    + jwt;

                                    response.sendRedirect(
                                            redirectUrl
                                    );
                                }
                        )
                )

                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =========================================================
    // GOOGLE USER SERVICE
    // =========================================================

    @Bean
    public OAuth2UserService<
            OAuth2UserRequest,
            OAuth2User
            > oauth2UserService() {

        return userRequest -> {

            OAuth2User oAuth2User =
                    new DefaultOAuth2UserService()
                            .loadUser(
                                    userRequest
                            );

            String email =
                    oAuth2User.getAttribute(
                            "email"
                    );

            String name =
                    oAuth2User.getAttribute(
                            "name"
                    );

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElseGet(() -> {

                                User newUser =
                                        new User();

                                newUser.setEmail(
                                        email
                                );

                                newUser.setUsername(
                                        name
                                );

                                newUser.setPassword(
                                        null
                                );

                                newUser.setRole(
                                        Role.USER
                                );

                                return userRepository
                                        .save(
                                                newUser
                                        );
                            });

            return new DefaultOAuth2User(

                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_"
                                            + user
                                            .getRole()
                                            .name()
                            )
                    ),

                    oAuth2User.getAttributes(),

                    "email"
            );
        };
    }

    // =========================================================
    // CORS
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // =====================================================
        // ALLOWED FRONTENDS
        // =====================================================

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        frontendUrl
                )
        );

        // =====================================================
        // HTTP METHODS
        // =====================================================

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        // =====================================================
        // REQUEST HEADERS
        // =====================================================

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        // =====================================================
        // RESPONSE HEADERS
        // =====================================================

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        // Required if credentials/cookies are ever used
        configuration.setAllowCredentials(
                true
        );

        // Optional cache for preflight response
        configuration.setMaxAge(
                3600L
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
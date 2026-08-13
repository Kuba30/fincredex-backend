package com.example.fincredex.security.filter;


import com.example.fincredex.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);


        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {

            Optional<Claims> claimsOpt = jwtService.parseToken(token);

            if (claimsOpt.isEmpty()) {
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            Claims claims = claimsOpt.get();
            String userId = claims.getSubject();


            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ Multiple roles support
                List<SimpleGrantedAuthority> authorities =
                        Optional.ofNullable(claims.get("roles", List.class))
                                .orElse(List.of())
                                .stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, token, authorities);


                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (ExpiredJwtException e) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Token has expired");
            return;
        } catch (JwtException e) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception e) {
            log.error("Unexpected error during JWT processing", e);
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response,
                           HttpStatus status,
                           String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

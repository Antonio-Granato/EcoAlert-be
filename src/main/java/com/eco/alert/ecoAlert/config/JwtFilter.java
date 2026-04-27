package com.eco.alert.ecoAlert.config;

import com.eco.alert.ecoAlert.service.JwtService;
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

/* Ho implementato un filtro JWT che valida il token, estrae userId e ruolo e popola il SecurityContext
* con le authorities. Ho anche gestito i casi di autenticazione già presente e migliorato la robustezza
* del parsing. */

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("PATH: {}", path);

        if (path.startsWith("/api/login") || path.startsWith("/api/sign-in")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization"); // legge header

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);         // estrae token

        try{
            if (SecurityContextHolder.getContext().getAuthentication() == null) {   // controllo se già autenticato

                Integer userId = jwtService.extractUserId(token);   // usa JwtService
                String role = jwtService.extractRole(token);        // usa JwtService

                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)  // crea ruoli
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);    // crea Authentication

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);   // salva nel contesto
                log.info("JWT VALIDATA -> userId={}, role={}", userId, role);
            }
        } catch (Exception e) {
            log.warn("JWT non valido per request {} {} - errore: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage()
            );

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token non valido");
            return;
        }

        filterChain.doFilter(request, response);
    }

}

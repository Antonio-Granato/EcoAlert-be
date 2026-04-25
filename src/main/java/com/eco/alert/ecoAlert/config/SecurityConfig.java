package com.eco.alert.ecoAlert.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*  Ho configurato Spring Security in modalità stateless per supportare JWT,
    disabilitando CSRF e aggiungendo un filtro custom prima dello UsernamePasswordAuthenticationFilter.
    Ho anche abilitato method security per gestire i ruoli tramite @PreAuthorize. */

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // niente CSRF (API REST)
                .csrf(csrf -> csrf.disable())

                // JWT = stateless niente sessioni
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //autorizzazioni
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/sign-in").permitAll()
                        .anyRequest().authenticated()
                )

                // gestione 401 custom
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non autenticato");
                        })
                )

                // filtro JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

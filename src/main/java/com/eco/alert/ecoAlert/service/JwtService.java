package com.eco.alert.ecoAlert.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/* “Ho refattorizzato il JwtService rendendo coerente l’uso della nuova API JJWT,
centralizzando il parsing dei claims e utilizzando una Key sicura per la firma del token.” */

@Service
public class JwtService {

    private static final String SECRET = "ecoalertsupersecretkeyecoalertsupersecretkey";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // GENERAZIONE TOKEN
    public String generateToken(Integer userId, String ruolo) {
        return Jwts.builder()   // crea JWT
                .claim("userId", userId)    // payload
                .claim("role", ruolo)       // payload
                .setIssuedAt(new Date())          // quando è stato creato
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) //scade dopo 24h
                .signWith(key)     // crea la signitura usa la tua chiave
                .compact();        // genera stringa finale
    }

    // PARSING PER CLAIMS
    /* verifica firma
    *  verifica scadenza
    * decodifica payload */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // CLAIMS legge dal payload
    public Integer extractUserId(String token) {
        return extractAllClaims(token).get("userId", Integer.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
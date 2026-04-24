package com.eco.alert.ecoAlert.config;

import com.eco.alert.ecoAlert.exception.AccessoNonAutorizzatoException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Integer userId)) {
            throw new AccessoNonAutorizzatoException("Utente non autenticato");
        }

        return userId;
    }
}

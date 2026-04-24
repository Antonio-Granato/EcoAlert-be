package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.config.SecurityUtils;
import com.eco.alert.ecoAlert.service.UserService;
import com.ecoalert.api.UtentiApi;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class UtenteController implements UtentiApi {

    private final UserService utenteService;
    private final SecurityUtils securityUtils;

    public UtenteController(UserService userService, SecurityUtils securityUtils) {
        this.utenteService = userService;
        this.securityUtils = securityUtils;
    }

    @Override
    public ResponseEntity<UtenteDettaglioOutput> getMe() {
        Integer idUtente = securityUtils.getCurrentUserId();

        log.info("GET /utente/me - userId={}", idUtente);

        return ResponseEntity.ok(
                utenteService.getUserById(idUtente)
        );
    }

    @Override
    public ResponseEntity<Void> deleteUser() {
        Integer idUtente = securityUtils.getCurrentUserId();

        log.info("DELETE /utente/me - userId={}", idUtente);

        utenteService.deleteUser(idUtente);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UtenteDettaglioOutput> updateUser(
            UtenteUpdateInput input
    ) {
        Integer idUtente = securityUtils.getCurrentUserId();

        log.info("PUT /utente/me - userId={}", idUtente);

        return ResponseEntity.ok(
                utenteService.updateUser(idUtente, input)
        );
    }
}
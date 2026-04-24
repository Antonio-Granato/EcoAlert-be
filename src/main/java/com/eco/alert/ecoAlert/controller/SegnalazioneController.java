package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.config.SecurityUtils;
import com.eco.alert.ecoAlert.service.SegnalazioneService;
import com.ecoalert.api.SegnalazioniApi;
import com.ecoalert.model.SegnalazioneInput;
import com.ecoalert.model.SegnalazioneOutput;
import com.ecoalert.model.SegnalazioneUpdateInputEnte;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
public class SegnalazioneController implements SegnalazioniApi {

    private final SegnalazioneService segnalazioneService;
    private final SecurityUtils securityUtils;

    public SegnalazioneController(SegnalazioneService segnalazioneService, SecurityUtils securityUtils){
        this.segnalazioneService = segnalazioneService;
        this.securityUtils = securityUtils;
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> createSegnalazione(
            SegnalazioneInput segnalazioneInput
    ) {
        log.info("Creazione segnalazione");
        return ResponseEntity.status(HttpStatus.CREATED).body(segnalazioneService.creaSegnalazione(segnalazioneInput));
    }

    @Override
    public ResponseEntity<List<SegnalazioneOutput>> getMySegnalazioni() {
        Integer idUtente = securityUtils.getCurrentUserId();

        log.info("GET /utente/me/segnalazioni - userId={}", idUtente);

        return ResponseEntity.ok(
                segnalazioneService.getSegnalazioniByUserId(idUtente)
        );
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> getSegnalazioneById(Integer idSegnalazione) {
        Integer idUtente = securityUtils.getCurrentUserId();

        log.info("GET /segnalazioni/{} - userId={}", idSegnalazione, idUtente);

        return ResponseEntity.ok(
                segnalazioneService.getSegnalazioneById(idSegnalazione)
        );
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> updateSegnalazioneEnte(Integer idSegnalazione, Integer idEnte, SegnalazioneUpdateInputEnte input) {
        log.info("Aggiornamento Segnalazione - idSegnalazione={}, idEnte={}", idSegnalazione, idEnte);
        return ResponseEntity.ok(
                segnalazioneService.aggiornaStatoSegnalazione(idEnte, idSegnalazione, input)
        );
    }

    @Override
    public ResponseEntity<Void> deleteSegnalazione(Integer idSegnalazione){
        segnalazioneService.cancellaSegnalazione(idSegnalazione);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> updateSegnalazione(Integer idSegnalazione, SegnalazioneInput segnalazioneInput) {
        return ResponseEntity.ok(segnalazioneService.modificaSegnalazione(idSegnalazione, segnalazioneInput));
    }
}
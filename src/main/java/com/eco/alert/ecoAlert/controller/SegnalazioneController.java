package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.service.SegnalazioneService;
import com.ecoalert.api.SegnalazioniApi;
import com.ecoalert.model.SegnalazioneInput;
import com.ecoalert.model.SegnalazioneOutput;
import com.ecoalert.model.SegnalazioneUpdateInputEnte;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
public class SegnalazioneController implements SegnalazioniApi {

    @Autowired
    private SegnalazioneService segnalazioneService;

    @Override
    public ResponseEntity<SegnalazioneOutput> createSegnalazione(
            Integer id,
            SegnalazioneInput segnalazioneInput
    ) {
        log.info("POST /user/{}/segnalazione", id);
        return ResponseEntity.status(201).body(segnalazioneService.creaSegnalazione(id, segnalazioneInput));
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> updateSegnalazioneEnte(Integer idSegnalazione, Integer idEnte, SegnalazioneUpdateInputEnte input) {
        log.info("Aggiornamento Segnalazione - idSegnalazione={}, idEnte={}", idSegnalazione, idEnte);
        SegnalazioneOutput output = segnalazioneService.aggiornaStatoSegnalazione(idSegnalazione,idEnte,input);
        return ResponseEntity.ok(output);
    }

    @Override
    public ResponseEntity<Void> deleteSegnalazione(Integer id, Integer idSegnalazione){
        segnalazioneService.cancellaSegnalazione(id, idSegnalazione);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<SegnalazioneOutput> updateSegnalazione(
            Integer id, Integer idSegnalazione, SegnalazioneInput input) {

        SegnalazioneOutput out = segnalazioneService.modificaSegnalazione(id, idSegnalazione, input);
        return ResponseEntity.ok(out);
    }
}
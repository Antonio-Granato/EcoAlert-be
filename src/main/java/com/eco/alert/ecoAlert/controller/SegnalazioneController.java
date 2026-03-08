package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.service.SegnalazioneService;
import com.ecoalert.api.SegnalazioniApi;
import com.ecoalert.model.SegnalazioneInput;
import com.ecoalert.model.SegnalazioneOutput;
import com.ecoalert.model.SegnalazioneUpdateInputEnte;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<SegnalazioneOutput> updateSegnalazioneEnte(
            Integer idSegnalazione,
            Integer idEnte,
            SegnalazioneUpdateInputEnte segnalazioneUpdateInputEnte
    ) {
        System.out.println("Controller → idSegnalazione: " + idSegnalazione);
        System.out.println("Controller → idEnte: " + idEnte);
        System.out.println("Controller → stato richiesto: " + segnalazioneUpdateInputEnte.getStato());

        // Converti enum OpenAPI → enum interno
        StatoSegnalazione nuovoStato =
                StatoSegnalazione.valueOf(segnalazioneUpdateInputEnte.getStato().name());

        //ottieni la ditta
        String nuovaDitta = segnalazioneUpdateInputEnte.getDitta();

        SegnalazioneOutput updated =
                segnalazioneService.aggiornaStatoSegnalazione(
                        idEnte,
                        idSegnalazione,
                        nuovoStato,
                        nuovaDitta
                );

        return ResponseEntity.ok(updated);
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
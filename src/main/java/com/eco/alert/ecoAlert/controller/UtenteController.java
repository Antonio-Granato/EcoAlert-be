package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.entity.SegnalazioneEntity;
import com.eco.alert.ecoAlert.service.SegnalazioneService;
import com.eco.alert.ecoAlert.service.UserService;
import com.ecoalert.api.UtentiApi;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Log4j2
public class UtenteController implements UtentiApi {

    @Autowired
    private UserService utenteService;

    @Autowired
    private SegnalazioneService segnalazioneService;

    @Override
    public ResponseEntity<UtenteDettaglioOutput> getUserById(Integer id) {
        log.info("Richiesta dettaglio utente con ID {}", id);
        UtenteDettaglioOutput utente = utenteService.getUserById(id);
        return ResponseEntity.ok(utente);
    }

    @Override
    public ResponseEntity<List<SegnalazioneOutput>> getSegnalazioniByUserId(Integer id) {
        log.info("Richiesta lista segnalazioni dell'utente con ID {}", id);
        return ResponseEntity.ok(segnalazioneService.getSegnalazioniByUserId(id));
    }


    @Override
    public ResponseEntity<SegnalazioneOutput> getSegnalazioneById(Integer id, Integer idSegnalazione) {
        log.info("Richiesta dettaglio segnalazione {} per utente {}", idSegnalazione, id);

        SegnalazioneOutput segnalazione = segnalazioneService.getSegnalazioneById(id, idSegnalazione);

        return ResponseEntity.ok(segnalazione);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Integer id){
        utenteService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UtenteDettaglioOutput> updateUser(
            Integer id,
            UtenteUpdateInput utenteUpdateInput
    ) {

        UtenteDettaglioOutput updated =
                utenteService.updateUser(id, utenteUpdateInput);

        return ResponseEntity.ok(updated);
    }
}

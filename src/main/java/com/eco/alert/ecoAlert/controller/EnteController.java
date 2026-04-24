package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.service.UserService;
import com.ecoalert.api.EntiApi;
import com.ecoalert.model.EnteOutput;
import com.ecoalert.model.SegnalazioneOutput;
import com.ecoalert.model.SegnalazioniStatisticheOutput;
import com.ecoalert.model.StatoEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EnteController implements EntiApi {

    private final UserService userService;

    public EnteController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<List<EnteOutput>> getAllEnti() {
        return ResponseEntity.ok(userService.getAllEnti());
    }

    @Override
    public ResponseEntity<List<SegnalazioneOutput>> getSegnalazioniByEnteAndStato(
            Integer idEnte,
            StatoEnum stato
    ) {
        return ResponseEntity.ok(userService.getSegnalazioniByEnteAndStato(idEnte, stato));
    }

    @Override
    public ResponseEntity<SegnalazioniStatisticheOutput> getSegnalazioniStatsByEnte(Integer idEnte) {
        return ResponseEntity.ok(userService.getSegnalazioniStatistiche(idEnte));
    }
}

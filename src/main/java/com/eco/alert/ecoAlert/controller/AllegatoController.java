package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.service.AllegatoService;
import com.ecoalert.api.AllegatiApi;
import com.ecoalert.model.AllegatoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RestController
public class AllegatoController implements AllegatiApi {

    private final AllegatoService allegatoService;

    public AllegatoController(AllegatoService allegatoService) {
        this.allegatoService = allegatoService;
    }

    @Override
    public ResponseEntity<AllegatoOutput> uploadAllegato(
            Integer idSegnalazione,
            MultipartFile file
    ) {

        log.info("Upload allegato - segnalazione={}", idSegnalazione);

        AllegatoOutput output = allegatoService.caricaAllegato(idSegnalazione, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @Override
    public ResponseEntity<Resource> downloadAllegato(
            Integer idAllegato
    ) {
        log.info("GET /allegato/{}/download", idAllegato);
        return allegatoService.downloadAllegato(idAllegato);
    }

    @Override
    public ResponseEntity<Void> deleteAllegato(
            Integer idAllegato
    ) {
        log.info("DELETE allegato {}", idAllegato);

        allegatoService.eliminaAllegato(idAllegato);
        return ResponseEntity.noContent().build();
    }
}

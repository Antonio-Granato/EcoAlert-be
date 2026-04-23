package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.entity.AllegatoEntity;
import com.eco.alert.ecoAlert.service.AllegatoService;
import com.ecoalert.api.AllegatiApi;
import com.ecoalert.model.AllegatoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Log4j2
@RestController
public class AllegatoController implements AllegatiApi {

    @Autowired
    private AllegatoService allegatoService;

    @PostMapping(path = "/segnalazione/{idSegnalazione}/allegato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AllegatoOutput> uploadAllegato(
            @PathVariable Integer idSegnalazione,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("POST /segnalazione/{}/allegato", idSegnalazione);
        return ResponseEntity.status(HttpStatus.CREATED).body(allegatoService.caricaAllegato(idSegnalazione, file));
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
        allegatoService.eliminaAllegato(idAllegato);
        return ResponseEntity.noContent().build();
    }
}

package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.service.CommentoService;
import com.ecoalert.api.CommentiApi;
import com.ecoalert.model.CommentoInput;
import com.ecoalert.model.CommentoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class CommentoController implements CommentiApi {

    private final CommentoService commentoService;

    public CommentoController(CommentoService commentoService) {
        this.commentoService = commentoService;
    }

    @Override
    public ResponseEntity<CommentoOutput> createCommento (
            Integer idSegnalazione,
            CommentoInput commentoInput
    ) {
        return ResponseEntity.ok(commentoService.creaCommento(idSegnalazione, commentoInput));
    }

    @Override
    public ResponseEntity<Void> deleteCommento (
            Integer idSegnalazione,
            Integer idCommento
    ) {
        commentoService.cancellaCommento(idSegnalazione, idCommento);
        return ResponseEntity.noContent().build();
    }
}

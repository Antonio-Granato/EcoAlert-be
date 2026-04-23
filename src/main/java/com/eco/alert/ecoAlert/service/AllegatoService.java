package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.AllegatoDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.entity.AllegatoEntity;
import com.eco.alert.ecoAlert.entity.SegnalazioneEntity;
import com.eco.alert.ecoAlert.exception.AllegatoNonTrovatoException;
import com.eco.alert.ecoAlert.exception.OperazioneNonPermessaException;
import com.eco.alert.ecoAlert.exception.SegnalazioneNonTrovataException;
import com.ecoalert.model.AllegatoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;

@Service
@Log4j2
public class AllegatoService {

    private final AllegatoDao allegatoDao;
    private final SegnalazioneDao segnalazioneDao;

    public AllegatoService(AllegatoDao allegatoDao, SegnalazioneDao segnalazioneDao) {
        this.allegatoDao = allegatoDao;
        this.segnalazioneDao = segnalazioneDao;
    }

    @Transactional
    public AllegatoOutput caricaAllegato(Integer idSegnalazione, MultipartFile file) {

        log.info("Upload allegato per segnalazione {}", idSegnalazione);

        if (file == null || file.isEmpty()) {
            throw new OperazioneNonPermessaException("File vuoto.");
        }

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() ->
                        new SegnalazioneNonTrovataException("Segnalazione non trovata.")
                );

        try {
            AllegatoEntity allegato = new AllegatoEntity();
            allegato.setNomeFile(file.getOriginalFilename());
            allegato.setContentType(file.getContentType());
            allegato.setFileData(file.getBytes());
            allegato.setDataAllegato(java.time.LocalDateTime.now());
            allegato.setSegnalazione(segnalazione);

            AllegatoEntity salvato = allegatoDao.save(allegato);

            log.info("Allegato {} caricato per segnalazione {}",
                    salvato.getId_allegato(), idSegnalazione);

            return toOutput(salvato);

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento del file", e);
        }
    }

    public ResponseEntity<Resource> downloadAllegato(Integer idAllegato) {

        log.info("Download allegato {}", idAllegato);

        AllegatoEntity allegato = allegatoDao.findById(idAllegato)
                .orElseThrow(() ->
                        new AllegatoNonTrovatoException("Allegato non trovato")
                );

        ByteArrayResource resource =
                new ByteArrayResource(allegato.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + allegato.getNomeFile() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, allegato.getContentType())
                .body(resource);
    }

    @Transactional
    public void eliminaAllegato(Integer idAllegato) {

        log.info("Eliminazione allegato {}", idAllegato);

        AllegatoEntity allegato = allegatoDao.findById(idAllegato)
                .orElseThrow(() ->
                        new AllegatoNonTrovatoException("Allegato non trovato")
                );

        allegatoDao.delete(allegato);
        log.info("Allegato {} eliminato", idAllegato);
    }

    // MAPPER
    private AllegatoOutput toOutput(AllegatoEntity entity) {

        AllegatoOutput output = new AllegatoOutput();
        output.setId(entity.getId_allegato());
        output.setNomeFile(entity.getNomeFile());
        output.setContentType(entity.getContentType());
        output.setIdSegnalazione(entity.getSegnalazione().getIdSegnalazione());

        if (entity.getDataAllegato() != null) {
            output.setDataCaricamento(
                    entity.getDataAllegato()
                            .atZone(ZoneId.systemDefault())
                            .toOffsetDateTime()
            );
        }

        return output;
    }
}

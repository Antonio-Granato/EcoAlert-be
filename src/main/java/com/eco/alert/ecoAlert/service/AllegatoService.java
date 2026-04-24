package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.config.SecurityUtils;
import com.eco.alert.ecoAlert.dao.AllegatoDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.entity.AllegatoEntity;
import com.eco.alert.ecoAlert.entity.SegnalazioneEntity;
import com.eco.alert.ecoAlert.exception.AllegatoNonTrovatoException;
import com.eco.alert.ecoAlert.exception.IdODatiMancantiException;
import com.eco.alert.ecoAlert.exception.OperazioneNonPermessaException;
import com.eco.alert.ecoAlert.exception.SegnalazioneNonTrovataException;
import com.ecoalert.model.AllegatoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SecurityUtils securityUtils;

    public AllegatoService(AllegatoDao allegatoDao, SegnalazioneDao segnalazioneDao, SecurityUtils securityUtils) {
        this.allegatoDao = allegatoDao;
        this.segnalazioneDao = segnalazioneDao;
        this.securityUtils = securityUtils;
    }

    @PreAuthorize("hasRole('CITTADINO')")
    @Transactional
    public AllegatoOutput caricaAllegato(Integer idSegnalazione, MultipartFile file) {

        log.info("Upload allegato per segnalazione {}", idSegnalazione);

        if (file == null || file.isEmpty()) {
            throw new OperazioneNonPermessaException("File vuoto.");
        }

        Integer userId = securityUtils.getCurrentUserId();

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() ->
                        new SegnalazioneNonTrovataException("Segnalazione non trovata.")
                );

        if (!segnalazione.getCittadino().getId().equals(userId) ) {
            throw new OperazioneNonPermessaException("Non autorizzato");
        }

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

    @PreAuthorize("hasAnyRole('CITTADINO','ENTE')")
    public ResponseEntity<Resource> downloadAllegato(Integer idAllegato) {

        if (idAllegato == null) {
            throw new IdODatiMancantiException("ID allegato mancante");
        }

        Integer userId = securityUtils.getCurrentUserId();

        log.info("Download allegato {} da utente {}", idAllegato, userId);

        AllegatoEntity allegato = allegatoDao.findById(idAllegato)
                .orElseThrow(() ->
                        new AllegatoNonTrovatoException("Allegato non trovato")
                );

        SegnalazioneEntity segnalazioneEntity = allegato.getSegnalazione();

        if (segnalazioneEntity == null) {
            throw new IdODatiMancantiException("ID segnalazione mancante");
        }

        // Controllo autorizzazione
        if (!segnalazioneEntity.getCittadino().getId().equals(userId)
                && !segnalazioneEntity.getEnte().getId().equals(userId)) {
            throw new OperazioneNonPermessaException("Non sei autorizzato");
        }

        ByteArrayResource resource =
                new ByteArrayResource(allegato.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + allegato.getNomeFile() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, allegato.getContentType())
                .body(resource);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void eliminaAllegato(Integer idAllegato) {

        Integer userId = securityUtils.getCurrentUserId();

        log.info("Eliminazione allegato {} da utente {}", idAllegato, userId);

        AllegatoEntity allegatoEntity = allegatoDao.findById(idAllegato)
                .orElseThrow(() ->
                        new AllegatoNonTrovatoException("Allegato non trovato")
                );

        SegnalazioneEntity segnalazioneEntity = allegatoEntity.getSegnalazione();

        if (segnalazioneEntity == null) {
            throw new IdODatiMancantiException("ID segnalazione mancante");
        }

        if (!segnalazioneEntity.getCittadino().getId().equals(userId)) {
            throw  new OperazioneNonPermessaException("Non autorizzato");
        }

        allegatoDao.delete(allegatoEntity);

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

package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.config.SecurityUtils;
import com.eco.alert.ecoAlert.dao.CommentoDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.entity.*;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.CommentoInput;
import com.ecoalert.model.CommentoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@Service
public class CommentoService {

    private final CommentoDao commentoDao;
    private final UtenteDao utenteDao;
    private final SegnalazioneDao segnalazioneDao;
    private final SecurityUtils securityUtils;

    public CommentoService(CommentoDao commentoDao, UtenteDao utenteDao, SegnalazioneDao segnalazioneDao, SecurityUtils securityUtils) {
        this.commentoDao = commentoDao;
        this.utenteDao = utenteDao;
        this.segnalazioneDao = segnalazioneDao;
        this.securityUtils = securityUtils;
    }

    // HELPER
    private void checkAutorizzazione(UtenteEntity utente, SegnalazioneEntity segnalazione) {

        if (utente instanceof CittadinoEntity cittadino) {
            if (!segnalazione.getCittadino().getId().equals(cittadino.getId())) {
                throw new OperazioneNonPermessaException("Il cittadino non può commentare questa segnalazione");
            }
        } else if (utente instanceof EnteEntity ente) {
            if (!segnalazione.getEnte().getId().equals(ente.getId())) {
                throw new OperazioneNonPermessaException("L'ente non può commentare questa segnalazione");
            }
        } else {
            throw new OperazioneNonPermessaException("Tipo utente non autorizzato");
        }
    }

    // MAPPING
    private CommentoOutput toOutput(CommentoEntity entity) {
        CommentoOutput output = new CommentoOutput();

        output.setId(entity.getIdCommento());
        output.setDescrizione(entity.getDescrizione());
        output.setIdUtente(entity.getUtente().getId());
        output.setDataCommento(entity.getDataCommento().atZone(ZoneId.systemDefault()).toOffsetDateTime());

        if (entity.getUtente() instanceof CittadinoEntity cittadino) {
            output.setNome(cittadino.getNome());
            output.setCognome(cittadino.getCognome());
        }

        if (entity.getUtente() instanceof EnteEntity ente) {
            output.setNomeEnte(ente.getNomeEnte());
        }

        return output;
    }

    // BUSINESS
    @PreAuthorize("hasAnyRole('CITTADINO','ENTE')")
    @Transactional
    public CommentoOutput creaCommento(Integer idSegnalazione, CommentoInput input) {

        Integer idUtente = securityUtils.getCurrentUserId();

        if (idSegnalazione == null) {
            throw new IdODatiMancantiException("ID segnalazione mancante.");
        }

        if (input == null || !StringUtils.hasText(input.getDescrizione())) {
            throw new IdODatiMancantiException("Descrizione commento obbligatoria.");
        }

        log.info("Creazione commento - utente={}, segnalazione={}", idUtente, idSegnalazione);

        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato"));

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata"));

        checkAutorizzazione(utente, segnalazione);

        CommentoEntity commento = new CommentoEntity();
        commento.setDescrizione(input.getDescrizione());
        commento.setUtente(utente);
        commento.setSegnalazione(segnalazione);
        commento.setDataCommento(LocalDateTime.now());

        return toOutput(commentoDao.save(commento));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void cancellaCommento(Integer idSegnalazione, Integer idCommento) {

        Integer idUtente = securityUtils.getCurrentUserId();

        if (idSegnalazione == null || idCommento == null) {
            throw new IdODatiMancantiException("ID mancanti");
        }

        log.info("Eliminazione commento - utente={}, segnalazione={}, commento={}",
                idUtente, idSegnalazione, idCommento);

        CommentoEntity commento = commentoDao.findById(idCommento)
                .orElseThrow(() -> new CommentoNonTrovatoException("Commento non trovato"));

        if (!commento.getUtente().getId().equals(idUtente)) {
            throw new OperazioneNonPermessaException("Non puoi eliminare questo commento");
        }

        if (!commento.getSegnalazione().getIdSegnalazione().equals(idSegnalazione)) {
            throw new OperazioneNonPermessaException("Commento non associato alla segnalazione indicata");
        }

        commentoDao.delete(commento);
    }
}
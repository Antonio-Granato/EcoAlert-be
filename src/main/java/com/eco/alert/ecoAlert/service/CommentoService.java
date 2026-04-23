package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.CommentoDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.entity.*;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.CommentoInput;
import com.ecoalert.model.CommentoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@Service
public class CommentoService {

    private final CommentoDao commentoDao;
    private final UtenteDao utenteDao;
    private final SegnalazioneDao segnalazioneDao;

    public CommentoService(CommentoDao commentoDao, UtenteDao utenteDao, SegnalazioneDao segnalazioneDao) {
        this.commentoDao = commentoDao;
        this.utenteDao = utenteDao;
        this.segnalazioneDao = segnalazioneDao;
    }

    // HELPER
    private void checkAutorizzazione(UtenteEntity utente, SegnalazioneEntity segnalazione) {

        if (utente instanceof CittadinoEntity) {
            if (!segnalazione.getCittadino().getId().equals(utente.getId())) {
                throw new OperazioneNonPermessaException("Il cittadino non può commentare questa segnalazione");
            }
        } else if (utente instanceof EnteEntity) {
            if (!segnalazione.getEnte().getId().equals(utente.getId())) {
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

    // API
    @Transactional
    public CommentoOutput creaCommentoResponse(
            Integer idUtente,
            Integer idSegnalazione,
            CommentoInput commentoInput
    ) {
        CommentoEntity entity = creaCommento(idUtente, idSegnalazione, commentoInput);

        return toOutput(entity);
    }

    // BUSINESS
    @Transactional
    public CommentoEntity creaCommento(Integer idUtente, Integer idSegnalazione, CommentoInput commentoInput) {

        if (idUtente == null || idSegnalazione == null) {
            throw new IdODatiMancantiException("ID mancanti.");
        }

        log.info("Creazione commento - utente={}, segnalazione={}", idUtente, idSegnalazione);

        if (commentoInput == null || commentoInput.getDescrizione() == null || commentoInput.getDescrizione().isBlank()) {
            throw new IdODatiMancantiException("Descrizione commento obbligatoria.");
        }

        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato con ID: " + idUtente));

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata con ID: " + idSegnalazione));

        checkAutorizzazione(utente, segnalazione);

        CommentoEntity commento = new CommentoEntity();
        commento.setDescrizione(commentoInput.getDescrizione());
        commento.setUtente(utente);
        commento.setSegnalazione(segnalazione);
        commento.setDataCommento(LocalDateTime.now());

        return commentoDao.save(commento);
    }

    public void cancellaCommento(Integer idUtente, Integer idSegnalazione, Integer idCommento) {

        log.info("Eliminazione commento - utente={}, commento={}", idUtente, idCommento);

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

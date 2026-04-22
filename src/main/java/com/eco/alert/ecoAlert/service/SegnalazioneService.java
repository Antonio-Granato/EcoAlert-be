package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.EnteDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.entity.*;
import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class SegnalazioneService {

    private final SegnalazioneDao segnalazioneDao;
    private final EnteDao enteDao;
    private final UtenteDao utenteDao;

    public SegnalazioneService(
            SegnalazioneDao segnalazioneDao,
            EnteDao enteDao,
            UtenteDao utenteDao) {
        this.segnalazioneDao = segnalazioneDao;
        this.enteDao = enteDao;
        this.utenteDao = utenteDao;
    }

    // =========================
    // METODI PRIVATI (HELPER)
    // =========================

    private SegnalazioneEntity getSegnalazione(Integer id) {
        return segnalazioneDao.findById(id)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata"));
    }

    private EnteEntity getEnte(Integer id) {
        return enteDao.findById(id)
                .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato"));
    }

    private void checkEnteAutorizzato(SegnalazioneEntity segnalazione, EnteEntity ente) {
        if (!segnalazione.getEnte().getId().equals(ente.getId())) {
            throw new EnteNonAutorizzatoException("Questo ente non può modificare la segnalazione");
        }
    }

    private StatoSegnalazione convertiStato(StatoEnum statoInput) {
        try {
            return StatoSegnalazione.valueOf(statoInput.name());
        } catch (IllegalArgumentException e) {
            throw new StatoNonValidoException("Stato non valido: " + statoInput);
        }
    }

    @Transactional
    public SegnalazioneOutput creaSegnalazione(Integer idUtente, SegnalazioneInput input) {
        log.info("Creazione segnalazione - userId={}, titolo={}", idUtente, input.getTitolo());

        if (idUtente == null)
            throw new IdODatiMancantiException("ID utente o dati della segnalazione mancanti");

        if (!StringUtils.hasText(input.getTitolo()))
            throw new TitoloMancanteException("Titolo obbligatorio");

        if (!StringUtils.hasText(input.getDescrizione()))
            throw new DescrizioneMancanteException("Descrizione obbligatoria");

        if (input.getIdEnte() == null)
            throw new EnteNonTrovatoException("ID ente obbligatorio");

        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID " + idUtente + " non trovato."));

        if (!(utente instanceof CittadinoEntity))
            throw new UtenteNonCittadinoException("Solo i cittadini possono creare segnalazioni");

        EnteEntity ente = enteDao.findById(input.getIdEnte())
                .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato"));

        SegnalazioneEntity segnalazione = new SegnalazioneEntity();
        segnalazione.setTitolo(input.getTitolo());
        segnalazione.setDescrizione(input.getDescrizione());
        segnalazione.setLatitudine(input.getLatitudine());
        segnalazione.setLongitudine(input.getLongitudine());
        segnalazione.setCittadino((CittadinoEntity) utente); // mapping corretto
        segnalazione.setEnte(ente);
        segnalazione.setStato(StatoSegnalazione.INSERITO);
        segnalazione.setDataSegnalazione(LocalDateTime.now());

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);
        log.info("Segnalazione {} creata in stato {}", salvata.getIdSegnalazione(), salvata.getStato());

        return toOutput(salvata);
    }

    @Transactional
    public SegnalazioneOutput aggiornaStatoSegnalazione(
            Integer idEnte,
            Integer idSegnalazione,
            SegnalazioneUpdateInputEnte input) {

        log.info("Aggiornamento segnalazione id={} da ente={} con stato={}",
                idSegnalazione, idEnte, input.getStato());

        // Recupero dati
        SegnalazioneEntity segnalazione = getSegnalazione(idSegnalazione);
        EnteEntity ente = getEnte(idEnte);

        // Controllo autorizzazione
        checkEnteAutorizzato(segnalazione, ente);

        // Conversione stato
        StatoSegnalazione nuovoStato = convertiStato(input.getStato());

        // Aggiornamento stato
        if (segnalazione.getStato() == StatoSegnalazione.CHIUSO) {
            throw new StatoNonValidoException("La segnalazione è già chiusa");
        }

        // Aggiornamento ditta
        if (input.getDitta() != null && !input.getDitta().isBlank()) {
            segnalazione.setDitta(input.getDitta());
        }

        // Logica business: chiusura
        if (nuovoStato == StatoSegnalazione.CHIUSO) {
            segnalazione.setDataChiusura(LocalDateTime.now());
        }

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);

        log.info("Segnalazione {} aggiornata a stato {}", salvata.getIdSegnalazione(), nuovoStato);

        return toOutput(salvata);
    }

    //Ho centralizzato la logica di mapping in un metodo riutilizzabile, evitando duplicazioni e mantenendo il codice più leggibile.
    private List<SegnalazioneOutput> mapToOutputList(List<SegnalazioneEntity> entities) {
        return entities.stream()
                .map(this::toOutput)
                .toList();
    }

    // Mappa i commenti nella DTO
    public List<CommentoOutput> commentiOutputList(List<CommentoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(commentoEntity -> {
                    CommentoOutput output = new CommentoOutput();
                    output.setId(commentoEntity.getIdCommento());
                    output.setDescrizione(commentoEntity.getDescrizione());
                    output.setIdUtente(commentoEntity.getUtente().getId());
                    ZoneOffset offset = ZoneOffset.ofHours(1); // UTC+1
                    output.setDataCommento(commentoEntity.getDataCommento().atOffset(offset));

                    if (commentoEntity.getUtente() instanceof CittadinoEntity cittadino){
                        output.setNome(cittadino.getNome());
                        output.setCognome(cittadino.getCognome());
                    }

                    if (commentoEntity.getUtente() instanceof EnteEntity ente){
                        output.setNomeEnte(ente.getNomeEnte());
                    }

                    return output;
                })
                .toList();
    }

    // Mappa i commenti nella DTO
    public List<AllegatoOutput> allegatiOutputList(List<AllegatoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(allegatoEntity -> {
                    AllegatoOutput output = new AllegatoOutput();
                    output.setId(allegatoEntity.getId_allegato());
                    output.setNomeFile(allegatoEntity.getNomeFile());
                    return output;
                })
                .toList();
    }

    public SegnalazioneOutput toOutput(SegnalazioneEntity entity) {

        SegnalazioneOutput output = new SegnalazioneOutput();

        output.setId(entity.getIdSegnalazione());
        output.setTitolo(entity.getTitolo());
        output.setDescrizione(entity.getDescrizione());
        output.setLatitudine(entity.getLatitudine());
        output.setLongitudine(entity.getLongitudine());

        output.setStato(StatoEnum.valueOf(entity.getStato().name()));

        output.setIdUtente(
                entity.getCittadino() != null ? entity.getCittadino().getId() : null
        );

        output.setIdEnte(
                entity.getEnte() != null ? entity.getEnte().getId() : null
        );

        output.setDitta(entity.getDitta());

        ZoneId zone = ZoneId.systemDefault();

        output.setDataSegnalazione(
                entity.getDataSegnalazione().atZone(zone).toOffsetDateTime()
        );

        output.setDataChiusura(
                entity.getDataChiusura() != null
                        ? entity.getDataChiusura().atZone(zone).toOffsetDateTime()
                        : null
        );

        output.setCommenti(commentiOutputList(entity.getCommenti()));
        output.setAllegati(allegatiOutputList(entity.getAllegati()));

        return output;
    }

    public List<SegnalazioneOutput> getSegnalazioniByUserId(Integer id) {
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato."));

        if (utente instanceof CittadinoEntity) {
            List<SegnalazioneEntity> segnalazioni = segnalazioneDao.findByCittadino_Id(id);
            return mapToOutputList(segnalazioni);
        }

        if (utente instanceof EnteEntity) {
            List<SegnalazioneEntity> segnalazioni = segnalazioneDao.findByEnte_Id(id);
            return mapToOutputList(segnalazioni);
        }

        throw new AccessoNonAutorizzatoException("Ruolo utente non valido");
    }

    public SegnalazioneOutput getSegnalazioneById(Integer idUtente, Integer idSegnalazione) {

        // Recupera la segnalazione
        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata"));

        // Recupera l'utente
        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato"));

        // Controlla i permessi
        if (utente instanceof CittadinoEntity) {
            if (!segnalazione.getCittadino().getId().equals(idUtente)) {
                throw new AccessoNonAutorizzatoException("Non puoi vedere questa segnalazione");
            }
        } else if (utente instanceof EnteEntity) {
            if (!segnalazione.getEnte().getId().equals(idUtente)) {
                throw new AccessoNonAutorizzatoException("Non puoi vedere questa segnalazione");
            }
        } else {
            throw new AccessoNonAutorizzatoException("Tipo utente non autorizzato");
        }

        return toOutput(segnalazione);
    }

    public void cancellaSegnalazione(Integer id, Integer idSegnalazione) throws OperazioneNonPermessaException {

        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID : " + id + " non trovato."));
        if (!(utente instanceof CittadinoEntity)) {
            throw new UtenteNonCittadinoException("Solo i cittadini possono eliminare una segnalazione.");
        }

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione con ID : " + idSegnalazione + " non trovata."));

        if (!Objects.equals(segnalazione.getCittadino().getId(), id)) {
            throw new OperazioneNonPermessaException("Non puoi eliminare una segnalazione che non ti appartiene.");

        }

        if (segnalazione.getStato() != StatoSegnalazione.INSERITO && segnalazione.getStato() != StatoSegnalazione.CHIUSO) {
            throw new StatoNonValidoException(
                    "Non puoi eliminare una segnalazione in stato " + segnalazione.getStato()
            );
        }
        segnalazioneDao.delete(segnalazione);
    }

    @Transactional
    public SegnalazioneOutput modificaSegnalazione(Integer id, Integer idSegnalazione, SegnalazioneInput input) {

        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() ->
                        new UtenteNonTrovatoException("Utente con ID " + id + " non trovato.")
                );

        if (!(utente instanceof CittadinoEntity)) {
            throw new OperazioneNonPermessaException("Solo i cittadini possono modificare una segnalazione.");
        }

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() ->
                        new SegnalazioneNonTrovataException("Segnalazione con ID " + idSegnalazione + " non trovata.")
                );

        if (!Objects.equals(segnalazione.getCittadino().getId(), id)) {
            throw new OperazioneNonPermessaException("Non puoi modificare una segnalazione che non ti appartiene.");
        }

        if (segnalazione.getStato() == StatoSegnalazione.CHIUSO) {
            throw new StatoNonValidoException("Non puoi modificare una segnalazione chiusa.");
        }

        // Aggiornamento campi
        if (input.getTitolo() != null) segnalazione.setTitolo(input.getTitolo());
        if (input.getDescrizione() != null) segnalazione.setDescrizione(input.getDescrizione());
        if (input.getLatitudine() != null) segnalazione.setLatitudine(input.getLatitudine());
        if (input.getLongitudine() != null) segnalazione.setLongitudine(input.getLongitudine());

        if (input.getIdEnte() != null) {
            EnteEntity ente = enteDao.findById(input.getIdEnte())
                    .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato."));
            segnalazione.setEnte(ente);
        }

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);
        return toOutput(salvata);
    }
}

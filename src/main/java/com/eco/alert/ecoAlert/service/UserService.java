package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.CittadinoDao;
import com.eco.alert.ecoAlert.dao.EnteDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.entity.CittadinoEntity;
import com.eco.alert.ecoAlert.entity.EnteEntity;
import com.eco.alert.ecoAlert.entity.SegnalazioneEntity;
import com.eco.alert.ecoAlert.entity.UtenteEntity;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.enums.Ruolo;
import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class UserService {

    private final UtenteDao utenteDao;
    private final CittadinoDao cittadinoDao;
    private final EnteDao enteDao;
    private final SegnalazioneDao segnalazioneDao;
    private final SegnalazioneService segnalazioneService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UtenteDao utenteDao, CittadinoDao cittadinoDao, EnteDao enteDao, SegnalazioneDao segnalazioneDao, SegnalazioneService segnalazioneService, BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utenteDao = utenteDao;
        this.cittadinoDao = cittadinoDao;
        this.enteDao = enteDao;
        this.segnalazioneDao = segnalazioneDao;
        this.segnalazioneService = segnalazioneService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // HELPER METHODS PRIVATE
    private UtenteOutput toOutput(UtenteEntity utente) {
        UtenteOutput output = new UtenteOutput();
        output.setId(utente.getId());
        output.setEmail(utente.getEmail());

        if (utente instanceof CittadinoEntity) {
            output.setRuolo(Ruolo.CITTADINO.name());
        } else if (utente instanceof EnteEntity) {
            output.setRuolo(Ruolo.ENTE.name());
        }

        return output;
    }
    
    private StatoSegnalazione convertiStato(StatoEnum statoInput) {
        if (statoInput == null) {
            return null;
        }
        
        try {
            return StatoSegnalazione.valueOf(statoInput.name());
        } catch (IllegalArgumentException e) {
            throw new StatoNonValidoException("Stato non valido: " + statoInput);
        }
    }

    private UtenteOutput creaCittadino(UtenteInput input) {

        if (input.getNome() == null || input.getCognome() == null) {
            throw new IdODatiMancantiException("Nome e cognome obbligatori.");
        }

        // Impediamo che un cittadino inserisca un nome da ente
        if (input.getNome().toLowerCase().contains("comune di")) {
            throw new OperazioneNonPermessaException(
                    "Un cittadino non può registrarsi come ente."
            );
        }

        CittadinoEntity cittadino = new CittadinoEntity();
        cittadino.setEmail(input.getEmail());
        cittadino.setPassword(passwordEncoder.encode(input.getPassword()));
        cittadino.setNome(input.getNome());
        cittadino.setCognome(input.getCognome());
        cittadino.setCitta(input.getCitta());
        cittadino.setNumeroTelefono(input.getNumeroTelefono());
        cittadino.setCodiceFiscale(input.getCodiceFiscale());

        CittadinoEntity saved = cittadinoDao.save(cittadino);

        return toOutput(saved);
    }

    private UtenteOutput creaEnte(UtenteInput input) {

        if (input.getNome() == null || input.getNome().isBlank()) {
            throw new IdODatiMancantiException("Nome ente obbligatorio.");
        }

        String nomeEnte = input.getNome().trim();

        // Deve iniziare con "Comune di"
        if (!nomeEnte.toLowerCase().startsWith("comune di ")) {
            throw new OperazioneNonPermessaException(
                    "Il nome ente deve iniziare con 'Comune di ...'"
            );
        }

        // Email istituzionale obbligatoria
        if (!isEmailIstituzionale(input.getEmail())) {
            throw new OperazioneNonPermessaException(
                    "Registrazione ente consentita solo con email istituzionale."
            );
        }
        EnteEntity ente = new EnteEntity();
        ente.setEmail(input.getEmail());
        ente.setPassword(passwordEncoder.encode(input.getPassword()));
        ente.setNomeEnte(nomeEnte);
        ente.setCittaEnte(input.getCitta());

        EnteEntity saved = enteDao.save(ente);

        return toOutput(saved);
    }

    private boolean isEmailIstituzionale(String email) {
        String lower = email.toLowerCase();

        return lower.endsWith(".gov.it") || lower.contains("@comune.");
    }

    @PreAuthorize("permitAll()")
    @Transactional
    public UtenteOutput creaUtente(UtenteInput input) {

        log.info("Creazione utente email={}, ruolo={}", input.getEmail(), input.getRuolo());

        if (input.getEmail() == null || input.getEmail().isBlank()) {
            throw new IdODatiMancantiException("Email obbligatoria.");
        }

        if (input.getPassword() == null || input.getPassword().length() < 8) {
            throw new IdODatiMancantiException("Password non valida.");
        }

        UtenteEntity utenteConStessaMail = utenteDao.findByEmail(input.getEmail());
        if (utenteConStessaMail != null) {
            throw new EmailDuplicataException();
        }

        Ruolo ruolo;
        try {
            ruolo = Ruolo.valueOf(input.getRuolo().toUpperCase());
        } catch (Exception e) {
            throw new OperazioneNonPermessaException("Ruolo non valido.");
        }

        return switch (ruolo) {
            case CITTADINO -> creaCittadino(input);
            case ENTE -> creaEnte(input);
        };
    }

    @PreAuthorize("permitAll()")
    @Transactional
    public LoginOutput login (LoginInput loginInput) {
        log.info("Login Utente...");

        if (loginInput.getEmail() == null || loginInput.getPassword() == null) {
            throw new LoginException("Email o password mancanti");
        }

        UtenteEntity utente = utenteDao.findByEmail(loginInput.getEmail());
        if(utente == null){
            throw new LoginException("Credenziali non valide");
        }

        if(!passwordEncoder.matches(loginInput.getPassword(), utente.getPassword())){
            throw new LoginException("Credenziali non valide");
        }

        Ruolo ruolo = (utente instanceof CittadinoEntity)
                ? Ruolo.CITTADINO
                : Ruolo.ENTE;
        String token = jwtService.generateToken(utente.getId(), ruolo.name());

        LoginOutput output = new LoginOutput();
        output.setToken(token);
        output.setRuolo(ruolo.name());
        output.setUserId(utente.getId());
        return output;
    }

    @PreAuthorize("#id == authentication.principal") //solo proprietario
    @Transactional
    public UtenteDettaglioOutput getUserById(Integer id) {
        log.info("Recupero utente con ID {}", id);

        // Cerca l'utente nella tabella base
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID " + id + " non trovato."));

        UtenteDettaglioOutput output = new UtenteDettaglioOutput();
        output.setId(utente.getId());
        output.setEmail(utente.getEmail());

        // Controlla il tipo effettivo dell’utente
        if (utente instanceof CittadinoEntity cittadino) {
            output.setRuolo(Ruolo.CITTADINO.name());
            output.setCognome(cittadino.getCognome());
            output.setNome(cittadino.getNome());
            output.setCitta(cittadino.getCitta());
            output.setCodiceFiscale(cittadino.getCodiceFiscale());
            output.setEmail(utente.getEmail());
            output.setNumeroTelefono(cittadino.getNumeroTelefono());
        } else if (utente instanceof EnteEntity ente) {
            output.setRuolo(Ruolo.ENTE.name());
            output.setNomeEnte(ente.getNomeEnte());
            output.setCitta(ente.getCittaEnte());
        }

        return output;
    }

    @PreAuthorize("hasRole('CITTADINO')")
    @Transactional
    public List<EnteOutput> getAllEnti() {
        List<EnteEntity> enti = enteDao.findAll();
        List<EnteOutput> result = new ArrayList<>();

        for (EnteEntity ente : enti) {
            EnteOutput enteOutput = new EnteOutput();
            enteOutput.setId(ente.getId());
            enteOutput.setNomeEnte(ente.getNomeEnte());
            enteOutput.setCitta(ente.getCittaEnte());
            enteOutput.setEmail(ente.getEmail());
            result.add(enteOutput);
        }
        return result;
    }

    @PreAuthorize("#id == authentication.principal")
    @Transactional
    public void deleteUser(Integer id) {
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato."));

        if (utente instanceof CittadinoEntity cittadino) {
            boolean hasSegnalazioniAttive = cittadino.getSegnalazioni().stream()
                    .anyMatch(s -> s.getStato() != StatoSegnalazione.CHIUSO);
            if (hasSegnalazioniAttive) {
                throw new OperazioneNonPermessaException(
                        "Impossibile eliminare utente con segnalazioni attive"
                );
            }
            cittadinoDao.delete(cittadino);
            return;
        }
        if (utente instanceof EnteEntity ente) {
            boolean hasSegnalazioniAttive = ente.getSegnalazioniGestite().stream()
                    .anyMatch(s -> s.getStato() != StatoSegnalazione.CHIUSO);
            if (hasSegnalazioniAttive) {
                throw new OperazioneNonPermessaException(
                        "Impossibile eliminare ente con segnalazioni attive"
                );
            }
            enteDao.delete(ente);
            return;
        }
    }

    @PreAuthorize("#id == authentication.principal")
    @Transactional
    public UtenteDettaglioOutput updateUser(Integer id, UtenteUpdateInput input) {
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato"));

        if (input.getEmail() != null) {
            UtenteEntity existing = utenteDao.findByEmail(input.getEmail());

            if (existing != null && !existing.getId().equals(id)) {
                throw new EmailDuplicataException();
            }

            utente.setEmail(input.getEmail());
        }

        if (utente instanceof CittadinoEntity cittadino) {
            cittadino.setNome(input.getNome());
            cittadino.setCognome(input.getCognome());
            cittadino.setCitta(input.getCitta());
            cittadino.setNumeroTelefono(input.getNumeroTelefono());
            cittadino.setCodiceFiscale(input.getCodiceFiscale());

            cittadinoDao.save(cittadino);
        }

        if (utente instanceof EnteEntity ente) {
            ente.setNomeEnte(input.getNome());
            ente.setCittaEnte(input.getCitta());

            enteDao.save(ente);
        }

        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            utente.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        utenteDao.save(utente);

        return getUserById(id);
    }

    @PreAuthorize("#idEnte == authentication.principal")
    @Transactional
    public List<SegnalazioneOutput> getSegnalazioniByEnteAndStato(
            Integer idEnte,
            StatoEnum stato
    ) {
        StatoSegnalazione statoConvertito = convertiStato(stato);

        List<SegnalazioneEntity> segnalazioni;

        if (statoConvertito != null) {
            segnalazioni = segnalazioneDao.findByEnte_IdAndStato(idEnte, statoConvertito);
        } else {
            segnalazioni = segnalazioneDao.findByEnte_Id(idEnte);
        }

        return segnalazioni.stream().map(segnalazioneService::toOutput).toList();
    }

    @PreAuthorize("#idEnte == authentication.principal")
    @Transactional
    public SegnalazioniStatisticheOutput getSegnalazioniStatistiche(Integer idEnte){

        List<Object[]> result = segnalazioneDao.countSegnalazioniByStato(idEnte);

        SegnalazioniStatisticheOutput stats = new SegnalazioniStatisticheOutput();

        for(Object[] row : result){
            StatoSegnalazione stato = (StatoSegnalazione) row[0];
            Long count = (Long) row[1];

            switch(stato){
                case INSERITO -> stats.setINSERITO(count.intValue());
                case RICEVUTO -> stats.setRICEVUTO(count.intValue());
                case SOSPESO -> stats.setSOSPESO(count.intValue());
                case CHIUSO -> stats.setCHIUSO(count.intValue());
            }
        }

        return stats;
    }
}

package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.Error;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class ExceptionController {

    // Creo un metodo helper
    private ResponseEntity<Error> buildError(String detail, String message, HttpStatus status) {
        log.error("Errore: {} - {}", detail, message);

        Error error = new Error().detail(detail).message(message);

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EmailDuplicataException.class)
    public ResponseEntity<Error> handleEmailDuplicata(EmailDuplicataException ex) {
        return buildError("Email già presente nel database.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<Error> handleLoginException(LoginException ex) {
        return buildError("Dati non validi.", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UtenteNonTrovatoException.class)
    public ResponseEntity<Error> handleUserNotFound(UtenteNonTrovatoException ex) {
        return buildError("Utente non trovato.", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UtenteNonCittadinoException.class)
    public ResponseEntity<Error> handleUserNotCittadino(UtenteNonCittadinoException ex) {
        return buildError("Solo i cittadini possono creare segnalazioni.", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EnteNonAutorizzatoException.class)
    public ResponseEntity<Error> handleEnteSbagliato(EnteNonAutorizzatoException ex) {
        return buildError("Ente non autorizzato.", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EnteNonTrovatoException.class)
    public ResponseEntity<Error> handleEnteNonTrovato(EnteNonTrovatoException ex) {
        return buildError("Ente non trovato.", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SegnalazioneNonTrovataException.class)
    public ResponseEntity<Error> handleSegnalazioneNonTrovata(SegnalazioneNonTrovataException ex) {
        return buildError("Segnalazione non trovata", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IdODatiMancantiException.class)
    public ResponseEntity<Error> handleDatiMancanti(IdODatiMancantiException ex) {
        return buildError("Dati della segnalazione mancanti.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TitoloMancanteException.class)
    public ResponseEntity<Error> handleTitoloMancante(TitoloMancanteException ex) {
        return buildError("Titolo mancante.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DescrizioneMancanteException.class)
    public ResponseEntity<Error> handleDescrizioneMancante(DescrizioneMancanteException ex) {
        return buildError("Descrizione mancante.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessoNonAutorizzatoException.class)
    public ResponseEntity<Error> handleAccessoNonAutorizzato(AccessoNonAutorizzatoException ex) {
        return buildError("Non sei autorizzato.", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OperazioneNonPermessaException.class)
    public ResponseEntity<Error> handleOperazioneNonPermessa(OperazioneNonPermessaException ex) {
        return buildError("Operazione non permessa.", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AllegatoNonTrovatoException.class)
    public ResponseEntity<Error> handleAllegatoNonTrovato(AllegatoNonTrovatoException ex) {
        return buildError("Allegato non trovato.", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StatoNonValidoException.class)
    public ResponseEntity<Error> handleStatoNonValido(StatoNonValidoException ex) {
        return buildError("Stato non valido.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CommentoNonTrovatoException.class)
    public ResponseEntity<Error> handleCommentoNonTrovato(CommentoNonTrovatoException ex) {
        return buildError("Commento non trovato.", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleGenericException(Exception ex) {

        ex.printStackTrace(); // 🔥 fondamentale

        return buildError(
                ex.getClass().getSimpleName(),  // tipo errore
                ex.getMessage(),                // messaggio reale
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

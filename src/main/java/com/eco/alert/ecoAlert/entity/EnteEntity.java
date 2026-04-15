package com.eco.alert.ecoAlert.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Rappresenta un ente che gestisce le segnalazioni.
 * Estende UtenteEntity tramite ereditarietà JOINED.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "ente")
@PrimaryKeyJoinColumn(name = "id")
public class EnteEntity extends UtenteEntity{

    @Column(name = "nome_Ente")
    private String nomeEnte;

    @Column(name = "citta")
    private String cittaEnte;

    /**
     * Segnalazioni gestite dall’ente.
     * Nessun cascade → le segnalazioni non devono essere eliminate
     * se un ente viene eliminato.
     */
    @OneToMany(mappedBy = "ente")
    private List<SegnalazioneEntity> segnalazioniGestite;
}

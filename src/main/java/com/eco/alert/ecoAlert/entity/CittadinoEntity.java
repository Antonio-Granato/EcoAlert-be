package com.eco.alert.ecoAlert.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Rappresenta un cittadino del sistema.
 * Estende UtenteEntity tramite ereditarietà JOINED.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "cittadino")
@PrimaryKeyJoinColumn(name = "id")
public class CittadinoEntity extends UtenteEntity{

    @Column(name = "nome")
    private String nome;

    @Column(name = "cognome")
    private String cognome;

    @Column(name = "citta")
    private String citta;

    @Column(name = "numero_telefono")
    private String numeroTelefono;

    @Column(name = "codice_fiscale", unique = true)
    private String codiceFiscale;

    /**
     * Segnalazioni create dal cittadino.
     * Se il cittadino viene eliminato, vengono eliminate tutte le sue segnalazioni.
     */
    @OneToMany(mappedBy = "cittadino", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SegnalazioneEntity> segnalazioni;
}

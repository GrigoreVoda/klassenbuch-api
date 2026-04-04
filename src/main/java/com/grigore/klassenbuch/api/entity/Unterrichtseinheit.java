package com.grigore.klassenbuch.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;



@Entity
@Table(name = "unterrichtseinheit")
public class Unterrichtseinheit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "einheit_id")
    private Integer einheitId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lerntag_id", nullable = false)
    @JsonIgnore
    private Lerntag lerntag;

    @Column(name = "stunde")
    private Integer stunde;

    @Column(name = "inhalt", columnDefinition = "TEXT")
    private String inhalt;


    public Unterrichtseinheit() {}

    public Unterrichtseinheit(Integer einheitId, Lerntag lerntag, Integer stunde, String inhalt) {
        this.einheitId = einheitId;
        this.lerntag = lerntag;
        this.stunde = stunde;
        this.inhalt = inhalt;
    }

    public Unterrichtseinheit(Lerntag lt, Integer stunde, String inhalt) {
    }

    public Integer getEinheitId() {
        return einheitId;
    }

    public void setEinheitId(Integer einheitId) {
        this.einheitId = einheitId;
    }

    public Lerntag getLerntag() {
        return lerntag;
    }

    public void setLerntag(Lerntag lerntag) {
        this.lerntag = lerntag;
    }

    public Integer getStunde() {
        return stunde;
    }

    public void setStunde(Integer stunde) {
        this.stunde = stunde;
    }

    public String getInhalt() {
        return inhalt;
    }

    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }
}

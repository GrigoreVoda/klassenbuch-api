package com.grigore.klassenbuch.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lerntag")
public class Lerntag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lerntag_id")
    private Integer lerntagId;

    @NotNull(message = "datum muss nicht null sein")
    @Column(name = "datum", nullable = false, unique = true)
    private LocalDate datum;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "lernfeld_id", nullable = true)
    private Lernfeld lernfeld;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "dozent_id", nullable = true)
    private Dozent dozent;

    @OneToMany(
            mappedBy      = "lerntag",
            cascade       = CascadeType.ALL,
            orphanRemoval = true,
            fetch         = FetchType.LAZY
    )
    @OrderBy("stunde ASC")
    private List<Unterrichtseinheit> einheiten = new ArrayList<>();

    public Lerntag() {}

    public Lerntag(Integer lerntagId, LocalDate datum, Lernfeld lernfeld,
                   Dozent dozent, List<Unterrichtseinheit> unterrichtseinheit) {
        this.lerntagId = lerntagId;
        this.datum = datum;
        this.lernfeld = lernfeld;
        this.dozent = dozent;
        this.einheiten = unterrichtseinheit;
    }

    public Lerntag(@NotNull(message = "datum must not be null") LocalDate datum, Lernfeld lf, Dozent d) {
    }

    public List<Unterrichtseinheit> getEinheiten() {
        return einheiten;
    }

    public void setEinheiten(List<Unterrichtseinheit> einheiten) {
        this.einheiten = einheiten;
    }

    public Integer getLerntagId() {
        return lerntagId;
    }

    public void setLerntagId(Integer lerntagId) {
        this.lerntagId = lerntagId;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public Lernfeld getLernfeld() {
        return lernfeld;
    }

    public void setLernfeld(Lernfeld lernfeld) {
        this.lernfeld = lernfeld;
    }

    public Dozent getDozent() {
        return dozent;
    }

    public void setDozent(Dozent dozent) {
        this.dozent = dozent;
    }
}

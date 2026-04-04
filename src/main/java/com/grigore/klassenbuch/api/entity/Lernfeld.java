package com.grigore.klassenbuch.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lernfeld")
public class Lernfeld {
    @Id
    @Column(name = "lernfeld_id", length = 10)
    @NotBlank(message = "lernfeld_id nicht leer sein")
    @Size(max = 10, message = "lernfeld_id muss mindestens 10 characters sein")
    private String lernfeldId;

    @NotBlank(message = "titel muss nicht leer sein")
    @Size(max = 255)
    @Column(name = "titel", nullable = false)
    private String titel;

    @Column(name = "start_datum")
    private LocalDate startDatum;

    @Column(name = "end_datum")
    private LocalDate endDatum;

    // Many-to-many via lernfeld_dozent junction table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name               = "lernfeld_dozent",
            joinColumns        = @JoinColumn(name = "lernfeld_id"),
            inverseJoinColumns = @JoinColumn(name = "dozent_id")
    )
    @JsonIgnore
    private List<Dozent> dozenten = new ArrayList<>();

    // Cross-field validation: endDatum must not be before startDatum
    @AssertTrue(message = "end_datum muss nich vor dem start_datum sein")
    @JsonIgnore
    public boolean isDateRangeValid() {
        if (startDatum == null || endDatum == null) return true;
        return startDatum.isBefore(endDatum);
    }

    public Lernfeld() {}

    public Lernfeld(String lernfeldId, String titel, LocalDate startDatum, LocalDate endDatum) {
        this.lernfeldId = lernfeldId;
        this.titel = titel;
        this.startDatum = startDatum;
        this.endDatum = endDatum;
    }

    public Lernfeld(String lernfeldId, String titel, LocalDate startDatum, LocalDate endDatum, List<Dozent> dozenten) {
        this.lernfeldId = lernfeldId;
        this.titel = titel;
        this.startDatum = startDatum;
        this.endDatum = endDatum;
        this.dozenten = dozenten;
    }

    public String getLernfeldId() {
        return lernfeldId;
    }

    public void setLernfeldId(String lernfeldId) {
        this.lernfeldId = lernfeldId;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public void setStartDatum(LocalDate startDatum) {
        this.startDatum = startDatum;
    }

    public LocalDate getEndDatum() {
        return endDatum;
    }

    public void setEndDatum(LocalDate endDatum) {
        this.endDatum = endDatum;
    }

    public List<Dozent> getDozenten() {
        return dozenten;
    }

    public void setDozenten(List<Dozent> dozenten) {
        this.dozenten = dozenten;
    }
}

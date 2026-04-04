package com.grigore.klassenbuch.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dozent")
public class Dozent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dozent_id")
    private Integer dozentId;

    @NotBlank(message = "vorname muss nich leer sein")
    @Size(max = 50, message = "vorname muss mindestens 50 characters sein")
    @Column(name = "vorname", nullable = false, length = 50)
    private String vorname;

    @NotBlank(message = "nachname muss nich leer sein")
    @Size(max = 50, message = "nachname muss mindestens 50 characters sein")
    @Column(name = "nachname", nullable = false, length = 50)
    private String nachname;

    @ManyToMany(mappedBy = "dozenten", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Lernfeld> lernfelder = new ArrayList<>();

    public Dozent() {
    }

    public Dozent( String vorname, String nachname) {
        this.vorname = vorname;
        this.nachname = nachname;
    }

    public Dozent(Integer dozentId, String vorname, String nachname, List<Lernfeld> lernfelder) {
        this.dozentId = dozentId;
        this.vorname = vorname;
        this.nachname = nachname;
        this.lernfelder = lernfelder;
    }

    public Integer getDozentId() {
        return dozentId;
    }

    public void setDozentId(Integer dozentId) {
        this.dozentId = dozentId;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public List<Lernfeld> getLernfelder() {
        return lernfelder;
    }

    public void setLernfelder(List<Lernfeld> lernfelder) {
        this.lernfelder = lernfelder;
    }
}

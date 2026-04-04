package com.grigore.klassenbuch.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class Dto {

    public Dto() {
    }
    public record DozentRequest(
            @NotBlank(message = "vorname muss nicht leer sein")
            @Size(max = 50)
            String vorname,

            @NotBlank(message = "nachname muss nicht leer sein")
            @Size(max = 50)
            String nachname
    ) {}

    public record DozentResponse(
            Integer dozentId,
            String  vorname,
            String  nachname
    ) {}


    public record LernfeldRequest(
            @NotBlank @Size(max = 10)
            String    lernfeldId,
            @NotBlank @Size(max = 255)
            String    titel,
            LocalDate startDatum,
            LocalDate endDatum
    ) {}

    public record LernfeldResponse(
            String               lernfeldId,
            String               titel,
            LocalDate            startDatum,
            LocalDate            endDatum,
            List<DozentResponse> dozenten

    ) {}

    public record LerntagRequest(
            @NotNull(message = "datum must not be null")
            LocalDate datum,
            String  lernfeldId,
            Integer dozentId
    ) {}

    public record LerntagResponse(
            Integer   lerntagId,
            LocalDate datum,
            String    lernfeldId,
            String    lernfeldTitel,
            Integer   dozentId,
            String    dozentVorname,
            String    dozentNachname,
            List<EinheitResponse> einheiten
    ) {}

    public record LerntagSummary(
            Integer   lerntagId,
            LocalDate datum,
            String    lernfeldId,
            String    lernfeldTitel,
            Integer   dozentId,
            String    dozentVorname,
            String    dozentNachname
    ) {}


    public record EinheitRequest(
         //   @NotNull @Min(1) @Max(9)
            Integer stunde,
            String  inhalt   // nullable
    ) {}

    public record EinheitResponse(
            Integer einheitId,
            Integer lerntagId,
            Integer stunde,
            String  inhalt
    ) {}

}

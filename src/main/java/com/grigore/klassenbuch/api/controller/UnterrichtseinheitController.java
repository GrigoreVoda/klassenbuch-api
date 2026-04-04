package com.grigore.klassenbuch.api.controller;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.service.UnterrichtseinheitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Unterrichtseinheiten")
@RestController
@RequestMapping("/lerntage/{lerntagId}/einheiten")
public class UnterrichtseinheitController {

    private final UnterrichtseinheitService service;
    public UnterrichtseinheitController(UnterrichtseinheitService s) { this.service = s; }

    @GetMapping
    public List<EinheitResponse> getAll(@PathVariable Integer lerntagId) {
        return service.findAll(lerntagId);
    }

    @GetMapping("/{id}")
    public EinheitResponse getById(@PathVariable Integer lerntagId,
                                   @PathVariable Integer id) {
        return service.findById(lerntagId, id);
    }

    @PostMapping
    public ResponseEntity<EinheitResponse> create(
            @PathVariable Integer lerntagId,
            @RequestBody @Valid EinheitRequest req) {
        EinheitResponse created = service.create(lerntagId, req);
        return ResponseEntity
                .created(URI.create("/lerntage/" + lerntagId + "/einheiten/" + created.einheitId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public EinheitResponse update(@PathVariable Integer lerntagId,
                                  @PathVariable Integer id,
                                  @RequestBody @Valid EinheitRequest req) {
        return service.update(lerntagId, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer lerntagId,
                                       @PathVariable Integer id) {
        service.delete(lerntagId, id);
        return ResponseEntity.noContent().build();
    }
}


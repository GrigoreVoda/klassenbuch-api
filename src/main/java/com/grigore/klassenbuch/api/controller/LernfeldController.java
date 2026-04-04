package com.grigore.klassenbuch.api.controller;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.service.LernfeldService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Lernfelder")
@RestController
@RequestMapping("/lernfelder")
public class LernfeldController {

    private final LernfeldService service;
    public LernfeldController(LernfeldService s) { this.service = s; }

    @GetMapping
    public List<LernfeldResponse> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public LernfeldResponse getById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<LernfeldResponse> create(
            @RequestBody @Valid LernfeldRequest req) {
        LernfeldResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/lernfelder/" + created.lernfeldId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public LernfeldResponse update(@PathVariable String id,
                                   @RequestBody @Valid LernfeldRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dozenten")
    public List<DozentResponse> getDozenten(@PathVariable String id) {
        return service.findDozenten(id);
    }

    @PostMapping("/{id}/dozenten/{dozentId}")
    public LernfeldResponse assignDozent(@PathVariable String id,
                                         @PathVariable Integer dozentId) {
        return service.assignDozent(id, dozentId);
    }

    @DeleteMapping("/{id}/dozenten/{dozentId}")
    public ResponseEntity<Void> removeDozent(@PathVariable String id,
                                             @PathVariable Integer dozentId) {
        service.removeDozent(id, dozentId);
        return ResponseEntity.noContent().build();
    }
}

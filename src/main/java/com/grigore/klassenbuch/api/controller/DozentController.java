package com.grigore.klassenbuch.api.controller;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.service.DozentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Dozenten")
@RestController
@RequestMapping("/dozenten")
public class DozentController {

    private final DozentService service;
    public DozentController(DozentService s) { this.service = s; }

    @GetMapping
    public List<DozentResponse> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public DozentResponse getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<DozentResponse> create(
            @RequestBody @Valid DozentRequest req) {
        DozentResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/dozenten/" + created.dozentId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public DozentResponse update(@PathVariable Integer id,
                                 @RequestBody @Valid DozentRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}

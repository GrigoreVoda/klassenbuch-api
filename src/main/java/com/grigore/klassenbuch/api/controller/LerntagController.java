package com.grigore.klassenbuch.api.controller;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.service.LerntagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Lerntage")
@RestController
@RequestMapping("/lerntage")
public class LerntagController {

    private final LerntagService service;
    public LerntagController(LerntagService s) { this.service = s; }

    @GetMapping // nach lernfeld, mit unterrichtseinheiten
    public List<LerntagResponse> getAll(
            @RequestParam(value = "lernfeld_id", required = false) String lernfeldId) {
        return service.findAll(lernfeldId);
    }

    @GetMapping("/summary/") // nach lernfeld
    public List<LerntagSummary> getAllSummary(
            @RequestParam(value = "lernfeld_id",
                    required = false) String lernfeldId) {
        return service.findAllSummary(lernfeldId);
    }

    @Operation(
            summary     = "Search lerntage by einheit inhalt",
            description = "Case-insensitive partial match on unterrichtseinheit.inhalt. "
                    + "Returns complete lerntage including all their einheiten."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "List of matching lerntage (empty if none found)"),
            @ApiResponse(responseCode = "400",
                    description = "Search query is missing or blank")
    })
    @GetMapping("/search")
    public ResponseEntity<List<LerntagResponse>> search(
            @Parameter(description = "Search string (case-insensitive, partial match)",
                    example = "Linux")
            @RequestParam("q") String q) {

        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.search(q));
    }

    @GetMapping("/{id}")
    public LerntagResponse getById(@PathVariable Integer id) {
        return service.findById(id);
    }

  //  @GetMapping("/sucheContext/{context}")
   // public List<LerntagResponse> findText(@PathVariable String context){
   //     return service.
  //  }

    @PostMapping
    public ResponseEntity<LerntagResponse> create(
            @RequestBody @Valid LerntagRequest req) {
        LerntagResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/lerntage/" + created.lerntagId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public LerntagResponse update(@PathVariable Integer id,
                                  @RequestBody @Valid LerntagRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}

package com.grigore.klassenbuch.api.service;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.entity.Dozent;
import com.grigore.klassenbuch.api.entity.Lernfeld;
import com.grigore.klassenbuch.api.entity.Lerntag;
import com.grigore.klassenbuch.api.exception.GlobalExceptionHandler;
import com.grigore.klassenbuch.api.exception.ResourceNotFoundException;
import com.grigore.klassenbuch.api.repository.DozentRepository;
import com.grigore.klassenbuch.api.repository.LernfeldRepository;
import com.grigore.klassenbuch.api.repository.LerntagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LerntagService {

    private static final Logger log = LoggerFactory.getLogger(LerntagService.class);
    private final LerntagRepository  lerntagRepo;
    private final LernfeldRepository lernfeldRepo;
    private final DozentRepository   dozentRepo;

    public LerntagService(LerntagRepository l, LernfeldRepository lf, DozentRepository d) {
        this.lerntagRepo = l; this.lernfeldRepo = lf; this.dozentRepo = d;
    }

    private LerntagResponse toResponse(Lerntag lt) {
        Lernfeld lf = lt.getLernfeld();
        Dozent   d  = lt.getDozent();

        List<EinheitResponse> einheiten = lt.getEinheiten()
                .stream()
                .map(e -> new EinheitResponse(
                        e.getEinheitId(),
                        lt.getLerntagId(),
                        e.getStunde(),
                        e.getInhalt()))
                .toList();

        return new LerntagResponse(
                lt.getLerntagId(),
                lt.getDatum(),
                lf != null ? lf.getLernfeldId() : null,
                lf != null ? lf.getTitel()      : null,
                d  != null ? d.getDozentId()    : null,
                d  != null ? d.getVorname()     : null,
                d  != null ? d.getNachname()    : null,
                einheiten);                                 // ← new
    }

    private LerntagSummary toSummary(Lerntag lt) {
        Lernfeld lf = lt.getLernfeld();
        Dozent   d  = lt.getDozent();
        return new LerntagSummary(
                lt.getLerntagId(), lt.getDatum(),
                lf != null ? lf.getLernfeldId() : null,
                lf != null ? lf.getTitel()      : null,
                d  != null ? d.getDozentId()    : null,
                d  != null ? d.getVorname()     : null,
                d  != null ? d.getNachname()    : null);
    }


    public Lerntag getEntityById(Integer id) {
        return lerntagRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lerntag", id));
    }

    public List<LerntagResponse> findAll(String lernfeldId) {
        if (lernfeldId != null)
            return lerntagRepo.findByLernfeld_LernfeldIdOrderByDatumAsc(lernfeldId)
                    .stream().map(this::toResponse).toList();
        return lerntagRepo.findAllWithDetails().stream().map(this::toResponse).toList();
    }
    // findAll() ohne einheiten
    public List<LerntagSummary> findAllSummary(String lernfeldId) {
        if (lernfeldId != null)
            return lerntagRepo
                    .findByLernfeld_LernfeldIdOrderByDatumAsc(lernfeldId)
                    .stream().map(this::toSummary).toList();
        return lerntagRepo.findAllWithDetails()
                .stream().map(this::toSummary).toList();
    }
    public LerntagResponse findById(Integer id) {
        return lerntagRepo.findByIdWithDetails(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Lerntag", id));
    }

    public List<LerntagResponse> search(String search) {
        if (search == null || search.isBlank()) {
            return List.of();  // return empty instead of searching for blank
        }
        log.info("Searching lerntage by einheit inhalt: '{}'", search);
        return lerntagRepo.searchByEinheitInhalt(search.trim())
                .stream()
                .map(this::toResponse)   // uses the existing toResponse() with einheiten
                .toList();
    }

    @Transactional
    public LerntagResponse create(LerntagRequest req) {
        if (lerntagRepo.existsByDatum(req.datum()))
            throw new GlobalExceptionHandler.ConflictException("Lerntag already exists for datum=" + req.datum());
        Lernfeld lf = req.lernfeldId() != null
                ? lernfeldRepo.findById(req.lernfeldId())
                  .orElseThrow(() -> new ResourceNotFoundException("Lernfeld", req.lernfeldId())) : null;
        Dozent d = req.dozentId() != null
                ? dozentRepo.findById(req.dozentId())
                  .orElseThrow(() -> new ResourceNotFoundException("Dozent", req.dozentId())) : null;
        log.info("Creating lerntag datum={}", req.datum());
        return toResponse(lerntagRepo.save(new Lerntag(req.datum(), lf, d)));
    }

    @Transactional
    public LerntagResponse update(Integer id, LerntagRequest req) {
        Lerntag lt = getEntityById(id);
        if (lerntagRepo.existsByDatumAndLerntagIdNot(req.datum(), id))
            throw new GlobalExceptionHandler.ConflictException("Another Lerntag already exists for datum=" + req.datum());
        lt.setDatum(req.datum());
        lt.setLernfeld(req.lernfeldId() != null
                ? lernfeldRepo.findById(req.lernfeldId())
                  .orElseThrow(() -> new ResourceNotFoundException("Lernfeld", req.lernfeldId())) : null);
        lt.setDozent(req.dozentId() != null
                ? dozentRepo.findById(req.dozentId())
                  .orElseThrow(() -> new ResourceNotFoundException("Dozent", req.dozentId())) : null);
        return toResponse(lerntagRepo.save(lt));
    }

    @Transactional
    public void delete(Integer id) { getEntityById(id); lerntagRepo.deleteById(id); }
}

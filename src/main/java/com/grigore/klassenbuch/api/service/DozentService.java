package com.grigore.klassenbuch.api.service;


import com.grigore.klassenbuch.api.dto.Dto.DozentRequest;
import com.grigore.klassenbuch.api.dto.Dto.DozentResponse;
import com.grigore.klassenbuch.api.entity.Dozent;
import com.grigore.klassenbuch.api.exception.ResourceNotFoundException;
import com.grigore.klassenbuch.api.repository.DozentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DozentService {

    private static final Logger log = LoggerFactory.getLogger(DozentService.class);
    private final DozentRepository repo;

    public DozentService(DozentRepository repo) { this.repo = repo; }

    private DozentResponse toResponse(Dozent d) {
        return new DozentResponse(d.getDozentId(), d.getVorname(), d.getNachname());
    }

    public List<DozentResponse> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public DozentResponse findById(Integer id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dozent", id));
    }

    public Dozent getEntityById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dozent", id));
    }

    @Transactional
    public DozentResponse create(DozentRequest req) {
        log.info("Creating dozent: {} {}", req.vorname(), req.nachname());
        Dozent saved = repo.save(new Dozent(req.vorname(), req.nachname()));
        log.info("Created dozent id={}", saved.getDozentId());
        return toResponse(saved);
    }

    @Transactional
    public DozentResponse update(Integer id, DozentRequest req) {
        Dozent existing = getEntityById(id);
        existing.setVorname(req.vorname());
        existing.setNachname(req.nachname());
        return toResponse(repo.save(existing));
    }

    @Transactional
    public void delete(Integer id) {
        getEntityById(id);   // throws 404 if missing
        log.info("Deleting dozent id={}", id);
        repo.deleteById(id);
    }

}

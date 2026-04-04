package com.grigore.klassenbuch.api.service;

import com.grigore.klassenbuch.api.dto.Dto.LernfeldResponse;
import com.grigore.klassenbuch.api.dto.Dto.LernfeldRequest;
import com.grigore.klassenbuch.api.dto.Dto.DozentResponse;
import com.grigore.klassenbuch.api.dto.Dto.DozentRequest;
import com.grigore.klassenbuch.api.entity.Dozent;
import com.grigore.klassenbuch.api.entity.Lernfeld;
import com.grigore.klassenbuch.api.exception.GlobalExceptionHandler;
import com.grigore.klassenbuch.api.exception.ResourceNotFoundException;
import com.grigore.klassenbuch.api.repository.DozentRepository;
import com.grigore.klassenbuch.api.repository.LernfeldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LernfeldService {

    private static final Logger log = LoggerFactory.getLogger(LernfeldService.class);
    private final LernfeldRepository lernfeldRepo;
    private final DozentRepository dozentRepo;

    public LernfeldService(LernfeldRepository lernfeldRepo,
                           DozentRepository   dozentRepo) {
        this.lernfeldRepo = lernfeldRepo;
        this.dozentRepo   = dozentRepo;
    }

    private DozentResponse toDto(Dozent d) {
        return new DozentResponse(d.getDozentId(), d.getVorname(), d.getNachname());
    }

    private LernfeldResponse toResponse(Lernfeld lf) {
        List<DozentResponse> dozenten = lf.getDozenten().stream().map(this::toDto).toList();
        return new LernfeldResponse(lf.getLernfeldId(), lf.getTitel(),
                lf.getStartDatum(), lf.getEndDatum(), dozenten);
    }

    public Lernfeld getEntityById(String id) {
        return lernfeldRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lernfeld", id));
    }

    public List<LernfeldResponse> findAll() {
        return lernfeldRepo.findAll().stream().map(this::toResponse).toList();
    }

    public LernfeldResponse findById(String id) {
        return toResponse(getEntityById(id));
    }

    public List<DozentResponse> findDozenten(String lernfeldId) {
        return getEntityById(lernfeldId).getDozenten().stream().map(this::toDto).toList();
    }

    @Transactional
    public LernfeldResponse create(LernfeldRequest req) {
        if (lernfeldRepo.existsById(req.lernfeldId()))
            throw new GlobalExceptionHandler.ConflictException("Lernfeld already exists: id=" + req.lernfeldId());
        log.info("Creating lernfeld: {}", req.lernfeldId());
        return toResponse(lernfeldRepo.save(
                new Lernfeld(req.lernfeldId(), req.titel(), req.startDatum(), req.endDatum())));
    }

    @Transactional
    public LernfeldResponse update(String id, LernfeldRequest req) {
        Lernfeld lf = getEntityById(id);
        lf.setTitel(req.titel()); lf.setStartDatum(req.startDatum()); lf.setEndDatum(req.endDatum());
        return toResponse(lernfeldRepo.save(lf));
    }

    @Transactional
    public void delete(String id) { getEntityById(id); lernfeldRepo.deleteById(id); }

    @Transactional
    public LernfeldResponse assignDozent(String lernfeldId, Integer dozentId) {
        Lernfeld lf = getEntityById(lernfeldId);
        Dozent   d  = dozentRepo.findById(dozentId)
                .orElseThrow(() -> new ResourceNotFoundException("Dozent", dozentId));
        if (lf.getDozenten().stream().anyMatch(x -> x.getDozentId().equals(dozentId)))
            throw new GlobalExceptionHandler.ConflictException("Dozent already assigned");
        lf.getDozenten().add(d);
        return toResponse(lernfeldRepo.save(lf));
    }

    @Transactional
    public void removeDozent(String lernfeldId, Integer dozentId) {
        Lernfeld lf = getEntityById(lernfeldId);
        boolean removed = lf.getDozenten().removeIf(d -> d.getDozentId().equals(dozentId));
        if (!removed) throw new ResourceNotFoundException("Dozent assignment", dozentId);
        lernfeldRepo.save(lf);
    }

}

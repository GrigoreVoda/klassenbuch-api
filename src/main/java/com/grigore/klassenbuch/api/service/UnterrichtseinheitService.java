package com.grigore.klassenbuch.api.service;

import com.grigore.klassenbuch.api.dto.Dto.*;
import com.grigore.klassenbuch.api.entity.Lerntag;
import com.grigore.klassenbuch.api.entity.Unterrichtseinheit;
import com.grigore.klassenbuch.api.exception.GlobalExceptionHandler;
import com.grigore.klassenbuch.api.exception.ResourceNotFoundException;
import com.grigore.klassenbuch.api.repository.UnterrichtseinheitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UnterrichtseinheitService {
    private static final Logger log = LoggerFactory.getLogger(UnterrichtseinheitService.class);
    private final UnterrichtseinheitRepository einheitRepo;
    private final LerntagService               lerntagService;

    public UnterrichtseinheitService(
            UnterrichtseinheitRepository repo, LerntagService ls) {
        this.einheitRepo    = repo;
        this.lerntagService = ls;
    }

    private EinheitResponse toResponse(Unterrichtseinheit e) {
        return new EinheitResponse(e.getEinheitId(),
                e.getLerntag().getLerntagId(), e.getStunde(), e.getInhalt());
    }

    private Unterrichtseinheit getAndVerify(Integer lerntagId, Integer einheitId) {
        Unterrichtseinheit e = einheitRepo.findById(einheitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unterrichtseinheit", einheitId));
        if (!e.getLerntag().getLerntagId().equals(lerntagId))
            throw new ResourceNotFoundException("Unterrichtseinheit", einheitId);
        return e;
    }

    public List<EinheitResponse> findAll(Integer lerntagId) {
        return lerntagService.getEntityById(lerntagId)
                .getEinheiten()
                .stream().map(this::toResponse).toList();
    }

    public EinheitResponse findById(Integer lerntagId, Integer id) {
        return toResponse(getAndVerify(lerntagId, id));
    }

    @Transactional
    public EinheitResponse create(Integer lerntagId, EinheitRequest req) {
        Lerntag lt = lerntagService.getEntityById(lerntagId);
        if (einheitRepo.existsByLerntag_LerntagIdAndStunde(lerntagId, req.stunde()))
            throw new GlobalExceptionHandler.ConflictException("Stunde " + req.stunde() + " already exists for lerntag id=" + lerntagId);
        log.info("Creating einheit stunde={} for lerntag={}", req.stunde(), lerntagId);
        return toResponse(einheitRepo.save(new Unterrichtseinheit(lt, req.stunde(), req.inhalt())));
    }

    @Transactional
    public EinheitResponse update(Integer lerntagId, Integer id, EinheitRequest req) {
        Unterrichtseinheit e = getAndVerify(lerntagId, id);
        if (einheitRepo.existsByLerntag_LerntagIdAndStundeAndEinheitIdNot(lerntagId, req.stunde(), id))
            throw new GlobalExceptionHandler.ConflictException("Stunde already taken");
        e.setStunde(req.stunde()); e.setInhalt(req.inhalt());
        return toResponse(einheitRepo.save(e));
    }

    @Transactional
    public void delete(Integer lerntagId, Integer id) {
        getAndVerify(lerntagId, id);
        einheitRepo.deleteById(id);
    }
}

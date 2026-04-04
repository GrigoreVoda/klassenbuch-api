package com.grigore.klassenbuch.api.repository;

import com.grigore.klassenbuch.api.entity.Unterrichtseinheit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnterrichtseinheitRepository extends JpaRepository<Unterrichtseinheit, Integer> {

    List<Unterrichtseinheit> findByLerntag_LerntagIdOrderByStundeAsc(Integer lerntagId);
    boolean existsByLerntag_LerntagIdAndStunde(Integer lerntagId, Integer stunde);
    boolean existsByLerntag_LerntagIdAndStundeAndEinheitIdNot(
            Integer lerntagId, Integer stunde, Integer einheitId);


}

package com.grigore.klassenbuch.api.repository;

import com.grigore.klassenbuch.api.entity.Lerntag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LerntagRepository extends JpaRepository<Lerntag, Integer> {

    List<Lerntag> findByLernfeld_LernfeldIdOrderByDatumAsc(String lernfeldId);
    boolean existsByDatum(LocalDate datum);
    boolean existsByDatumAndLerntagIdNot(LocalDate datum, Integer id);


    @Query("""
    SELECT lt FROM Lerntag lt
    LEFT JOIN FETCH lt.lernfeld
    LEFT JOIN FETCH lt.dozent
    ORDER BY lt.datum ASC
    """)
    List<Lerntag> findAllWithDetails();

    @Query("""
    SELECT lt FROM Lerntag lt
    LEFT JOIN FETCH lt.lernfeld
    LEFT JOIN FETCH lt.dozent
    LEFT JOIN FETCH lt.einheiten   
    WHERE lt.lerntagId = :id
    """)
    Optional<Lerntag> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
    SELECT DISTINCT lt FROM Lerntag lt
    LEFT JOIN FETCH lt.lernfeld
    LEFT JOIN FETCH lt.dozent
    LEFT JOIN FETCH lt.einheiten
    WHERE EXISTS (
        SELECT e FROM Unterrichtseinheit e
        WHERE e.lerntag = lt
          AND LOWER(e.inhalt) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    ORDER BY lt.datum ASC
    """)
    List<Lerntag> searchByEinheitInhalt(@Param("search") String search);
}

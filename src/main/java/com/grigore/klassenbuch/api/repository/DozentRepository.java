package com.grigore.klassenbuch.api.repository;

import com.grigore.klassenbuch.api.entity.Dozent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DozentRepository extends JpaRepository<Dozent, Integer> {

    List<Dozent> findByNachnameContainingIgnoreCase(String nachname);
    boolean existsByVornameAndNachname(String vorname, String nachname);

}

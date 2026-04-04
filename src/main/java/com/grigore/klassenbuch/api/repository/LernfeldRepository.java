package com.grigore.klassenbuch.api.repository;

import com.grigore.klassenbuch.api.entity.Lernfeld;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LernfeldRepository extends JpaRepository<Lernfeld, String> {

   //Optional<Lernfeld> findById(String id);

   //Lernfeld save(Lernfeld lernfeld);
   //Boolean existsById(String id);

}

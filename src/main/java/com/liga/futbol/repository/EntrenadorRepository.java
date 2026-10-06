package com.liga.futbol.repository;

import com.liga.futbol.model.Entrenador;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EntrenadorRepository extends MongoRepository<Entrenador, String> {
}

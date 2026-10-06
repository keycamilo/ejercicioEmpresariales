package com.liga.futbol.repository;

import com.liga.futbol.model.Jugador;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JugadorRepository extends MongoRepository<Jugador, String> {
}

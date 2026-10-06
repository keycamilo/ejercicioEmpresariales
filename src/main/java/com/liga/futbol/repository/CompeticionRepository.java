package com.liga.futbol.repository;

import com.liga.futbol.model.Competicion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompeticionRepository extends MongoRepository<Competicion, String> {
}

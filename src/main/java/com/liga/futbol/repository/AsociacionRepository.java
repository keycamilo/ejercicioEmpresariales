package com.liga.futbol.repository;

import com.liga.futbol.model.Asociacion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AsociacionRepository extends MongoRepository<Asociacion, String> {
}

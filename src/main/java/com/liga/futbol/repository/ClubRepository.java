package com.liga.futbol.repository;

import com.liga.futbol.model.Club;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClubRepository extends MongoRepository<Club, String> {
}

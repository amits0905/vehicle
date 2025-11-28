package com.park_karo.vehicle.parkingspot.repository;

import com.park_karo.vehicle.parkingspot.model.ParkingSpot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSpotRepository extends MongoRepository<ParkingSpot, String> {
    // Basic CRUD methods are inherited.
    @Override
    long count(); // Used to check if data already exists for initialization.
}
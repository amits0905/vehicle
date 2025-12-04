package com.park_karo.vehicle;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

	// Custom finder method
	Vehicle findByLicensePlate(String licensePlate);
}
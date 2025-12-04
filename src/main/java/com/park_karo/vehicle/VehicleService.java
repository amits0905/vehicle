package com.park_karo.vehicle;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	public VehicleService(VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	public Vehicle saveVehicle(Vehicle vehicle) {
		// Prevent duplicate license plates
		if (vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()) != null) {
			throw new RuntimeException("Vehicle with license plate " + vehicle.getLicensePlate() + " already exists.");
		}
		return vehicleRepository.save(vehicle);
	}

	public List<Vehicle> getAllVehicles() {
		return vehicleRepository.findAll();
	}

	public Optional<Vehicle> getVehicleById(String id) {
		return vehicleRepository.findById(id);
	}

	public Vehicle getVehicleByLicensePlate(String licensePlate) {
		return vehicleRepository.findByLicensePlate(licensePlate);
	}

	public void deleteVehicle(String id) {
		vehicleRepository.deleteById(id);
	}
}
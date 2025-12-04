package com.park_karo.vehicle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

	private final VehicleService vehicleService;

	public VehicleController(VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	// POST /api/v1/vehicles
	@PostMapping
	public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
		try {
			Vehicle savedVehicle = vehicleService.saveVehicle(vehicle);
			return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			// 409 Conflict (e.g., license plate already exists)
			return new ResponseEntity<>(null, HttpStatus.CONFLICT);
		}
	}

	// GET /api/v1/vehicles
	@GetMapping
	public ResponseEntity<List<Vehicle>> getAllVehicles() {
		List<Vehicle> vehicles = vehicleService.getAllVehicles();
		return new ResponseEntity<>(vehicles, HttpStatus.OK);
	}

	// GET /api/v1/vehicles/{id}
	@GetMapping("/{id}")
	public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
		return vehicleService.getVehicleById(id).map(vehicle -> new ResponseEntity<>(vehicle, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// DELETE /api/v1/vehicles/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteVehicle(@PathVariable String id) {
		vehicleService.deleteVehicle(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
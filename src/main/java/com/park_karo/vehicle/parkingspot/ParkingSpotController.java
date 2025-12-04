package com.park_karo.vehicle.parkingspot;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parkingspots")
public class ParkingSpotController {

	private final ParkingSpotService parkingSpotService;

	// 1. CONSTRUCTOR MUST COME FIRST!
	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	// 2. Then other methods
	@GetMapping("/test")
	public String testEndpoint() {
		System.out.println("Check--> we hit the url");
		return "We get controlled ";
	}

	@GetMapping
	public ResponseEntity<List<ParkingSpot>> getAllParkingSpots() {
		List<ParkingSpot> spots = parkingSpotService.findAllParkingSpots();
		return ResponseEntity.ok(spots);
	}

	@GetMapping("/nearby")
	public ResponseEntity<List<ParkingSpot>> findNearbyParkingSpots(@RequestParam double lat, @RequestParam double lon,
			@RequestParam double radiusKm) {
		List<ParkingSpot> spots = parkingSpotService.findNearbyParkingSpots(lat, lon, radiusKm);
		return ResponseEntity.ok(spots);
	}

	@PostMapping
	public ResponseEntity<ParkingSpot> createParkingSpot(@RequestBody ParkingSpot parkingSpot) {
		ParkingSpot savedSpot = parkingSpotService.save(parkingSpot);
		return new ResponseEntity<>(savedSpot, HttpStatus.CREATED);
	}
}